package com.tac5studios.codexfireworks.launcher;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/**
 * Launcher: holds up to 9 stacks of firework rockets and fires them on a redstone pulse
 * (a volley of "count" rockets, "interval" ticks apart) or when a Show Controller cue says so.
 * Settings: direction (yaw), tilt (angle from straight up), random spread, count and interval.
 */
public class LauncherBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOTS = 9;
    /** Index of each setting in the menu data. */
    public static final int YAW = 0, ANGLE = 1, SPREAD = 2, COUNT = 3, INTERVAL = 4, DATA_COUNT = 5;

    private final SimpleContainer items = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            LauncherBlockEntity.this.setChanged();
        }

        @Override
        public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
            return stack.is(Items.FIREWORK_ROCKET);
        }
    };

    private int yaw = 0, angle = 0, spread = 0, count = 1, interval = 10;
    private int nextSlot = 0;
    private boolean powered;
    @Nullable
    private UUID owner;

    /** Shots waiting to go: each is {slot (-1 = next rocket), ticks to wait after the previous shot}. */
    private final Deque<int[]> queue = new ArrayDeque<>();
    private long lastShot = -1000;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int i) {
            return switch (i) {
                case YAW -> yaw;
                case ANGLE -> angle;
                case SPREAD -> spread;
                case COUNT -> count;
                case INTERVAL -> interval;
                default -> 0;
            };
        }

        @Override
        public void set(int i, int v) {
            switch (i) {
                case YAW -> yaw = v;
                case ANGLE -> angle = v;
                case SPREAD -> spread = v;
                case COUNT -> count = v;
                case INTERVAL -> interval = v;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public LauncherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAUNCHER.get(), pos, state);
    }

    public SimpleContainer items() {
        return items;
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    // ---- settings (changed by the menu buttons) ----

    /** Button ids: 0/1 direction -/+, 2/3 tilt, 4/5 spread, 6/7 count, 8/9 interval, 10 test shot. */
    public void button(int id, boolean big) {
        int minInterval = Limits.MIN_INTERVAL.get();
        switch (id) {
            case 0 -> yaw = Math.floorMod(yaw - 15, 360);
            case 1 -> yaw = Math.floorMod(yaw + 15, 360);
            case 2 -> angle = Math.max(0, angle - 5);
            case 3 -> angle = Math.min(60, angle + 5);
            case 4 -> spread = Math.max(0, spread - 2);
            case 5 -> spread = Math.min(30, spread + 2);
            case 6 -> count = Math.max(1, count - 1);
            case 7 -> count = Math.min(9, count + 1);
            case 8 -> interval = Math.max(minInterval, interval - (big ? 10 : 1));
            case 9 -> interval = Math.min(100, interval + (big ? 10 : 1));
            case 10 -> fire(-1, 0);
            default -> { }
        }
        interval = Math.max(minInterval, interval);
        setChanged();
    }

    // ---- firing ----

    /** Redstone changed. A rising edge fires a volley. */
    public void redstone(boolean now) {
        if (now && !powered) volley();
        powered = now;
        setChanged();
    }

    /** "count" rockets, "interval" ticks apart. */
    public void volley() {
        for (int i = 0; i < count; i++) fire(-1, i == 0 ? 0 : interval);
    }

    /**
     * Queue one shot. The server limit min_interval_ticks is always kept between two shots.
     *
     * @param slot 0-8, or -1 for the next rocket in turn
     * @param wait ticks to wait after the previous shot
     */
    public void fire(int slot, int wait) {
        if (queue.size() < 64) queue.add(new int[]{slot, wait});
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LauncherBlockEntity be) {
        int[] next = be.queue.peek();
        if (next == null) return;
        if (!Features.on(Feature.LAUNCHERS)) {
            be.queue.clear();
            return;
        }
        long now = level.getGameTime();
        if (now - be.lastShot < Math.max(next[1], Limits.MIN_INTERVAL.get())) return;
        be.queue.poll();
        be.lastShot = now;
        be.launch(level, pos, next[0]);
    }

    private void launch(Level level, BlockPos pos, int slot) {
        ItemStack stack = take(slot);
        if (stack.isEmpty()) {
            level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1f, 1.2f);
            return;
        }
        RandomSource r = level.getRandom();
        double x = pos.getX() + 0.5, y = pos.getY() + 1.05, z = pos.getZ() + 0.5;
        FireworkRocketEntity rocket;
        if (angle == 0 && spread == 0) {
            rocket = new FireworkRocketEntity(level, x, y, z, stack);
        } else {
            double tilt = Math.toRadians(angle + (r.nextDouble() * 2 - 1) * spread);
            double turn = Math.toRadians(yaw + (r.nextDouble() * 2 - 1) * spread * 3);
            Vec3 dir = new Vec3(-Math.sin(turn) * Math.sin(tilt), Math.cos(tilt), Math.cos(turn) * Math.sin(tilt));
            rocket = new FireworkRocketEntity(level, stack, x, y, z, true);
            rocket.shoot(dir.x, dir.y, dir.z, 1.1f, 0f);
        }
        level.addFreshEntity(rocket);
        level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 3f, 1f);
    }

    /** Take one rocket from this slot, or from the next filled slot in turn when slot is -1 or empty. */
    private ItemStack take(int slot) {
        if (slot >= 0 && slot < SLOTS && !items.getItem(slot).isEmpty()) {
            return items.removeItem(slot, 1);
        }
        for (int k = 0; k < SLOTS; k++) {
            int i = Mth.positiveModulo(nextSlot + k, SLOTS);
            if (!items.getItem(i).isEmpty()) {
                nextSlot = (i + 1) % SLOTS;
                return items.removeItem(i, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    public int rocketCount() {
        int n = 0;
        for (int i = 0; i < SLOTS; i++) n += items.getItem(i).getCount();
        return n;
    }

    // ---- saving ----

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        NonNullList<ItemStack> list = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < SLOTS; i++) list.set(i, items.getItem(i));
        ContainerHelper.saveAllItems(tag, list, registries);
        tag.putInt("Yaw", yaw);
        tag.putInt("Angle", angle);
        tag.putInt("Spread", spread);
        tag.putInt("Count", count);
        tag.putInt("Interval", interval);
        tag.putInt("NextSlot", nextSlot);
        tag.putBoolean("Powered", powered);
        if (owner != null) tag.putUUID("Owner", owner);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        NonNullList<ItemStack> list = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, list, registries);
        for (int i = 0; i < SLOTS; i++) items.setItem(i, list.get(i));
        yaw = tag.getInt("Yaw");
        angle = tag.getInt("Angle");
        spread = tag.getInt("Spread");
        count = tag.contains("Count") ? tag.getInt("Count") : 1;
        interval = tag.contains("Interval") ? tag.getInt("Interval") : 10;
        nextSlot = tag.getInt("NextSlot");
        powered = tag.getBoolean("Powered");
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    // ---- menu ----

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.codex_fireworks.launcher");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inv, @NotNull Player player) {
        return new LauncherMenu(id, inv, this, data);
    }
}
