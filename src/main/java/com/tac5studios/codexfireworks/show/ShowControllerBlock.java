package com.tac5studios.codexfireworks.show;

import com.mojang.serialization.MapCodec;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.protect.Protection;
import com.tac5studios.codexfireworks.util.Mining;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Show Controller block. Right-click to edit the show; shift-right-click with an empty hand to link
 * launchers; a redstone pulse starts the show when "start on redstone" is on.
 */
public class ShowControllerBlock extends Block implements EntityBlock {

    public static final MapCodec<ShowControllerBlock> CODEC = simpleCodec(ShowControllerBlock::new);

    public ShowControllerBlock(Properties props) {
        super(props);
    }

    @Override
    protected @NotNull MapCodec<ShowControllerBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ShowControllerBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player p && level.getBlockEntity(pos) instanceof ShowControllerBlockEntity be) {
            be.setOwner(p.getUUID());
        }
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer sp) || !(level.getBlockEntity(pos) instanceof ShowControllerBlockEntity be)) return InteractionResult.PASS;
        if (!Features.on(Feature.SHOWS)) {
            Msg.send(sp, "show.off");
            return InteractionResult.CONSUME;
        }
        if (!Perm.has(sp, Perm.SHOW_EDIT)) {
            Msg.send(sp, "general.no_permission");
            return InteractionResult.CONSUME;
        }
        if (!Protection.canEdit(sp, be.owner(), level, pos)) {
            Msg.send(sp, "general.not_yours");
            return InteractionResult.CONSUME;
        }
        if (sp.isShiftKeyDown()) Linking.startOrFinish(sp, be);
        else ShowActions.open(sp, be);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block block,
                                   @NotNull BlockPos from, boolean moving) {
        super.neighborChanged(state, level, pos, block, from, moving);
        if (level instanceof ServerLevel sl && level.getBlockEntity(pos) instanceof ShowControllerBlockEntity be) {
            if (be.redstone(level.hasNeighborSignal(pos)) && be.show().redstoneStart()) ShowRunner.start(sl, pos, be);
        }
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel sl) {
            GlobalPos key = GlobalPos.of(sl.dimension(), pos);
            ShowRunner.stop(key);
            ShowNames.get(sl.getServer()).release(key);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    protected float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Mining.progress(player, pos, Blocks.STONE.defaultBlockState(), state.getDestroySpeed(level, pos));
    }
}
