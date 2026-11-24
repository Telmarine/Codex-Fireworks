package com.tac5studios.codexfireworks.workbench;

import com.mojang.serialization.MapCodec;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.registry.ModBlockEntities;
import com.tac5studios.codexfireworks.util.Mining;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
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
 * Firework Workbench: opens the rocket designer. It also copies a template rocket for hoppers and pipes
 * (see {@link WorkbenchBlockEntity} and {@link WorkbenchClicks}).
 */
public class WorkbenchBlock extends Block implements EntityBlock {

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
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new WorkbenchBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.WORKBENCH.get()) return null;
        return (lvl, pos, st, be) -> WorkbenchBlockEntity.serverTick(lvl, pos, st, (WorkbenchBlockEntity) be);
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player p && level.getBlockEntity(pos) instanceof WorkbenchBlockEntity be) {
            be.setOwner(p.getUUID());
        }
    }

    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof WorkbenchBlockEntity be) {
            for (ItemStack s : be.takeAll()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), s);
        }
        super.onRemove(state, level, pos, newState, moving);
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
