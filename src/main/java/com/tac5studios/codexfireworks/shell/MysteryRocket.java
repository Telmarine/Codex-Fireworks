package com.tac5studios.codexfireworks.shell;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.mixin.FireworkRocketEntityAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * When a Mystery Rocket is launched (by hand, dispenser, crossbow or launcher), the flying rocket
 * is swapped for a random rocket preset. The pool is limits.toml [mystery] pool (empty = all presets).
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class MysteryRocket {

    private MysteryRocket() {}

    @SubscribeEvent
    @SuppressWarnings("unused") // called by the event bus
    public static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide() || e.loadedFromDisk()) return;
        if (!(e.getEntity() instanceof FireworkRocketEntity rocket)) return;
        ItemStack item = rocket.getItem();
        if (!RocketFactory.isMystery(item) || !Features.on(Feature.MYSTERY_ROCKET)) return;

        RocketPreset preset = pick(rocket.getRandom());
        if (preset == null) return; // nothing to pick: it pops with its own vanilla stars
        rocket.getEntityData().set(FireworkRocketEntityAccessor.codex_fireworks$itemData(), RocketFactory.rocket(preset, 1));
        RandomSource r = rocket.getRandom();
        ((FireworkRocketEntityAccessor) rocket).codex_fireworks$setLifetime(10 * (1 + preset.flight()) + r.nextInt(6) + r.nextInt(7));
    }

    private static RocketPreset pick(RandomSource random) {
        List<? extends String> pool = Limits.MYSTERY_POOL.get();
        List<RocketPreset> options = new ArrayList<>();
        if (pool.isEmpty()) {
            options.addAll(RocketPresets.all().values());
        } else {
            for (String id : pool) {
                RocketPreset p = RocketPresets.get(id);
                if (p != null) options.add(p);
            }
        }
        options.removeIf(p -> RocketFactory.disabledShape(p) != null);
        return options.isEmpty() ? null : options.get(random.nextInt(options.size()));
    }
}
