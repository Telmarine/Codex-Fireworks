package com.tac5studios.codexfireworks;

import com.tac5studios.codexfireworks.config.ClientConfig;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Codex: Fireworks — players design their own firework rockets.
 * Client + server: the new bursts are drawn by the client, so players need the mod too.
 *
 * <p>Everything else starts from events (each class subscribes itself):
 *   core.Mods              check installed mods and set the overlap guard
 *   perms.Perm             register permission nodes
 *   messages.Msg           read messages.toml
 *   shell.RocketPresets    load the rocket presets from datapacks
 *   network.Network        send the server's switches to each player
 *   commands.Commands      /fireworks
 *   rules.FireworkDamage   firework damage switch
 *   client.*               particles, tooltips, drawing the bursts
 */
@Mod(CodexFireworks.MOD_ID)
public class CodexFireworks {

    public static final String MOD_ID = "codex_fireworks";
    public static final Logger LOGGER = LoggerFactory.getLogger("CodexFireworks");

    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MOD_ID);

    /** The spark every burst is drawn with (uses the vanilla spark textures). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPARK =
            PARTICLES.register("spark", () -> new SimpleParticleType(true));

    public CodexFireworks(IEventBus modBus, ModContainer container) {
        PARTICLES.register(modBus);
        // config/codex_fireworks/features.toml, limits.toml, client.toml
        Features.register(container);
        Limits.register(container);
        ClientConfig.register(container);
        LOGGER.info("[Fireworks] Codex: Fireworks loading.");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
