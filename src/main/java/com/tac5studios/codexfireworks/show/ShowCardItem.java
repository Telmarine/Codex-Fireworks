package com.tac5studios.codexfireworks.show;

import com.tac5studios.codexfireworks.registry.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Show Card: stores a show so it can be copied between Show Controllers.
 * Hold it while using "Write card" or "Read card" in the controller screen.
 */
public class ShowCardItem extends Item {

    public ShowCardItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag) {
        ShowData show = stack.get(ModComponents.SHOW.get());
        if (show == null) {
            lines.add(Component.translatable("tooltip.codex_fireworks.show_blank").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (!show.name().isEmpty()) lines.add(Component.translatable("tooltip.codex_fireworks.show_name", show.name()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.codex_fireworks.show_cues", show.cues().size(),
                String.format("%.1f", show.length() / 20.0)).withStyle(ChatFormatting.GRAY));
        if (show.loop()) lines.add(Component.translatable("tooltip.codex_fireworks.show_loop").withStyle(ChatFormatting.DARK_GRAY));
    }
}
