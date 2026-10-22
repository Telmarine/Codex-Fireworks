package com.tac5studios.codexfireworks.pattern;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.network.OpenPatternPayload;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pattern Card: holds one drawn burst. Use it (right-click in the air) to open the design screen.
 * Put it in a Firework Workbench burst slot to use the picture; the card isn't used up.
 */
public class PatternCardItem extends Item {

    public PatternCardItem(Properties props) {
        super(props);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.success(stack);
        if (!Features.on(Feature.PATTERNS)) {
            Msg.send(sp, "pattern.off");
            return InteractionResultHolder.fail(stack);
        }
        if (!Perm.has(sp, Perm.DESIGN)) {
            Msg.send(sp, "general.no_permission");
            return InteractionResultHolder.fail(stack);
        }
        boolean hex = Features.on(Feature.HEX_FOR_EVERYONE) || Perm.has(sp, Perm.PATTERN_HEX);
        List<OpenPatternPayload.Preset> presets = new ArrayList<>();
        if (Features.on(Feature.PATTERN_PRESETS)) {
            for (Map.Entry<String, PatternFile.Loaded> e : PatternPresets.all().entrySet()) {
                presets.add(new OpenPatternPayload.Preset(e.getKey(), e.getValue().name(), e.getValue().pattern()));
            }
        }
        PacketDistributor.sendToPlayer(sp, new OpenPatternPayload(hand == InteractionHand.OFF_HAND, hex,
                Features.on(Feature.TEXT_MODE), Limits.MAX_PATTERN_SPARKS.get(), presets));
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        Pattern p = stack.get(ModComponents.PATTERN.get());
        if (p == null || p.isEmpty()) {
            lines.add(Component.translatable("tooltip.codex_fireworks.card_blank").withStyle(ChatFormatting.GRAY));
            return;
        }
        lines.add(Component.translatable("tooltip.codex_fireworks.card_size", p.width(), p.height(), p.sparks()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.codex_fireworks.card_options",
                Component.translatable("pattern.codex_fireworks.size." + p.size()),
                Component.translatable(p.faceViewer() ? "pattern.codex_fireworks.face_viewer" : "pattern.codex_fireworks.face_fixed")).withStyle(ChatFormatting.GRAY));
        if (p.spin()) lines.add(Component.translatable("pattern.codex_fireworks.spin").withStyle(ChatFormatting.DARK_GRAY));
        if (p.sparkle()) lines.add(Component.translatable("pattern.codex_fireworks.sparkle").withStyle(ChatFormatting.DARK_GRAY));
    }
}
