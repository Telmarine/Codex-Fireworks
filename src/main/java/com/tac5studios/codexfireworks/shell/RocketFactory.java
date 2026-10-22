package com.tac5studios.codexfireworks.shell;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

import java.util.ArrayList;
import java.util.List;

/** Turns a shell into a normal firework rocket item. */
public final class RocketFactory {

    /** custom_data key that marks a Mystery Rocket. */
    public static final String MYSTERY_KEY = "codex_fireworks_mystery";

    private RocketFactory() {}

    /**
     * A firework rocket carrying the shell, with vanilla fallback stars.
     * Bursts beyond the server limit are dropped and offsets/delays are clamped.
     *
     * @param rarity "common", "rare", "epic", or "" for no tag
     */
    public static ItemStack rocket(String name, int flight, List<Burst> bursts, float scale, String rarity, int count) {
        int max = Limits.MAX_BURSTS.get();
        List<Burst> kept = new ArrayList<>();
        for (Burst b : bursts) {
            if (kept.size() >= max) break;
            kept.add(b.clamped(Limits.MAX_OFFSET.get(), Limits.MAX_DELAY.get()));
        }
        boolean tagged = Features.on(Feature.RARITY) && Rarity.isTag(rarity);
        Shell shell = new Shell(kept, scale, tagged ? rarity : "");

        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET, count);
        stack.set(DataComponents.FIREWORKS, new Fireworks(Math.max(1, Math.min(3, flight)), shell.fallbackStars()));
        // Our own tooltip lists the bursts; the vanilla star list would only show the fallback.
        stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        if (name != null && !name.isEmpty()) stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        if (tagged) stack.set(DataComponents.RARITY, Rarity.vanilla(rarity));
        shell.write(stack);
        return stack;
    }

    public static ItemStack rocket(String name, int flight, List<Burst> bursts, float scale, int count) {
        return rocket(name, flight, bursts, scale, "", count);
    }

    public static ItemStack rocket(RocketPreset preset, int count) {
        return rocket(preset.name(), preset.flight(), preset.bursts(), preset.scale(), preset.rarity(), count);
    }

    /**
     * A Mystery Rocket: a plain rocket that turns into a random rocket preset when it's launched.
     * Without this mod it pops with its three vanilla stars.
     */
    public static ItemStack mystery(int count) {
        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET, count);
        List<FireworkExplosion> stars = List.of(
                star(FireworkExplosion.Shape.LARGE_BALL, DyeColor.ORANGE, DyeColor.YELLOW),
                star(FireworkExplosion.Shape.STAR, DyeColor.MAGENTA, DyeColor.PINK),
                star(FireworkExplosion.Shape.BURST, DyeColor.LIGHT_BLUE, DyeColor.WHITE));
        stack.set(DataComponents.FIREWORKS, new Fireworks(2, stars));
        stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        stack.set(DataComponents.ITEM_NAME, Component.literal("Mystery Rocket"));
        if (Features.on(Feature.RARITY)) stack.set(DataComponents.RARITY, net.minecraft.world.item.Rarity.EPIC);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, (CompoundTag root) -> root.putBoolean(MYSTERY_KEY, true));
        return stack;
    }

    private static FireworkExplosion star(FireworkExplosion.Shape shape, DyeColor a, DyeColor b) {
        return new FireworkExplosion(shape, new IntArrayList(new int[]{a.getFireworkColor(), b.getFireworkColor()}),
                new IntArrayList(), true, true);
    }

    public static boolean isMystery(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(MYSTERY_KEY);
    }

    /** The first shape in this preset that's switched off, or null when all are allowed. */
    public static String disabledShape(RocketPreset preset) {
        for (Burst b : preset.bursts()) {
            Shape s = Shape.byId(b.shape());
            if (s == null) return b.shape();
            if (!Features.on(s.feature)) return s.id;
        }
        return null;
    }
}
