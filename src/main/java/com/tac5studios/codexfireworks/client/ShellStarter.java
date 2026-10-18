package com.tac5studios.codexfireworks.client;

import com.tac5studios.codexfireworks.client.shapes.BuiltInShapes;
import com.tac5studios.codexfireworks.client.shapes.BurstContext;
import com.tac5studios.codexfireworks.config.ClientConfig;
import com.tac5studios.codexfireworks.shell.Burst;
import com.tac5studios.codexfireworks.shell.Shape;
import com.tac5studios.codexfireworks.shell.Shell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * An invisible particle that runs one rocket's shell: each burst goes off at its own delay and offset.
 * Removes itself once the last burst has gone off.
 */
public class ShellStarter extends NoRenderParticle {

    private final List<Burst> bursts;
    private final float rocketScale;

    public ShellStarter(ClientLevel level, double x, double y, double z, Shell shell) {
        super(level, x, y, z);
        this.bursts = shell.bursts();
        this.rocketScale = shell.scale();
        int last = 0;
        for (Burst b : bursts) last = Math.max(last, b.delay());
        this.lifetime = last + 2;
    }

    @Override
    public void tick() {
        for (Burst b : bursts) {
            if (b.delay() == age) explode(b);
        }
        if (age++ >= lifetime) remove();
    }

    private void explode(Burst b) {
        // Offsets grow with the bursts, so formations keep their spacing.
        double k = ClientSettings.burstScale() * rocketScale;
        double bx = x + b.dx() * k, by = y + b.dy() * k, bz = z + b.dz() * k;
        Shape shape = Shape.byId(b.shape());
        if (shape == null || ClientSettings.shapeOff(shape.id)) shape = Shape.PEONY;

        level.addParticle(ParticleTypes.FLASH, bx, by, bz, 0, 0, 0);
        BuiltInShapes.draw(shape, new BurstContext(level, b, bx, by, bz, rocketScale));
        sound(shape, b, bx, by, bz);
    }

    private void sound(Shape shape, Burst b, double bx, double by, double bz) {
        float volume = (float) (20f * ClientConfig.VOLUME.get());
        if (volume <= 0f) return;
        boolean far = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()
                .distanceToSqr(new Vec3(bx, by, bz)) >= 256.0;
        boolean large = switch (shape) {
            case PEONY, CHRYSANTHEMUM, WILLOW, PALM, BROCADE -> true;
            default -> false;
        };
        if (!ClientSettings.sounds()) large = false;

        SoundEvent blast = large
                ? (far ? SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_LARGE_BLAST)
                : (far ? SoundEvents.FIREWORK_ROCKET_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_BLAST);
        float pitch = 0.95f + random.nextFloat() * 0.1f;
        if (ClientSettings.sounds()) {
            pitch = switch (shape) {
                case WILLOW -> 0.75f;
                case PALM -> 0.85f;
                case RING, DOUBLE_RING, HEART -> 1.1f;
                case COMET -> 1.2f;
                default -> pitch;
            };
        }
        level.playLocalSound(bx, by, bz, blast, SoundSource.AMBIENT, volume, pitch, true);

        boolean sparkle = b.twinkle() || (ClientSettings.sounds() && switch (shape) {
            case CRACKLE, STROBE, BROCADE, WILLOW -> true;
            default -> false;
        });
        if (sparkle) {
            ClientSchedule.later(level, shape == Shape.CRACKLE ? 18 : 30, () ->
                    level.playLocalSound(bx, by, bz,
                            far ? SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR : SoundEvents.FIREWORK_ROCKET_TWINKLE,
                            SoundSource.AMBIENT, volume, 0.9f + random.nextFloat() * 0.15f, true));
        }
    }
}
