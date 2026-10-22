package com.tac5studios.codexfireworks.workbench;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.pattern.Pattern;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.registry.ModComponents;
import com.tac5studios.codexfireworks.registry.ModItems;
import com.tac5studios.codexfireworks.shell.Burst;
import com.tac5studios.codexfireworks.shell.RocketFactory;
import com.tac5studios.codexfireworks.shell.RocketPreset;
import com.tac5studios.codexfireworks.shell.RocketPresets;
import com.tac5studios.codexfireworks.shell.Shape;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;

import java.util.ArrayList;
import java.util.List;

/**
 * Turning the workbench slots into a rocket. The burst list is built the same way on the client
 * (for the preview) and on the server (for the real rocket); only the server takes items.
 */
public final class WorkbenchCrafting {

    public static final int MAX_NAME = 32;

    private WorkbenchCrafting() {}

    /** The shape a vanilla star turns into before the player picks another one. */
    public static String defaultShape(ItemStack star) {
        FireworkExplosion fx = star.get(DataComponents.FIREWORK_EXPLOSION);
        if (fx == null) return Shape.PEONY.id;
        return switch (fx.shape()) {
            case LARGE_BALL -> Shape.CHRYSANTHEMUM.id;
            case STAR -> Shape.HEART.id;
            case CREEPER -> Shape.CRACKLE.id;
            case BURST -> Shape.PALM.id;
            default -> Shape.PEONY.id;
        };
    }

    /**
     * The bursts for the filled burst slots, in slot order.
     *
     * @param layouts one per burst slot (8)
     * @param error   gets a messages.toml key when something is wrong (then the result is null)
     */
    public static List<Burst> bursts(Container c, List<SlotLayout> layouts, int maxOffset, int maxDelay, String[] error) {
        List<Burst> out = new ArrayList<>();
        for (int i = 0; i < WorkbenchMenu.BURSTS; i++) {
            ItemStack stack = c.getItem(WorkbenchMenu.FIRST_BURST + i);
            if (stack.isEmpty()) continue;
            SlotLayout l = i < layouts.size() ? layouts.get(i) : SlotLayout.DEFAULT;
            float dx = clamp(l.dx(), maxOffset), dy = clamp(l.dy(), maxOffset), dz = clamp(l.dz(), maxOffset);
            int delay = Math.max(0, Math.min(maxDelay, l.delay()));
            float size = Math.max(0.25f, Math.min(2.0f, l.size()));

            if (stack.is(ModItems.PATTERN_CARD.get())) {
                Pattern p = stack.get(ModComponents.PATTERN.get());
                if (p == null || p.isEmpty()) {
                    error[0] = "workbench.blank_card";
                    return null;
                }
                out.add(new Burst(Shape.PATTERN.id, p.usedColors(), p.fade() >= 0 ? List.of(p.fade()) : List.of(),
                        false, p.sparkle(), size, dx, dy, dz, delay, p));
            } else if (stack.is(Items.FIREWORK_STAR)) {
                FireworkExplosion fx = stack.get(DataComponents.FIREWORK_EXPLOSION);
                List<Integer> colors = new ArrayList<>();
                List<Integer> fades = new ArrayList<>();
                boolean trail = false, twinkle = false;
                if (fx != null) {
                    for (int col : fx.colors()) if (colors.size() < 8) colors.add(col);
                    for (int col : fx.fadeColors()) if (fades.size() < 8) fades.add(col);
                    trail = fx.hasTrail();
                    twinkle = fx.hasTwinkle();
                }
                if (colors.isEmpty()) colors.add(0xFFFFFF);
                String shape = l.shape().isEmpty() ? defaultShape(stack) : l.shape();
                Shape s = Shape.byId(shape);
                if (s == null || s == Shape.PATTERN) s = Shape.PEONY;
                out.add(new Burst(s.id, colors, fades, trail, twinkle, size, dx, dy, dz, delay));
            }
        }
        if (out.isEmpty()) {
            error[0] = "workbench.no_bursts";
            return null;
        }
        return out;
    }

    private static float clamp(float v, int max) {
        return Math.max(-max, Math.min(max, v));
    }

