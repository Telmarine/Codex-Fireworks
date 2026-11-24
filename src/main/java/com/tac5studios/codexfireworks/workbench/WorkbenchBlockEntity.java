package com.tac5studios.codexfireworks.workbench;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.copy.RocketCopy;
import com.tac5studios.codexfireworks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Firework Workbench copying: a template rocket (set by sneak-right-clicking with it) and a small stock of
 * paper, gunpowder and firework stars fed by hoppers or pipes. Every few ticks it makes one copy of the
 * template into the output slot, which hoppers and pipes empty. Rockets are never taken in by pipes, so a
 * pipe that fills launchers can't feed a workbench by mistake.
 * The designer screen doesn't use any of this; it still gives its items back when it closes.
 */
public class WorkbenchBlockEntity extends BlockEntity {

    public static final int PAPER = 0, GUNPOWDER = 1, STARS = 2, OUTPUT = 3, SIZE = 4;

    private final SimpleContainer stock = new SimpleContainer(SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            WorkbenchBlockEntity.this.setChanged();
        }
    };
    private final IItemHandler pipes = new Pipes();
    private ItemStack template = ItemStack.EMPTY;
    private int progress;
    @Nullable
    private UUID owner;

    public WorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WORKBENCH.get(), pos, state);
    }

    public ItemStack template() {
        return template;
    }

    public void setTemplate(ItemStack stack) {
        template = stack.copyWithCount(1);
        progress = 0;
        setChanged();
    }

    public SimpleContainer stock() {
        return stock;
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    /** Takes out the template and everything in stock (for the player taking them back, or the block breaking). */
    public List<ItemStack> takeAll() {
        List<ItemStack> out = new ArrayList<>();
        if (!template.isEmpty()) out.add(template);
        template = ItemStack.EMPTY;
        progress = 0;
        for (int i = 0; i < SIZE; i++) {
            ItemStack s = stock.removeItemNoUpdate(i);
            if (!s.isEmpty()) out.add(s);
        }
        setChanged();
        return out;
    }

    /** True when the template can be copied and there are enough materials and room for one more copy. */
    public boolean canCopy() {
        if (template.isEmpty() || !RocketCopy.copyable(template)) return false;
        if (count(PAPER, Items.PAPER) < Limits.COPY_PAPER.get()) return false;
        if (count(GUNPOWDER, Items.GUNPOWDER) < Limits.COPY_GUNPOWDER.get()) return false;
        if (count(STARS, Items.FIREWORK_STAR) < Limits.COPY_STARS.get()) return false;
        ItemStack out = stock.getItem(OUTPUT);
        return out.isEmpty() || (ItemStack.isSameItemSameComponents(out, template) && out.getCount() < out.getMaxStackSize());
    }

    private int count(int slot, net.minecraft.world.item.Item item) {
        ItemStack s = stock.getItem(slot);
        return s.is(item) ? s.getCount() : 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WorkbenchBlockEntity be) {
        if (!Features.on(Feature.WORKBENCH_COPYING) || !be.canCopy()) {
            be.progress = 0;
            return;
        }
        if (++be.progress < Limits.COPY_TICKS.get()) return;
        be.progress = 0;
        be.stock.removeItem(PAPER, Limits.COPY_PAPER.get());
        be.stock.removeItem(GUNPOWDER, Limits.COPY_GUNPOWDER.get());
        be.stock.removeItem(STARS, Limits.COPY_STARS.get());
        ItemStack out = be.stock.getItem(OUTPUT);
        if (out.isEmpty()) be.stock.setItem(OUTPUT, be.template.copyWithCount(1));
        else {
            out.grow(1);
            be.stock.setChanged();
        }
    }

    /** Item handler for hoppers and pipes. */
    public IItemHandler pipes() {
        return pipes;
    }

    /** Paper, gunpowder and firework stars go in their own slot; only finished copies come out. */
    private final class Pipes implements IItemHandler {

        @Override
        public int getSlots() {
            return SIZE;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return stock.getItem(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
            ItemStack in = stock.getItem(slot);
            if (!in.isEmpty() && !ItemStack.isSameItemSameComponents(in, stack)) return stack;
            int moved = Math.min(stack.getMaxStackSize() - in.getCount(), stack.getCount());
            if (moved <= 0) return stack;
            if (!simulate) {
                if (in.isEmpty()) stock.setItem(slot, stack.copyWithCount(moved));
                else {
                    in.grow(moved);
                    stock.setChanged();
                }
            }
            return stack.copyWithCount(stack.getCount() - moved);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != OUTPUT || amount <= 0) return ItemStack.EMPTY;
            ItemStack in = stock.getItem(OUTPUT);
            if (in.isEmpty()) return ItemStack.EMPTY;
            int n = Math.min(amount, in.getCount());
            return simulate ? in.copyWithCount(n) : stock.removeItem(OUTPUT, n);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (!Features.on(Feature.WORKBENCH_COPYING)) return false;
            return switch (slot) {
                case PAPER -> stack.is(Items.PAPER);
                case GUNPOWDER -> stack.is(Items.GUNPOWDER);
                case STARS -> stack.is(Items.FIREWORK_STAR);
                default -> false;
            };
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        for (int i = 0; i < SIZE; i++) list.set(i, stock.getItem(i));
        ContainerHelper.saveAllItems(tag, list, registries);
        if (!template.isEmpty()) tag.put("Template", template.save(registries));
        tag.putInt("Progress", progress);
        if (owner != null) tag.putUUID("Owner", owner);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        NonNullList<ItemStack> list = NonNullList.withSize(SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, list, registries);
        for (int i = 0; i < SIZE; i++) stock.setItem(i, list.get(i));
        template = tag.contains("Template") ? ItemStack.parse(registries, tag.getCompound("Template")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        progress = tag.getInt("Progress");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
