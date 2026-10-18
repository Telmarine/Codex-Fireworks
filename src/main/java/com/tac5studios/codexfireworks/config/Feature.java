package com.tac5studios.codexfireworks.config;

import java.util.List;

/**
 * Every feature, with its switch in features.toml and the mods that already do the same job.
 * When one of those mods is installed, the overlap guard turns the feature off
 * (overlap_guard.force_on keeps it on). The mod lists can be changed in features.toml.
 */
public enum Feature {

    // [shapes] one switch per built-in burst shape
    PEONY("shapes", "peony", true, "Round burst of evenly spread sparks."),
    CHRYSANTHEMUM("shapes", "chrysanthemum", true, "Round burst where every spark leaves a trail."),
    WILLOW("shapes", "willow", true, "Slow sparks with long trails that droop down."),
    PALM("shapes", "palm", true, "A few thick arms that rise and curve out."),
    RING("shapes", "ring", true, "One flat ring of sparks."),
    DOUBLE_RING("shapes", "double_ring", true, "Two rings crossing each other."),
    HEART("shapes", "heart", true, "A heart that faces the player watching."),
    SPIRAL("shapes", "spiral", true, "Arms that curl outward in a spiral."),
    CROSSETTE("shapes", "crossette", true, "A few sparks that each split into four."),
    CRACKLE("shapes", "crackle", true, "Sparks that end in small popping flashes."),
    STROBE("shapes", "strobe", true, "Slow sparks that blink on and off."),
    COMET("shapes", "comet", true, "One bright spark with a long tail."),
    FISH("shapes", "fish", true, "Sparks that wiggle as they fly."),
    BROCADE("shapes", "brocade", true, "Dense glittering burst with fine trails."),

    // [patterns]
    PATTERNS("patterns", "patterns", true, "Player-drawn 16x16 bursts saved on Pattern Cards."),
    TEXT_MODE("patterns", "text_mode", true, "Type up to 12 characters to fill a pattern."),
    HEX_FOR_EVERYONE("patterns", "hex_for_everyone", false,
            "Let every player use custom hex colors. When off, only fireworks.pattern.hex can."),

    // [shells]
    SHELLS("shells", "shells", true, "Rockets with up to 8 bursts, built in the Firework Workbench."),
    ROCKET_PRESETS("shells", "rocket_presets", true, "Ready-made rockets (/fireworks give rocket, workbench presets)."),
    PATTERN_PRESETS("shells", "pattern_presets", true, "Ready-made patterns (/fireworks give pattern)."),
    MYSTERY_ROCKET("shells", "mystery_rocket", true, "A rocket that fires a random preset each launch."),
    RARITY("shells", "rarity", true, "Common / Rare / Epic tag on rockets."),
    SHAPE_SOUNDS("shells", "shape_sounds", true, "Each shape plays its own explosion sound."),

    // [shows]
    LAUNCHERS("shows", "launchers", true, "Launcher block that fires rockets on redstone or a show cue."),
    SHOWS("shows", "shows", true, "Show Controller, Show Cards and /fireworks show."),

    // [rules]
    PLAYER_DAMAGE("rules", "player_damage", true,
            "Firework explosions hurt players (vanilla). Turn off to make fireworks harmless to players."),
    CLAIMS("rules", "claims", true,
            "Respect claim mods (Elements: Claims, Open Parties and Claims, FTB Chunks) for launchers and controllers.");

    public final String section;
    public final String key;
    public final boolean def;
    public final String comment;
    /** Mod ids that already do this job. */
    public final List<String> guardMods;

    Feature(String section, String key, boolean def, String comment, String... guardMods) {
        this.section = section;
        this.key = key;
        this.def = def;
        this.comment = comment;
        this.guardMods = List.of(guardMods);
    }

    public static Feature byKey(String key) {
        for (Feature f : values()) if (f.key.equals(key)) return f;
        return null;
    }
}
