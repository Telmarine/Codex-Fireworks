package com.tac5studios.codexfireworks.shell;

import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;

import java.util.ArrayList;
import java.util.List;

/** Turns a shell into a normal firework rocket item. */
public final class RocketFactory {

    private RocketFactory() {}

    /**
     * A firework rocket carrying the shell, with vanilla fallback stars.
     * Bursts beyond the server limit are dropped and offsets/delays are clamped.
     */
    public static ItemStack rocket(String name, int flight, List<Burst> bursts, float scale, int count) {
        int max = Limits.MAX_BURSTS.get();
        List<Burst> kept = new ArrayList<>();
        for (Burst b : bursts) {
            if (kept.size() >= max) break;
            kept.add(b.clamped(Limits.MAX_OFFSET.get(), Limits.MAX_DELAY.get()));
        }
        Shell shell = new Shell(kept, scale);

        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET, count);
        stack.set(DataComponents.FIREWORKS, new Fireworks(Math.max(1, Math.min(3, flight)), shell.fallbackStars()));
        // Our own tooltip lists the bursts; the vanilla star list would only show the fallback.
        stack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        if (name != null && !name.isEmpty()) stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        shell.write(stack);
        return stack;
    }

    public static ItemStack rocket(RocketPreset preset, int count) {
        return rocket(preset.name(), preset.flight(), preset.bursts(), preset.scale(), count);
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
