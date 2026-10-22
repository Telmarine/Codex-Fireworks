package com.tac5studios.codexfireworks.launcher;

import com.mojang.serialization.MapCodec;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.protect.Protection;
import com.tac5studios.codexfireworks.registry.ModBlockEntities;
import com.tac5studios.codexfireworks.show.Linking;
import com.tac5studios.codexfireworks.util.Mining;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Launcher block. Right-click to load rockets and change settings; a redstone pulse fires a volley.
 * Shift-right-click with an empty hand while linking adds or removes it from a Show Controller.
 */
public class LauncherBlock extends Block implements EntityBlock {

    public static final MapCodec<LauncherBlock> CODEC = simpleCodec(LauncherBlock::new);

    public LauncherBlock(Properties props) {
        super(props);
    }

    @Override
    protected @NotNull MapCodec<LauncherBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new LauncherBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.LAUNCHER.get()) return null;
        return (lvl, pos, st, be) -> LauncherBlockEntity.serverTick(lvl, pos, st, (LauncherBlockEntity) be);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player p && level.getBlockEntity(pos) instanceof LauncherBlockEntity be) {
            be.setOwner(p.getUUID());
        }
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp) || !(level.getBlockEntity(pos) instanceof LauncherBlockEntity be)) return InteractionResult.PASS;
        if (!Features.on(Feature.LAUNCHERS)) {
            Msg.send(sp, "launcher.off");
            return InteractionResult.CONSUME;
        }
        if (sp.isShiftKeyDown() && Linking.active(sp)) {
            Linking.toggleLauncher(sp, be);
            return InteractionResult.CONSUME;
        }
        if (!Perm.has(sp, Perm.LAUNCHER_USE)) {
            Msg.send(sp, "general.no_permission");
            return InteractionResult.CONSUME;
        }
        if (!Protection.canEdit(sp, be.owner(), level, pos)) {
            Msg.send(sp, "general.not_yours");
            return InteractionResult.CONSUME;
        }
        sp.openMenu(be, buf -> buf.writeBlockPos(pos));
        return InteractionResult.CONSUME;
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block,
                                   @NotNull BlockPos from, boolean moving) {
        super.neighborChanged(state, level, pos, block, from, moving);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof LauncherBlockEntity be) {
            be.redstone(level.hasNeighborSignal(pos));
        }
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof LauncherBlockEntity be) {
            Containers.dropContents(level, pos, be.items());
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Mining.progress(player, pos, Blocks.STONE.defaultBlockState(), state.getDestroySpeed(level, pos));
    }
}
