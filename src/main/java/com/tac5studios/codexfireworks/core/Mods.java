package com.tac5studios.codexfireworks.core;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Looks at which mods are loaded, only at server start and on /fireworks reload,
 * and decides which features the overlap guard turns off. Nothing is looked up while the server runs.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class Mods {

    /** Features turned off by the overlap guard. */
    private static volatile Set<Feature> guarded = Collections.emptySet();
    /** Which installed mods caused each guard (for the log). */
    private static volatile Map<Feature, List<String>> found = Collections.emptyMap();

    private static volatile boolean elementsClaims, opac, ftbChunks;

    private Mods() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused") // the event bus needs the parameter
    public static void onAboutToStart(ServerAboutToStartEvent e) {
        detect();
        log();
    }

    /** Re-check the loaded mods against features.toml. */
    public static synchronized void detect() {
        ModList mods = ModList.get();
        elementsClaims = mods.isLoaded("elements_claims");
        opac = mods.isLoaded("openpartiesandclaims");
        ftbChunks = mods.isLoaded("ftbchunks");

        Map<Feature, List<String>> hits = new EnumMap<>(Feature.class);
        for (String line : Features.OVERLAP_MODS.get()) {
            int eq = line.indexOf('=');
            if (eq <= 0) continue;
            Feature f = Feature.byKey(line.substring(0, eq).trim());
            String mod = line.substring(eq + 1).trim();
            if (f == null) {
                CodexFireworks.LOGGER.warn("[Fireworks] overlap_guard.mods: unknown feature in \"{}\".", line);
                continue;
            }
            if (!mod.isEmpty() && mods.isLoaded(mod)) hits.computeIfAbsent(f, k -> new ArrayList<>()).add(name(mod));
        }

        Set<Feature> off = EnumSet.noneOf(Feature.class);
        if (Features.OVERLAP_GUARD.get()) {
            for (Feature f : hits.keySet()) if (!Features.forcedOn(f)) off.add(f);
        }
        found = hits;
        guarded = off;
    }

    /** Write the overlap guard results to the server log. */
    public static void log() {
        found.forEach((f, names) -> {
            if (guarded.contains(f)) {
                CodexFireworks.LOGGER.info("[Fireworks] {} found: {} is turned off. Add \"{}\" to overlap_guard.force_on to keep it.",
                        String.join(", ", names), f.key, f.key);
            } else if (Features.OVERLAP_GUARD.get()) {
                CodexFireworks.LOGGER.info("[Fireworks] {} found: {} is kept on (overlap_guard.force_on).",
                        String.join(", ", names), f.key);
            }
        });
    }

    private static String name(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(c -> c.getModInfo().getDisplayName())
                .orElse(modId);
    }

    public static boolean guarded(Feature f) {
        return guarded.contains(f);
    }

    public static int guardedCount() {
        return guarded.size();
    }

    public static boolean elementsClaims() {
        return elementsClaims;
    }

    public static boolean opac() {
        return opac;
    }

    public static boolean ftbChunks() {
        return ftbChunks;
    }
}
