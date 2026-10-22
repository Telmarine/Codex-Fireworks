package com.tac5studios.codexfireworks.workbench;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Formation presets for the Firework Workbench. Each one places the filled burst slots
 * (in slot order) around the point where the rocket pops.
 */
public enum Formation {

    SINGLE("single"),
    RING("ring"),
    LINE("line"),
    CROSS("cross"),
    STACK("stack"),
    MULTI_BREAK("multi_break"),
    RANDOM("random");

    public final String id;

    Formation(String id) {
        this.id = id;
    }

    public String langKey() {
        return "formation.codex_fireworks." + id;
    }

    /**
     * New offsets and delays for {@code n} bursts, spaced {@code spacing} blocks apart.
     * Shapes and sizes are kept from {@code old}.
     */
    public List<SlotLayout> apply(List<SlotLayout> old, int n, float spacing, long seed) {
        List<SlotLayout> out = new ArrayList<>();
        Random r = new Random(seed);
        for (int i = 0; i < n; i++) {
            SlotLayout base = i < old.size() ? old.get(i) : SlotLayout.DEFAULT;
            float x = 0, y = 0, z = 0;
            int delay = 0;
            switch (this) {
                case SINGLE -> { }
                case RING -> {
                    double a = Math.PI / 2 + i * Math.PI * 2 / n;
                    x = (float) (Math.cos(a) * spacing);
                    y = (float) (Math.sin(a) * spacing);
                }
                case LINE -> {
                    x = (i - (n - 1) / 2f) * spacing;
                    delay = i * 2;
                }
                case CROSS -> {
                    if (i > 0) {
                        int arm = (i - 1) % 4;
                        int step = 1 + (i - 1) / 4; // 2nd ring of arms after the first four
                        float d = spacing * step;
                        x = arm == 0 ? d : arm == 1 ? -d : 0;
                        y = arm == 2 ? d : arm == 3 ? -d : 0;
                        delay = 6;
                    }
                }
                case STACK -> {
                    y = i * spacing;
                    delay = i * 4;
                }
                case MULTI_BREAK -> {
                    y = i * spacing * 0.6f;
                    delay = i * 10;
                }
                case RANDOM -> {
                    if (i > 0) {
                        x = (r.nextFloat() * 2 - 1) * spacing;
                        y = (r.nextFloat() * 2 - 1) * spacing;
                        z = (r.nextFloat() * 2 - 1) * spacing;
                        delay = 6 + r.nextInt(20);
                    }
                }
            }
            out.add(base.withOffset(round(x), round(y), round(z)).withDelay(delay));
        }
        return out;
    }

    private static float round(float v) {
        return Math.round(v * 2) / 2f; // half blocks
    }
}
