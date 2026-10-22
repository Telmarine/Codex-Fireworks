package com.tac5studios.codexfireworks.show;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.network.OpenShowPayload;
import com.tac5studios.codexfireworks.network.SaveShowPayload;
import com.tac5studios.codexfireworks.network.ShowActionPayload;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.protect.Protection;
import com.tac5studios.codexfireworks.pattern.PatternSaving;
import com.tac5studios.codexfireworks.registry.ModComponents;
import com.tac5studios.codexfireworks.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Server side of the Show Controller screen: opening it, saving the show and the buttons. */
@SuppressWarnings("resource") // ServerLevel is AutoCloseable, but these levels belong to the server and must not be closed
public final class ShowActions {

    private ShowActions() {}

    public static void open(ServerPlayer player, ShowControllerBlockEntity be) {
        GlobalPos key = GlobalPos.of(player.serverLevel().dimension(), be.getBlockPos());
        PacketDistributor.sendToPlayer(player, new OpenShowPayload(be.getBlockPos(), be.show(), be.launchers(),
                ShowRunner.running(key), Limits.MAX_CUES.get()));
    }

    /** The controller when this player may change it right now, otherwise null (with a message). */
    @Nullable
    private static ShowControllerBlockEntity editable(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        if (!Features.on(Feature.SHOWS)) {
            Msg.send(player, "show.off");
            return null;
        }
        if (player.distanceToSqr(pos.getCenter()) > 64.0 || !level.isLoaded(pos)
                || !(level.getBlockEntity(pos) instanceof ShowControllerBlockEntity be)) return null;
        if (!Perm.has(player, Perm.SHOW_EDIT)) {
            Msg.send(player, "general.no_permission");
            return null;
        }
        if (!Protection.canEdit(player, be.owner(), level, pos)) {
            Msg.send(player, "general.not_yours");
            return null;
        }
        return be;
    }

    public static void save(ServerPlayer player, SaveShowPayload p) {
        ShowControllerBlockEntity be = editable(player, p.pos());
        if (be == null) return;
        ShowData show = clean(p.show(), be.launchers().size());
        if (show.cues().size() > Limits.MAX_CUES.get()) {
            Msg.send(player, "show.too_many_cues", "max", Limits.MAX_CUES.get());
            show = show.withCues(show.cues().subList(0, Limits.MAX_CUES.get()));
        }
        GlobalPos key = GlobalPos.of(player.serverLevel().dimension(), p.pos());
        ShowNames names = ShowNames.get(player.server);
        if (!names.claim(show.name(), key)) {
            Msg.send(player, "show.name_taken", "name", show.name());
            show = show.withName(be.show().name());
        }
        be.setShow(show);
        ShowRunner.update(key, show);
    }

    /** Cues that point at launchers which aren't linked are kept but clamped to the linked count. */
    private static ShowData clean(ShowData show, int launchers) {
        List<Cue> out = new ArrayList<>();
        for (Cue c : show.cues()) out.add(new Cue(c.time(), Math.min(c.launcher(), launchers), c.slot()));
        return show.withCues(out);
    }

    public static void action(ServerPlayer player, ShowActionPayload p) {
        ShowControllerBlockEntity be = editable(player, p.pos());
        if (be == null) return;
        ServerLevel level = player.serverLevel();
        GlobalPos key = GlobalPos.of(level.dimension(), p.pos());
        switch (p.action()) {
            case ShowActionPayload.START -> {
                String err = ShowRunner.start(level, p.pos(), be);
                Msg.send(player, err == null ? "show.started" : err, "max", Limits.MAX_SHOWS_PER_AREA.get());
            }
            case ShowActionPayload.STOP -> {
                if (ShowRunner.stop(key)) Msg.send(player, "show.stopped");
            }
            case ShowActionPayload.WRITE_CARD -> {
                ItemStack held = card(player);
                if (held == null) {
                    Msg.send(player, "show.need_card");
                } else {
                    // From a stack, only the top card is written; it goes back to the inventory on its own.
                    ItemStack card = held.getCount() > 1 ? held.split(1) : held;
                    card.set(ModComponents.SHOW.get(), be.show());
                    if (be.show().name().isEmpty()) card.remove(DataComponents.ITEM_NAME);
                    else card.set(DataComponents.ITEM_NAME, Component.literal("Show Card: " + be.show().name()));
                    PatternSaving.giveBack(player, held, card);
                    Msg.send(player, "show.card_written");
                }
            }
            case ShowActionPayload.READ_CARD -> {
                ItemStack card = writtenCard(player);
                ShowData from = card == null ? null : card.get(ModComponents.SHOW.get());
                if (from == null) {
                    Msg.send(player, card == null ? "show.need_card" : "show.blank_card");
                } else {
                    ShowNames names = ShowNames.get(player.server);
                    ShowData show = clean(from, be.launchers().size());
                    if (!names.claim(show.name(), key)) show = show.withName("");
                    be.setShow(show);
                    ShowRunner.update(key, show);
                    Msg.send(player, "show.card_read");
                }
            }
            case ShowActionPayload.UNLINK -> be.unlink(p.arg());
            case ShowActionPayload.LINK -> {
                Linking.start(player, be);
                return; // the screen closes
            }
            case ShowActionPayload.TEST_CUE -> {
                List<Cue> cues = be.show().cues();
                if (p.arg() >= 0 && p.arg() < cues.size()) ShowRunner.fire(level, be.launchers(), cues.get(p.arg()));
            }
            default -> {
                return;
            }
        }
        open(player, be); // refresh the screen
    }

    /** A written Show Card in either hand, or else any Show Card in hand (so a blank one gets its own message). */
    @Nullable
    private static ItemStack writtenCard(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack s = player.getItemInHand(hand);
            if (s.is(ModItems.SHOW_CARD.get()) && s.has(ModComponents.SHOW.get())) return s;
        }
        return card(player);
    }

    /** A Show Card in the main hand, or else the off hand. */
    @Nullable
    private static ItemStack card(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack s = player.getItemInHand(hand);
            if (s.is(ModItems.SHOW_CARD.get())) return s;
        }
        return null;
    }
}
