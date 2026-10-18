package com.tac5studios.codexfireworks.config;

import com.tac5studios.codexfireworks.core.Mods;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Master control file: config/codex_fireworks/features.toml
 * One master switch, then one switch per feature. A feature that's off does nothing.
 * Switches can be changed while the server runs; /fireworks reload re-checks the overlap guard
 * and sends the new switches to every player.
 */
public final class Features {

    public static final ModConfigSpec SPEC;

    private static final Map<Feature, BooleanValue> SWITCHES = new EnumMap<>(Feature.class);

    public static final BooleanValue ENABLED;
    public static final BooleanValue OVERLAP_GUARD;
    public static final ConfigValue<List<? extends String>> OVERLAP_FORCE_ON;
    public static final ConfigValue<List<? extends String>> OVERLAP_MODS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment(
                "Codex: Fireworks master switches.",
                "Every feature has its own switch. Numbers and limits are in limits.toml."
        );

        b.push("general");
        ENABLED = b.comment("Master switch. When off, rockets from this mod pop with their plain vanilla stars.")
                .define("enabled", true);
        b.pop();

        String section = null;
        for (Feature f : Feature.values()) {
            if (!f.section.equals(section)) {
                if (section != null) b.pop();
                section = f.section;
                b.push(section);
            }
            SWITCHES.put(f, b.comment(f.comment).define(f.key, f.def));
        }
        if (section != null) b.pop();

        b.comment("Turns a feature off when another installed mod already does the same job.",
                "Mods are checked at server start and on /fireworks reload.").push("overlap_guard");
        OVERLAP_GUARD = b.comment("Turn the overlap guard on or off.").define("enabled", true);
        OVERLAP_FORCE_ON = b.comment("Features to keep on even when an overlapping mod is installed, e.g. [\"launchers\"].")
                .defineListAllowEmpty("force_on", List.of(), () -> "", o -> o instanceof String);
        OVERLAP_MODS = b.comment("Which mods turn which feature off, as \"feature=modid\".",
                        "Empty by default: no other mod changes vanilla rockets the way this one does.",
                        "Add a line when you find a mod that does the same job.")
                .defineListAllowEmpty("mods", defaultGuards(), () -> "", o -> o instanceof String s && s.contains("="));
        b.pop();

        SPEC = b.build();
    }

    private Features() {}

    private static List<String> defaultGuards() {
        List<String> out = new ArrayList<>();
        for (Feature f : Feature.values()) for (String mod : f.guardMods) out.add(f.key + "=" + mod);
        return out;
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC, "codex_fireworks/features.toml");
    }

    /** True when the master switch is on. */
    public static boolean enabled() {
        return ENABLED.get();
    }

    /** The switch alone, ignoring the master switch and the overlap guard. */
    public static boolean switchedOn(Feature f) {
        return SWITCHES.get(f).get();
    }

    /** True when the mod is on, the feature is switched on and the overlap guard hasn't turned it off. */
    public static boolean on(Feature f) {
        return enabled() && switchedOn(f) && !Mods.guarded(f);
    }

    public static boolean forcedOn(Feature f) {
        return OVERLAP_FORCE_ON.get().contains(f.key);
    }
}
