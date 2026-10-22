package com.tac5studios.codexfireworks.workbench;

import com.mojang.serialization.MapCodec;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.util.Mining;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/** Firework Workbench: opens the rocket designer. */
public class WorkbenchBlock extends Block {

    public static final MapCodec<WorkbenchBlock> CODEC = simpleCodec(WorkbenchBlock::new);
    private static final Component TITLE = Component.translatable("container.codex_fireworks.firework_workbench");

    public WorkbenchBlock(Properties props) {
        super(props);
    }

    @Override
    protected @NotNull MapCodec<WorkbenchBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp)) return InteractionResult.PASS;
        if (!Features.enabled() || (!Features.on(Feature.SHELLS) && !Features.on(Feature.ROCKET_PRESETS))) {
            Msg.send(sp, "workbench.shells_off");
            return InteractionResult.CONSUME;
        }
        if (!Perm.has(sp, Perm.DESIGN)) {
            Msg.send(sp, "general.no_permission");
            return InteractionResult.CONSUME;
        }
        sp.openMenu(new SimpleMenuProvider((id, inv, p) -> new WorkbenchMenu(id, inv, ContainerLevelAccess.create(level, pos)), TITLE),
                WorkbenchMenu::writeOpenData);
        return InteractionResult.CONSUME;
    }

    @Override
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Mining.progress(player, pos, Blocks.OAK_PLANKS.defaultBlockState(), state.getDestroySpeed(level, pos));
    }
}
