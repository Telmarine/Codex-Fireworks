package com.tac5studios.codexfireworks.client.shapes;

import com.tac5studios.codexfireworks.client.CodexSpark;
import com.tac5studios.codexfireworks.shell.Shape;
import net.minecraft.world.phys.Vec3;

/**
 * Drawing code for the 14 built-in shapes (drawn patterns are in PatternShape). Every shape works the same way: sparks start at the
 * burst center with a velocity, and drag slows them down so they settle into the shape.
 * The distance a spark travels is roughly speed / (1 - drag).
 */
public final class BuiltInShapes {

    private static final double GOLDEN = Math.PI * (3 - Math.sqrt(5)); // golden angle, spreads points evenly

    private BuiltInShapes() {}

    public static void draw(Shape shape, BurstContext c) {
        switch (shape) {
            case PEONY -> peony(c);
            case CHRYSANTHEMUM -> chrysanthemum(c);
            case WILLOW -> willow(c);
            case PALM -> palm(c);
            case RING -> ring(c, randomTilt(c), 0);
            case DOUBLE_RING -> doubleRing(c);
            case HEART -> heart(c);
            case SPIRAL -> spiral(c);
            case CROSSETTE -> crossette(c);
            case CRACKLE -> crackle(c);
            case STROBE -> strobe(c);
            case COMET -> comet(c);
            case FISH -> fish(c);
            case BROCADE -> brocade(c);
            case PATTERN -> PatternShape.draw(c);
        }
    }

    // ---- helpers ----

    /** Point i of n spread evenly over a sphere (unit length). */
    private static Vec3 spherePoint(int i, int n) {
        double y = 1 - (i + 0.5) * 2.0 / n;
        double r = Math.sqrt(Math.max(0, 1 - y * y));
        double a = i * GOLDEN;
        return new Vec3(Math.cos(a) * r, y, Math.sin(a) * r);
    }

    /** Sphere of sparks; returns nothing, each spark gets {@code tune} applied. */
    private static void sphere(BurstContext c, int full, double speed, Tune tune) {
        int n = c.count(full);
        for (int i = 0; i < n; i++) {
            Vec3 d = spherePoint(i, n);
            double v = speed * c.size * (0.94 + c.random.nextDouble() * 0.12);
            CodexSpark s = c.spark(d.x * v, d.y * v, d.z * v);
            if (s == null) return;
            tune.apply(s);
        }
    }

    private interface Tune {
        void apply(CodexSpark s);
    }

    /** A plane that mostly faces the viewer, tilted a little at random. Returns {axisA, axisB}. */
    private static Vec3[] randomTilt(BurstContext c) {
        Vec3 a = c.right.add(c.up.scale(c.jitter(0.4))).normalize();
        Vec3 normal = a.cross(c.up).normalize();
        Vec3 b = c.up.add(normal.scale(c.jitter(0.5))).normalize();
        return new Vec3[]{a, b};
    }

    // ---- shapes ----

    /** Round burst of evenly spread sparks. */
    private static void peony(BurstContext c) {
        sphere(c, 90, 0.45, s -> s.life(c.life(40, 52)));
    }

    /** Round burst where every spark leaves a trail. */
    private static void chrysanthemum(BurstContext c) {
        sphere(c, 80, 0.45, s -> s.life(c.life(45, 58)).trail(true, c.trailEvery(), 0.65f));
    }

    /** Slow, heavy sparks with long trails that droop down. */
    private static void willow(BurstContext c) {
        sphere(c, 70, 0.32, s -> s.life(c.life(80, 100)).drag(0.95f).fall(0.55f)
                .trail(true, c.trailEvery(), 0.8f).twinkle(true));
    }

    /** A few thick arms that rise and curve out under gravity. */
    private static void palm(BurstContext c) {
        int arms = 7;
        int per = Math.max(3, c.count(70) / arms);
        for (int a = 0; a < arms; a++) {
            // upper half only, spread evenly
            Vec3 dir = spherePoint(a, arms * 2);
            if (dir.y < 0.15) dir = new Vec3(dir.x, 0.15 + Math.abs(dir.y), dir.z).normalize();
            int armColor = c.color(a);
            for (int k = 0; k < per; k++) {
                double v = (0.18 + 0.38 * k / (double) per) * c.size;
                CodexSpark s = c.spark(dir.x * v + c.jitter(0.015), dir.y * v + c.jitter(0.015), dir.z * v + c.jitter(0.015), armColor);
                if (s == null) return;
                s.life(c.life(55, 70)).fall(0.45f).size(0.12f);
                if (k == per - 1) s.trail(true, 1, 0.7f);
            }
        }
    }

    /** Flat ring in the plane (a, b). */
    private static void ring(BurstContext c, Vec3[] plane, int colorIndex) {
        int n = c.count(56);
        double v = 0.45 * c.size;
        for (int i = 0; i < n; i++) {
            double t = i * Math.PI * 2 / n;
            Vec3 d = plane[0].scale(Math.cos(t)).add(plane[1].scale(Math.sin(t)));
            CodexSpark s = c.spark(d.x * v, d.y * v, d.z * v, colorIndex < 0 ? c.color() : c.color(colorIndex));
            if (s == null) return;
            s.life(c.life(38, 48)).fall(0.02f);
        }
    }

