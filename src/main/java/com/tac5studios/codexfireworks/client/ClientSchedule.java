package com.tac5studios.codexfireworks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;

/** Runs a small client action a few ticks later (used for the delayed sparkle sound). */
public final class ClientSchedule {

    private ClientSchedule() {}

    public static void later(ClientLevel level, int ticks, Runnable action) {
        Minecraft.getInstance().particleEngine.add(new Delay(level, ticks, action));
    }

    private static final class Delay extends NoRenderParticle {

        private final Runnable action;

        Delay(ClientLevel level, int ticks, Runnable action) {
            super(level, 0, 0, 0);
            this.lifetime = Math.max(1, ticks);
            this.action = action;
        }

        @Override
        public void tick() {
            if (age++ >= lifetime) {
                remove();
                action.run();
            }
        }
    }
}
