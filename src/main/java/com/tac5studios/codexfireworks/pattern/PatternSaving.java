package com.tac5studios.codexfireworks.pattern;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.network.SavePatternPayload;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.registry.ModComponents;
import com.tac5studios.codexfireworks.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

/** Server side of the design screen: checks the picture and writes it on the card. */
public final class PatternSaving {

    /** custom_model_data of a drawn card (see models/item/pattern_card.json). */
    public static final CustomModelData DRAWN_MODEL = new CustomModelData(1);

    private PatternSaving() {}

    public static void save(ServerPlayer player, SavePatternPayload p) {
        ItemStack held = player.getItemInHand(p.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!held.is(ModItems.PATTERN_CARD.get())) {
            Msg.send(player, "pattern.not_holding");
            return;
        }
        if (!Features.on(Feature.PATTERNS)) {
            Msg.send(player, "pattern.off");
            return;
        }
        if (!Perm.has(player, Perm.DESIGN)) {
            Msg.send(player, "general.no_permission");
            return;
        }
        Pattern pattern = p.pattern().compact();
        boolean hex = Features.on(Feature.HEX_FOR_EVERYONE) || Perm.has(player, Perm.PATTERN_HEX);
        if (!hex && !pattern.dyeColorsOnly()) {
            Msg.send(player, "pattern.no_hex");
            return;
        }
        if (!Features.on(Feature.TEXT_MODE) && (pattern.width() > Pattern.DRAW_SIZE || pattern.height() > Pattern.DRAW_SIZE)) {
            Msg.send(player, "pattern.text_off");
            return;
        }
        int max = Limits.MAX_PATTERN_SPARKS.get();
        if (pattern.sparks() > max) {
            Msg.send(player, "pattern.too_many_sparks", "max", max);
            return;
        }
        // From a stack, only the top card is drawn on; it goes back to the inventory on its own.
        ItemStack card = held.getCount() > 1 ? held.split(1) : held;
        if (pattern.isEmpty()) {
            card.remove(ModComponents.PATTERN.get());
            card.remove(DataComponents.ITEM_NAME);
            card.remove(DataComponents.CUSTOM_MODEL_DATA);
            Msg.send(player, "pattern.cleared");
            giveBack(player, held, card);
            return;
        }
        card.set(ModComponents.PATTERN.get(), pattern);
        card.set(DataComponents.CUSTOM_MODEL_DATA, DRAWN_MODEL); // the item model shows a drawn card
        String name = p.name().replace('§', ' ').trim();
        if (name.length() > 32) name = name.substring(0, 32);
        if (name.isEmpty()) card.remove(DataComponents.ITEM_NAME);
        else card.set(DataComponents.ITEM_NAME, Component.literal(name));
        Msg.send(player, "pattern.saved", "sparks", pattern.sparks());
        giveBack(player, held, card);
    }

    /** Gives a card split off a stack back to the player (or drops it when the inventory is full). */
    public static void giveBack(ServerPlayer player, ItemStack held, ItemStack card) {
        if (card == held) return;
        if (!player.getInventory().add(card)) player.drop(card, false);
    }
}
