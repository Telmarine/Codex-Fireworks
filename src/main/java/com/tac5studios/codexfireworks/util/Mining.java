package com.tac5studios.codexfireworks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Breaking speed for this mod's blocks. They mine like the vanilla block given as {@code like}
 * (planks for the workbench, stone for launchers and controllers), so the right tool is faster,
 * but every tool (or a bare hand) still drops the block.
 */
public final class Mining {

    private Mining() {}

    public static float progress(Player player, BlockPos pos, BlockState like, float hardness) {
        if (hardness <= 0) return 1f;
        return player.getDigSpeed(like, pos) / hardness / 30f;
    }
}
