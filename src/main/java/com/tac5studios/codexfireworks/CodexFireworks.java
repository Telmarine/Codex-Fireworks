package com.tac5studios.codexfireworks;

import com.tac5studios.codexfireworks.config.ClientConfig;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.registry.ModBlockEntities;
import com.tac5studios.codexfireworks.registry.ModBlocks;
import com.tac5studios.codexfireworks.registry.ModComponents;
import com.tac5studios.codexfireworks.registry.ModItems;
import com.tac5studios.codexfireworks.registry.ModMenus;
import com.tac5studios.codexfireworks.registry.ModRecipes;
import com.tac5studios.codexfireworks.registry.ModSounds;
import com.tac5studios.codexfireworks.registry.ModTab;
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
 *   shell.MysteryRocket    swap a launched Mystery Rocket for a random preset
 *   pattern.PatternPresets load the pattern presets (datapacks + config/codex_fireworks/patterns)
 *   show.ShowRunner        play the running shows
 *   network.Network        all packets
 *   commands.Commands      /fireworks
 *   rules.FireworkDamage   firework damage switch
 *   client.*               particles, screens, tooltips, drawing the bursts
 *
 * <p>Registered here: blocks (Firework Workbench, Launcher, Show Controller), items (Pattern Card, Show Card),
 * block entities, menus, item data components, shape sounds, the Pattern Card copy recipe and the creative tab.
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
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenus.register(modBus);
        ModComponents.register(modBus);
        ModSounds.register(modBus);
        ModRecipes.register(modBus);
        ModTab.register(modBus);
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
