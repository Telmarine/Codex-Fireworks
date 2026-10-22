package com.tac5studios.codexfireworks.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/**
 * Player-side settings: config/codex_fireworks/client.toml
 * Lets players on low-end PCs turn the particle count down. Only read on the client.
 */
public final class ClientConfig {

    public enum Quality {
        FULL(1.0f), REDUCED(0.5f), MINIMAL(0.25f);

        public final float factor;

        Quality(float factor) {
            this.factor = factor;
        }
    }

    public static final ModConfigSpec SPEC;

    public static final EnumValue<Quality> QUALITY;
    public static final IntValue MAX_SPARKS_PER_BURST;
    public static final IntValue MAX_SPARKS_TOTAL;
    public static final DoubleValue VOLUME;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Codex: Fireworks client settings. Only affect your own screen.");
        QUALITY = b.comment("Particle quality: FULL, REDUCED (half the sparks) or MINIMAL (a quarter).")
                .defineEnum("quality", Quality.FULL);
        MAX_SPARKS_PER_BURST = b.comment("Most sparks drawn for one burst, after quality is applied.")
                .defineInRange("max_sparks_per_burst", 400, 16, 1000);
        MAX_SPARKS_TOTAL = b.comment("Most sparks from this mod on screen at once. New sparks are skipped above this,",
                        "so a big show can't reach the game's particle limit (16384).")
                .defineInRange("max_sparks_total", 12000, 500, 16000);
        VOLUME = b.comment("Volume of firework sounds from this mod, 0.0 to 1.0.")
                .defineInRange("volume", 1.0, 0.0, 1.0);
        SPEC = b.build();
    }

    private ClientConfig() {}

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC, "codex_fireworks/client.toml");
    }
}
