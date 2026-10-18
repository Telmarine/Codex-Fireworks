package com.tac5studios.codexfireworks.client.shapes;

import com.tac5studios.codexfireworks.client.ClientSettings;
import com.tac5studios.codexfireworks.client.CodexSpark;
import com.tac5studios.codexfireworks.config.ClientConfig;
import com.tac5studios.codexfireworks.shell.Burst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

/**
 * Everything a shape needs to draw one burst: where it is, its colors, the spark budget
 * after the player's quality setting, and the camera directions for shapes that face the viewer.
 */
public final class BurstContext {

    public final ClientLevel level;
    public final Burst burst;
    public final double x, y, z;
    public final RandomSource random;
    /** Burst size used for spread: the burst's own size x the server's burst_scale x the rocket's scale. */
    public final float size;
    /** Spark size multiplier: the server's spark_scale x the rocket's scale. */
    public final float sparkScale;
    private final float quality;
    private final int maxSparks;
    private int spawned;

    /** Directions of the player's view: right and up (for hearts, rings and spirals that face the viewer). */
    public final Vec3 right, up;

    public BurstContext(ClientLevel level, Burst burst, double x, double y, double z, float rocketScale) {
        this.level = level;
        this.burst = burst;
        this.x = x;
        this.y = y;
        this.z = z;
        this.random = level.getRandom();
        this.size = burst.size() * ClientSettings.burstScale() * rocketScale;
        this.sparkScale = ClientSettings.sparkScale() * rocketScale;
        this.quality = ClientConfig.QUALITY.get().factor;
        this.maxSparks = ClientConfig.MAX_SPARKS_PER_BURST.get();

        var cam = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f left = cam.getLeftVector();
        Vector3f camUp = cam.getUpVector();
        this.right = new Vec3(-left.x(), -left.y(), -left.z()).normalize();
        this.up = new Vec3(camUp.x(), camUp.y(), camUp.z()).normalize();
    }

    /** How many sparks to draw for a shape that would use {@code full} at full quality and size 1. */
    public int count(int full) {
        int n = Math.round(full * quality * Math.max(0.5f, burst.size()) * 1.25f);
        return Math.max(6, Math.min(n, maxSparks));
    }

    /** Trail spacing: every 2 ticks at full quality, sparser when turned down. */
    public int trailEvery() {
        return quality >= 1f ? 2 : quality >= 0.5f ? 3 : 5;
    }

    /** One of the burst's colors, picked at random. */
    public int color() {
        List<Integer> c = burst.colors();
        return c.isEmpty() ? 0xFFFFFF : c.get(random.nextInt(c.size()));
    }

    /** The color at a fixed index (wraps), for shapes that split their colors. */
    public int color(int i) {
        List<Integer> c = burst.colors();
        return c.isEmpty() ? 0xFFFFFF : c.get(Math.floorMod(i, c.size()));
    }

    /** A fade color, or -1 when the burst has none. */
    public int fade() {
        List<Integer> f = burst.fades();
        return f.isEmpty() ? -1 : f.get(random.nextInt(f.size()));
    }

    /** True while there's room in this burst's spark budget. */
    public boolean room() {
        return spawned < maxSparks;
    }

    /**
     * A spark at the burst center moving with this velocity, with the burst's color, fade,
     * trail and twinkle already set. Null when the budget is used up.
     */
    public CodexSpark spark(double vx, double vy, double vz) {
        return spark(vx, vy, vz, color());
    }

    public CodexSpark spark(double vx, double vy, double vz, int rgb) {
        if (!room()) return null;
        CodexSpark s = CodexSpark.spawn(level, x, y, z, vx, vy, vz, rgb);
        if (s == null) return null;
        spawned++;
        s.scaleSparks(sparkScale);
        s.fade(fade()).twinkle(burst.twinkle()).trail(burst.trail(), trailEvery(), 0.5f);
        return s;
    }

    /** Small random number in [-amount, amount]. */
    public double jitter(double amount) {
        return (random.nextDouble() * 2 - 1) * amount;
    }

    /** Life in ticks between min and max. */
    public int life(int min, int max) {
        return min + random.nextInt(Math.max(1, max - min + 1));
    }
}
