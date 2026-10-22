package com.tac5studios.codexfireworks.workbench;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.registry.ModBlocks;
import com.tac5studios.codexfireworks.registry.ModItems;
import com.tac5studios.codexfireworks.registry.ModMenus;
import com.tac5studios.codexfireworks.shell.RocketFactory;
import com.tac5studios.codexfireworks.shell.RocketPreset;
import com.tac5studios.codexfireworks.shell.RocketPresets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Firework Workbench: slot 0 paper, slot 1 gunpowder (1-3 for the flight), slots 2-9 bursts
 * (firework stars or Pattern Cards). The formation is edited in the screen and sent when crafting.
 * Items are given back when the screen closes, like a crafting table.
 */
public final class WorkbenchMenu extends AbstractContainerMenu {

    public static final int PAPER = 0, GUNPOWDER = 1, FIRST_BURST = 2, BURSTS = 8, SIZE = 10;

    /** Slot positions in the screen (shared with WorkbenchScreen). */
    public static final int PAPER_X = 8, PAPER_Y = 20, GUNPOWDER_X = 28, GUNPOWDER_Y = 20;
    public static final int BURST_X = 8, BURST_Y = 44, INV_X = 8, INV_Y = 154;

    private static final StreamCodec<ByteBuf, RocketPreset> PRESET_CODEC = ByteBufCodecs.fromCodec(RocketPreset.CODEC);

    /** A rocket preset as the screen lists it. */
    public record PresetEntry(String id, RocketPreset preset) {}

    private final Container items;
    private final ContainerLevelAccess access;

    // Sent by the server when the screen opens.
    public final int maxBursts, maxDelay, maxOffset, presetGunpowder;
    public final boolean shellsOn, presetsOn;
    public final List<PresetEntry> presets;

    /** Server side. */
    public WorkbenchMenu(int id, Inventory inv, ContainerLevelAccess access) {
        super(ModMenus.WORKBENCH.get(), id);
        this.items = new SimpleContainer(SIZE);
        this.access = access;
        this.maxBursts = Limits.MAX_BURSTS.get();
        this.maxDelay = Limits.MAX_DELAY.get();
        this.maxOffset = Limits.MAX_OFFSET.get();
        this.presetGunpowder = Limits.PRESET_GUNPOWDER.get();
        this.shellsOn = Features.on(Feature.SHELLS);
        this.presetsOn = Features.on(Feature.ROCKET_PRESETS);
        this.presets = List.of(); // the server never needs the list here
        addSlots(inv);
    }

    /** Client side: reads what {@link #writeOpenData} wrote. */
    public WorkbenchMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        super(ModMenus.WORKBENCH.get(), id);
        this.items = new SimpleContainer(SIZE);
        this.access = ContainerLevelAccess.NULL;
        this.maxBursts = buf.readVarInt();
        this.maxDelay = buf.readVarInt();
        this.maxOffset = buf.readVarInt();
        this.presetGunpowder = buf.readVarInt();
        this.shellsOn = buf.readBoolean();
        this.presetsOn = buf.readBoolean();
        int n = buf.readVarInt();
        List<PresetEntry> list = new ArrayList<>();
        for (int i = 0; i < n; i++) list.add(new PresetEntry(buf.readUtf(), PRESET_CODEC.decode(buf)));
        this.presets = List.copyOf(list);
        addSlots(inv);
    }

    /** Written by the server when the screen opens: limits, switches, and the presets that can be made. */
    public static void writeOpenData(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(Limits.MAX_BURSTS.get());
        buf.writeVarInt(Limits.MAX_DELAY.get());
        buf.writeVarInt(Limits.MAX_OFFSET.get());
        buf.writeVarInt(Limits.PRESET_GUNPOWDER.get());
        buf.writeBoolean(Features.on(Feature.SHELLS));
        boolean presets = Features.on(Feature.ROCKET_PRESETS);
        buf.writeBoolean(presets);
        List<Map.Entry<String, RocketPreset>> list = new ArrayList<>();
        if (presets) {
            for (Map.Entry<String, RocketPreset> e : RocketPresets.all().entrySet()) {
                if (RocketFactory.disabledShape(e.getValue()) == null) list.add(e);
            }
        }
        buf.writeVarInt(list.size());
        for (Map.Entry<String, RocketPreset> e : list) {
            buf.writeUtf(e.getKey());
            PRESET_CODEC.encode(buf, e.getValue());
        }
    }

    private void addSlots(Inventory inv) {
        addSlot(new Slot(items, PAPER, PAPER_X, PAPER_Y) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Items.PAPER);
            }
        });
        addSlot(new Slot(items, GUNPOWDER, GUNPOWDER_X, GUNPOWDER_Y) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Items.GUNPOWDER);
            }
        });
        for (int i = 0; i < BURSTS; i++) {
            addSlot(new Slot(items, FIRST_BURST + i, BURST_X + (i % 4) * 18, BURST_Y + (i / 4) * 18) {
                @Override
                public boolean mayPlace(@NotNull ItemStack stack) {
                    return isBurstItem(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 16;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, INV_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inv, col, INV_X + col * 18, INV_Y + 58));
    }

    public static boolean isBurstItem(ItemStack stack) {
        return stack.is(Items.FIREWORK_STAR) || stack.is(ModItems.PATTERN_CARD.get());
    }

    public Container container() {
        return items;
    }

    public ItemStack burstItem(int i) {
        return items.getItem(FIRST_BURST + i);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = getSlot(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < SIZE) {
            if (!moveItemStackTo(stack, SIZE, SIZE + 36, true)) return ItemStack.EMPTY;
        } else if (stack.is(Items.PAPER)) {
            if (!moveItemStackTo(stack, PAPER, PAPER + 1, false)) return ItemStack.EMPTY;
        } else if (stack.is(Items.GUNPOWDER)) {
            if (!moveItemStackTo(stack, GUNPOWDER, GUNPOWDER + 1, false)) return ItemStack.EMPTY;
        } else if (isBurstItem(stack)) {
            if (!moveItemStackTo(stack, FIRST_BURST, FIRST_BURST + BURSTS, false)) return ItemStack.EMPTY;
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
        return stillValid(access, player, ModBlocks.WORKBENCH.get());
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, items));
    }
}
