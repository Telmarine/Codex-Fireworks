package com.tac5studios.codexfireworks.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import java.util.List;

/**
 * Server limits: config/codex_fireworks/limits.toml
 * They keep shows readable and stop one player from flooding everyone's screen with particles.
 */
public final class Limits {

    public static final ModConfigSpec SPEC;

    // [display]
    public static final DoubleValue BURST_SCALE;
    public static final DoubleValue SPARK_SCALE;

    // [shells]
    public static final IntValue MAX_BURSTS;
    public static final IntValue MAX_DELAY;
    public static final IntValue MAX_OFFSET;

    // [workbench]
    public static final IntValue PRESET_GUNPOWDER;

    // [patterns]
    public static final IntValue MAX_PATTERN_SPARKS;

    // [mystery]
    public static final ConfigValue<List<? extends String>> MYSTERY_POOL;

    // [shows]
    public static final IntValue MAX_LAUNCHERS;
    public static final IntValue MAX_CUES;
    public static final IntValue MIN_INTERVAL;
    public static final IntValue MAX_SHOWS_PER_AREA;
    public static final IntValue LINK_RANGE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Codex: Fireworks limits. Changes are picked up while the server runs.");

        b.comment("Size of every burst on every player's screen. Sent to players when they join and on change.",
                "Each rocket file can add its own \"scale\" on top of these.").push("display");
        BURST_SCALE = b.comment("How far the sparks spread, and the spacing between bursts. 1.0 = vanilla firework size.")
                .defineInRange("burst_scale", 1.6, 0.25, 4.0);
        SPARK_SCALE = b.comment("Size of each spark. 1.0 = vanilla firework spark.")
                .defineInRange("spark_scale", 1.5, 0.25, 4.0);
        b.pop();

        b.push("shells");
        MAX_BURSTS = b.comment("Most bursts one rocket can hold.").defineInRange("max_bursts", 8, 1, 8);
        MAX_DELAY = b.comment("Longest delay of one burst after the rocket pops, in ticks (20 = 1 second).")
                .defineInRange("max_delay_ticks", 60, 0, 200);
        MAX_OFFSET = b.comment("Furthest a burst can sit from the rocket, in blocks.")
                .defineInRange("max_offset_blocks", 12, 0, 32);
        b.pop();

        b.push("workbench");
        PRESET_GUNPOWDER = b.comment("Extra gunpowder per burst when a rocket preset is made in the Firework Workbench.",
                        "Making a preset always takes 1 paper and 1-3 gunpowder for the flight, plus this per burst.")
                .defineInRange("preset_gunpowder_per_burst", 1, 0, 8);
        b.pop();

        b.push("patterns");
        MAX_PATTERN_SPARKS = b.comment("Most lit pixels (sparks) in one drawn pattern. The grid is 16x16 = 256.")
                .defineInRange("max_sparks", 256, 16, 256);
        b.pop();

        b.push("mystery");
        MYSTERY_POOL = b.comment("Rocket presets a Mystery Rocket can turn into, e.g. [\"golden_willow\", \"grand_finale\"].",
                        "Empty = every rocket preset that's loaded.")
                .defineListAllowEmpty("pool", List.of(), () -> "", o -> o instanceof String);
        b.pop();

        b.push("shows");
        MAX_LAUNCHERS = b.comment("Most launchers linked to one Show Controller.").defineInRange("max_launchers", 32, 1, 256);
        MAX_CUES = b.comment("Most cues in one show.").defineInRange("max_cues", 200, 1, 2000);
        MIN_INTERVAL = b.comment("Shortest time between two launches from one launcher, in ticks.")
                .defineInRange("min_interval_ticks", 5, 1, 200);
        MAX_SHOWS_PER_AREA = b.comment("Most shows running at once within 128 blocks of each other.")
                .defineInRange("max_shows_per_area", 2, 1, 16);
        LINK_RANGE = b.comment("Furthest a launcher can be from its Show Controller, in blocks.")
                .defineInRange("link_range", 48, 4, 128);
        b.pop();

        SPEC = b.build();
    }

    private Limits() {}

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC, "codex_fireworks/limits.toml");
    }
}
