package com.tac5studios.codexfireworks.launcher;

import com.tac5studios.codexfireworks.protect.Protection;
import com.tac5studios.codexfireworks.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Launcher screen: 9 rocket slots and the firing settings (synced as menu data, changed with buttons). */
public final class LauncherMenu extends AbstractContainerMenu {

    public static final int SLOT_X = 8, SLOT_Y = 18, INV_X = 8, INV_Y = 96;
    /** Added to a button id when Shift is held (bigger steps). */
    public static final int SHIFT = 100;

    private final Container rockets;
    private final ContainerData data;
    @Nullable
    private final LauncherBlockEntity launcher;
    public final BlockPos pos;

    /** Server side. */
    public LauncherMenu(int id, Inventory inv, LauncherBlockEntity launcher, ContainerData data) {
        super(ModMenus.LAUNCHER.get(), id);
        this.rockets = launcher.items();
        this.data = data;
        this.launcher = launcher;
        this.pos = launcher.getBlockPos();
        addSlots(inv);
    }

    /** Client side. */
    public LauncherMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        super(ModMenus.LAUNCHER.get(), id);
        this.rockets = new SimpleContainer(LauncherBlockEntity.SLOTS);
        this.data = new SimpleContainerData(LauncherBlockEntity.DATA_COUNT);
        this.launcher = null;
        this.pos = buf.readBlockPos();
        addSlots(inv);
    }

    private void addSlots(Inventory inv) {
        checkContainerSize(rockets, LauncherBlockEntity.SLOTS);
        for (int i = 0; i < LauncherBlockEntity.SLOTS; i++) {
            addSlot(new Slot(rockets, i, SLOT_X + (i % 3) * 18, SLOT_Y + (i / 3) * 18) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return stack.is(Items.FIREWORK_ROCKET);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col + row * 9 + 9, INV_X + col * 18, INV_Y + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, INV_X + col * 18, INV_Y + 58));
        addDataSlots(data);
    }

    public int get(int setting) {
        return data.get(setting);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int id) {
        if (launcher == null || !(player instanceof ServerPlayer sp)) return false;
        if (!Protection.canEdit(sp, launcher.owner(), launcher.getLevel() == null ? player.level() : launcher.getLevel(), pos)) return false;
        launcher.button(id % SHIFT, id >= SHIFT);
        return true;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = getSlot(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int n = LauncherBlockEntity.SLOTS;
        if (index < n) {
            if (!moveItemStackTo(stack, n, n + 36, true)) return ItemStack.EMPTY;
        } else if (stack.is(Items.FIREWORK_ROCKET)) {
            if (!moveItemStackTo(stack, 0, n, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return launcher == null || (!launcher.isRemoved() && player.distanceToSqr(pos.getCenter()) <= 64.0);
    }
}