    /** Server: make a rocket from the slots and the player's formation. */
    public static void craftRocket(ServerPlayer player, WorkbenchMenu menu, List<SlotLayout> layouts, int flight, String name) {
        if (!Features.on(Feature.SHELLS)) {
            Msg.send(player, "workbench.shells_off");
            return;
        }
        if (!Perm.has(player, Perm.DESIGN)) {
            Msg.send(player, "general.no_permission");
            return;
        }
        Container c = menu.container();
        flight = Math.max(1, Math.min(3, flight));
        if (!c.getItem(WorkbenchMenu.PAPER).is(Items.PAPER)) {
            Msg.send(player, "workbench.need_paper");
            return;
        }
        if (!c.getItem(WorkbenchMenu.GUNPOWDER).is(Items.GUNPOWDER) || c.getItem(WorkbenchMenu.GUNPOWDER).getCount() < flight) {
            Msg.send(player, "workbench.need_gunpowder", "amount", flight);
            return;
        }
        String[] err = new String[1];
        List<Burst> bursts = bursts(c, layouts, Limits.MAX_OFFSET.get(), Limits.MAX_DELAY.get(), err);
        if (bursts == null) {
            Msg.send(player, err[0]);
            return;
        }
        if (bursts.size() > Limits.MAX_BURSTS.get()) {
            Msg.send(player, "workbench.too_many", "max", Limits.MAX_BURSTS.get());
            return;
        }
        for (Burst b : bursts) {
            Shape s = Shape.byId(b.shape());
            if (s == null || !Features.on(s.feature)) {
                Msg.send(player, "workbench.shape_off", "shape", b.shape());
                return;
            }
            if (b.pattern() != null && b.pattern().sparks() > Limits.MAX_PATTERN_SPARKS.get()) {
                Msg.send(player, "pattern.too_many_sparks", "max", Limits.MAX_PATTERN_SPARKS.get());
                return;
            }
        }

        // Take the items: paper, gunpowder for the flight, and one star per star burst (cards stay).
        c.getItem(WorkbenchMenu.PAPER).shrink(1);
        c.getItem(WorkbenchMenu.GUNPOWDER).shrink(flight);
        for (int i = 0; i < WorkbenchMenu.BURSTS; i++) {
            ItemStack stack = c.getItem(WorkbenchMenu.FIRST_BURST + i);
            if (stack.is(Items.FIREWORK_STAR)) stack.shrink(1);
        }
        c.setChanged();

        String clean = cleanName(name);
        ItemStack rocket = RocketFactory.rocket(clean, flight, bursts, 1.0f, 1);
        give(player, rocket);
        menu.broadcastChanges();
        Msg.send(player, "workbench.crafted", "rocket", clean.isEmpty() ? "Firework Rocket" : clean, "bursts", bursts.size());
    }

    /** Server: make a rocket preset. Costs paper, the flight's gunpowder and extra gunpowder per burst. */
    public static void craftPreset(ServerPlayer player, WorkbenchMenu menu, String id) {
        if (!Features.on(Feature.ROCKET_PRESETS)) {
            Msg.send(player, "give.presets_off");
            return;
        }
        if (!Perm.has(player, Perm.DESIGN)) {
            Msg.send(player, "general.no_permission");
            return;
        }
        RocketPreset preset = RocketPresets.get(id);
        if (preset == null) {
            Msg.send(player, "give.unknown", "id", id);
            return;
        }
        String off = RocketFactory.disabledShape(preset);
        if (off != null) {
            Msg.send(player, "give.shape_off", "rocket", preset.name(), "shape", off);
            return;
        }
        Container c = menu.container();
        int gunpowder = presetCost(preset, Limits.PRESET_GUNPOWDER.get());
        if (!c.getItem(WorkbenchMenu.PAPER).is(Items.PAPER)) {
            Msg.send(player, "workbench.need_paper");
            return;
        }
        if (!c.getItem(WorkbenchMenu.GUNPOWDER).is(Items.GUNPOWDER) || c.getItem(WorkbenchMenu.GUNPOWDER).getCount() < gunpowder) {
            Msg.send(player, "workbench.need_gunpowder", "amount", gunpowder);
            return;
        }
        c.getItem(WorkbenchMenu.PAPER).shrink(1);
        c.getItem(WorkbenchMenu.GUNPOWDER).shrink(gunpowder);
        c.setChanged();
        give(player, RocketFactory.rocket(preset, 1));
        menu.broadcastChanges();
        Msg.send(player, "workbench.crafted", "rocket", preset.name(), "bursts", preset.bursts().size());
    }

    /** Gunpowder a preset costs in the workbench. */
    public static int presetCost(RocketPreset preset, int perBurst) {
        return Math.min(64, preset.flight() + preset.bursts().size() * perBurst);
    }

    /** Name limited to 32 characters, without color codes. */
    public static String cleanName(String name) {
        String n = name == null ? "" : name.replace('§', ' ').trim();
        return n.length() > MAX_NAME ? n.substring(0, MAX_NAME) : n;
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
}
