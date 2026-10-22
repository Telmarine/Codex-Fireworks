package com.tac5studios.codexfireworks.client.shapes;

import com.tac5studios.codexfireworks.client.CodexSpark;
import com.tac5studios.codexfireworks.pattern.Pattern;
import com.tac5studios.codexfireworks.shell.Shape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws a Pattern Card picture. Every lit pixel is one spark. The sparks fly out from the
 * burst center to their place in the picture, hang there, then sag and fade.
 * A small invisible helper particle steers every spark each tick, so the picture stays sharp
 * and can face the viewer, face a fixed way, or spin.
 */
public final class PatternShape {

    private static final int GROW = 10, HOLD = 46, END = 64;

    private PatternShape() {}

    public static void draw(BurstContext c) {
        Pattern p = c.burst.pattern();
        if (p == null || p.isEmpty()) {
            BuiltInShapes.draw(Shape.PEONY, c);
            return;
        }
        double spacing = p.spacing() * c.size;
        double cx = (p.width() - 1) / 2.0, cy = (p.height() - 1) / 2.0;
        int fade = p.fade() >= 0 ? p.fade() : c.fade();

        // Low quality settings draw every 2nd or 4th pixel.
        int step = c.count(100) >= 100 ? 1 : c.count(100) >= 50 ? 2 : 4;
        List<Dot> dots = new ArrayList<>();
        int n = 0;
        for (int y = 0; y < p.height(); y++) {
            for (int x = 0; x < p.width(); x++) {
                int rgb = p.color(x, y);
                if (rgb < 0) continue;
                if ((n++ % step) != 0) continue;
                CodexSpark s = c.spark(0, 0, 0, rgb);
                if (s == null) break;
                s.life(END + c.random.nextInt(6)).drag(1f).fall(0f).size(0.11f)
                        .trail(false, 2, 0f).twinkle(p.sparkle() || c.burst.twinkle());
                if (fade >= 0) s.fade(fade);
                dots.add(new Dot(s, (x - cx) * spacing, (cy - y) * spacing));
            }
        }
        if (dots.isEmpty()) return;

        // Which way the picture faces.
        Vec3 right;
        if (p.faceViewer()) {
            right = c.right;
        } else {
            // yaw 0: the picture faces south and reads left to right for someone standing south of it.
            double yaw = Math.toRadians(p.yaw());
            right = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        }
        Vec3 up = p.faceViewer() ? c.up : new Vec3(0, 1, 0);
        Minecraft.getInstance().particleEngine.add(new Animator(c.level, c.x, c.y, c.z, dots, right, up, p.spin()));
    }

    private record Dot(CodexSpark spark, double u, double v) {}

    /** Steers the sparks of one pattern every tick. */
    private static final class Animator extends NoRenderParticle {

        private final List<Dot> dots;
        private final Vec3 right, up, normal;
        private final boolean spin;

        Animator(ClientLevel level, double x, double y, double z, List<Dot> dots, Vec3 right, Vec3 up, boolean spin) {
            super(level, x, y, z);
            this.dots = dots;
            this.right = right.normalize();
            this.up = up.normalize();
            this.normal = this.right.cross(this.up).normalize();
            this.spin = spin;
            this.lifetime = END + 8;
            tick(); // place the sparks for the first frame
        }

        @Override
        public void tick() {
            float t = age;
            double grow = t >= GROW ? 1.0 : 1.0 - Math.pow(1.0 - t / GROW, 3); // ease out
            double sag = t > HOLD ? 0.012 * (t - HOLD) * (t - HOLD) : 0.0;

            Vec3 a = right, b = up;
            if (spin) {
                // Turn the picture around its own up axis, about one turn every 6 seconds.
                double ang = t * (Math.PI * 2 / 120.0);
                a = right.scale(Math.cos(ang)).add(normal.scale(Math.sin(ang)));
            }
            for (Dot d : dots) {
                if (!d.spark.isAlive()) continue;
                double u = d.u * grow, v = d.v * grow;
                d.spark.steer(x + a.x * u + b.x * v, y + a.y * u + b.y * v - sag, z + a.z * u + b.z * v);
            }
            if (age++ >= lifetime) remove();
        }
    }
}
