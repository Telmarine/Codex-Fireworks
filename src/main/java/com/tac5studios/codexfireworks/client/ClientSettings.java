package com.tac5studios.codexfireworks.client;

import com.tac5studios.codexfireworks.network.SettingsPayload;

import java.util.Set;

/**
 * The server's drawing switches as this player last received them.
 * Plain values only, so this class is safe to load on a dedicated server.
 */
public final class ClientSettings {

    /** Used until the server sends its own values (same as the limits.toml defaults). */
    public static final float DEFAULT_BURST_SCALE = 1.6f, DEFAULT_SPARK_SCALE = 1.5f;

    private static volatile boolean enabled = true;
    private static volatile Set<String> disabledShapes = Set.of();
    private static volatile boolean sounds = true;
    private static volatile float burstScale = DEFAULT_BURST_SCALE;
    private static volatile float sparkScale = DEFAULT_SPARK_SCALE;

    private ClientSettings() {}

    public static void apply(SettingsPayload p) {
        enabled = p.enabled();
        disabledShapes = Set.copyOf(p.disabledShapes());
        sounds = p.sounds();
        burstScale = p.burstScale();
        sparkScale = p.sparkScale();
    }

    /** Back to defaults when leaving a server. */
    public static void reset() {
        enabled = true;
        disabledShapes = Set.of();
        sounds = true;
        burstScale = DEFAULT_BURST_SCALE;
        sparkScale = DEFAULT_SPARK_SCALE;
    }

    public static boolean enabled() {
        return enabled;
    }

    public static boolean shapeOff(String id) {
        return disabledShapes.contains(id);
    }

    public static boolean sounds() {
        return sounds;
    }

    public static float burstScale() {
        return burstScale;
    }

    public static float sparkScale() {
        return sparkScale;
    }
}