    /** Two rings crossing at a right angle, one in each of the first two colors. */
    private static void doubleRing(BurstContext c) {
        Vec3[] p = randomTilt(c);
        Vec3 normal = p[0].cross(p[1]).normalize();
        ring(c, p, 0);
        ring(c, new Vec3[]{p[0], normal}, 1);
    }

    /** A heart in the plane of the screen, so it reads the right way up for whoever watches. */
    private static void heart(BurstContext c) {
        int n = c.count(64);
        double k = 0.034 * c.size;
        for (int i = 0; i < n; i++) {
            double t = i * Math.PI * 2 / n;
            double hx = 16 * Math.pow(Math.sin(t), 3);
            double hy = 13 * Math.cos(t) - 5 * Math.cos(2 * t) - 2 * Math.cos(3 * t) - Math.cos(4 * t);
            Vec3 d = c.right.scale(hx * k).add(c.up.scale(hy * k));
            CodexSpark s = c.spark(d.x, d.y, d.z);
            if (s == null) return;
            s.life(c.life(40, 50)).drag(0.88f).fall(0.01f);
        }
    }

    /** Three arms curling outward, facing the viewer. */
    private static void spiral(BurstContext c) {
        int arms = 3;
        int per = Math.max(6, c.count(54) / arms);
        for (int a = 0; a < arms; a++) {
            for (int k = 0; k < per; k++) {
                double angle = a * Math.PI * 2 / arms + k * 0.22;
                double v = (0.08 + 0.42 * k / per) * c.size;
                Vec3 d = c.right.scale(Math.cos(angle)).add(c.up.scale(Math.sin(angle)));
                CodexSpark s = c.spark(d.x * v, d.y * v, d.z * v, c.color(a));
                if (s == null) return;
                s.life(c.life(40, 50)).drag(0.89f).fall(0.02f);
            }
        }
    }

    /** A few fast sparks that each split into four after a short flight. */
    private static void crossette(BurstContext c) {
        int mains = Math.max(4, Math.min(6, c.count(6)));
        for (int i = 0; i < mains; i++) {
            double a = i * Math.PI * 2 / mains + c.jitter(0.2);
            double v = 0.5 * c.size;
            CodexSpark s = c.spark(Math.cos(a) * v, 0.12 + c.jitter(0.05), Math.sin(a) * v);
            if (s == null) return;
            int color = s.rgb();
            s.life(40).trail(true, 1, 1f).splitAt(14, parent -> {
                for (int q = 0; q < 4; q++) {
                    double b = q * Math.PI / 2 + Math.PI / 4;
                    double cv = 0.24 * c.size;
                    CodexSpark child = CodexSpark.spawn(parent.level(), parent.px(), parent.py(), parent.pz(),
                            Math.cos(b) * cv, (q % 2 == 0 ? 1 : -1) * cv * 0.6, Math.sin(b) * cv, color);
                    if (child != null) child.scaleSparks(c.sparkScale).life(c.life(26, 34)).fade(c.fade()).trail(true, c.trailEvery(), 0.6f);
                }
            });
        }
    }

    /** Short-lived sparks that end in small popping flashes. */
    private static void crackle(BurstContext c) {
        sphere(c, 60, 0.4, s -> s.life(c.life(22, 32)).crackle(true));
    }

    /** Slow sparks that blink on and off for a long time. */
    private static void strobe(BurstContext c) {
        sphere(c, 40, 0.28, s -> s.life(c.life(70, 90)).drag(0.93f).fall(0.15f).strobe(true).size(0.13f));
    }

    /** One bright spark with a long tail, plus two smaller ones. */
    private static void comet(BurstContext c) {
        for (int i = 0; i < 3; i++) {
            Vec3 d = new Vec3(c.jitter(0.5), 0.6 + c.random.nextDouble() * 0.4, c.jitter(0.5)).normalize();
            double v = (i == 0 ? 0.6 : 0.42) * c.size;
            CodexSpark s = c.spark(d.x * v, d.y * v, d.z * v);
            if (s == null) return;
            s.life(c.life(45, 55)).size(i == 0 ? 0.22f : 0.14f).drag(0.94f).fall(0.3f).trail(true, 1, 0.9f);
        }
    }

    /** Sparks that wiggle as they fly, like a school of small fish. */
    private static void fish(BurstContext c) {
        sphere(c, 40, 0.36, s -> s.life(c.life(50, 62)).drag(0.93f).wiggle(0.05f).trail(true, c.trailEvery() + 1, 0.4f));
    }

    /** Dense glittering burst with fine trails that sink slowly. */
    private static void brocade(BurstContext c) {
        sphere(c, 110, 0.42, s -> s.life(c.life(60, 75)).fall(0.3f).twinkle(true)
                .trail(true, c.trailEvery(), 0.7f).size(0.08f));
    }
}
