package com.tac5studios.codexfireworks.rules;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * features.toml [rules] player_damage = false makes firework explosions harmless to players.
 * Covers every firework rocket, not only the ones made by this mod. Mobs still take damage.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class FireworkDamage {

    private FireworkDamage() {}

    @SubscribeEvent
    @SuppressWarnings("unused") // called by the event bus
    public static void onDamage(LivingIncomingDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!e.getSource().is(DamageTypes.FIREWORKS)) return;
        if (!Features.enabled() || Features.switchedOn(Feature.PLAYER_DAMAGE)) return;
        e.setCanceled(true);
    }
}
