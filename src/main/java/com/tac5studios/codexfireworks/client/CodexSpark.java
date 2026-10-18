package com.tac5studios.codexfireworks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.NotNull;

/**
 * The spark every burst is drawn with. Fully bright, animated with the vanilla spark textures,
 * and with a few behaviours the shapes switch on: trail, twinkle, strobe, wiggle, split and crackle.
 */
public final class CodexSpark extends TextureSheetParticle {

    /** Filled in when the particle provider is registered (client start). */
    static SpriteSet sprites;


    /** Called when a splitting spark reaches its split age. */
    public interface Splitter {
        void split(CodexSpark spark);
    }

    private final float baseR, baseG, baseB;
    private float fadeR, fadeG, fadeB;
    private boolean hasFade;
    private boolean trail;
    private int trailEvery = 2;
    private float trailUntil = 0.6f;
    private boolean twinkle;
    private boolean strobe;
    private float wiggle;
    private int splitAge = -1;
    private Splitter splitter;
    private boolean crackle;
    private float sparkScale = 1f;

    CodexSpark(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, int rgb) {
        super(level, x, y, z);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.baseR = ((rgb >> 16) & 0xFF) / 255f;
        this.baseG = ((rgb >> 8) & 0xFF) / 255f;
        this.baseB = (rgb & 0xFF) / 255f;
        setColor(baseR, baseG, baseB);
        this.friction = 0.91f;
        this.gravity = 0.05f;
        this.hasPhysics = false;
        this.quadSize = 0.1f;
        this.lifetime = 45;
        if (sprites != null) setSpriteFromAge(sprites);
    }

    /** Make a spark and add it to the particle engine. Returns null when the textures aren't ready. */
    public static CodexSpark spawn(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, int rgb) {
        if (sprites == null) return null;
        CodexSpark s = new CodexSpark(level, x, y, z, vx, vy, vz, rgb);
        Minecraft.getInstance().particleEngine.add(s);
        return s;
    }

    // ---- settings used by the shapes (chainable) ----

    /** Grow this spark (and the size calls after it) by this factor. */
    public CodexSpark scaleSparks(float scale) {
        this.sparkScale = scale;
        this.quadSize = 0.1f * scale;
        return this;
    }

    public float sparkScale() {
        return sparkScale;
    }

    public CodexSpark life(int ticks) {
        this.lifetime = Math.max(2, ticks);
        return this;
    }

    public CodexSpark drag(float friction) {
        this.friction = friction;
        return this;
    }

    public CodexSpark fall(float gravity) {
        this.gravity = gravity;
        return this;
    }

    /** Spark size, where 0.1 is a vanilla firework spark (multiplied by this spark's scale). */
    public CodexSpark size(float quad) {
        this.quadSize = quad * sparkScale;
        return this;
    }

    public CodexSpark fade(int rgb) {
        if (rgb >= 0) {
            this.hasFade = true;
            this.fadeR = ((rgb >> 16) & 0xFF) / 255f;
            this.fadeG = ((rgb >> 8) & 0xFF) / 255f;
            this.fadeB = (rgb & 0xFF) / 255f;
        }
        return this;
    }

    /** Leave a trail of short sparks every {@code every} ticks for the first {@code untilPart} of its life. */
    public CodexSpark trail(boolean on, int every, float untilPart) {
        this.trail = on;
        this.trailEvery = Math.max(1, every);
        this.trailUntil = untilPart;
        return this;
    }

    public CodexSpark twinkle(boolean on) {
        this.twinkle = on;
        return this;
    }

    public CodexSpark strobe(boolean on) {
        this.strobe = on;
        return this;
    }

    public CodexSpark wiggle(float amount) {
        this.wiggle = amount;
        return this;
    }

    public CodexSpark splitAt(int age, Splitter splitter) {
        this.splitAge = age;
        this.splitter = splitter;
        return this;
    }

    public CodexSpark crackle(boolean on) {
        this.crackle = on;
        return this;
    }

    public int rgb() {
        return ((int) (baseR * 255) << 16) | ((int) (baseG * 255) << 8) | (int) (baseB * 255);
    }

    public double px() {
        return x;
    }

    public double py() {
        return y;
    }

    public double pz() {
        return z;
    }

    public ClientLevel level() {
        return level;
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) {
            if (crackle) pop();
            return;
        }
        if (sprites != null) setSpriteFromAge(sprites);

        if (splitter != null && age == splitAge) {
            Splitter s = splitter;
            splitter = null;
            s.split(this);
            remove();
            return;
        }

        float part = (float) age / lifetime;

        // Color: fade toward the fade color over the second half of the spark's life.
        if (hasFade && part > 0.5f) {
            float t = Math.min(1f, (part - 0.5f) * 2f);
            setColor(baseR + (fadeR - baseR) * t, baseG + (fadeG - baseG) * t, baseB + (fadeB - baseB) * t);
        }

        // Brightness: strobe blinks the whole life, twinkle flickers in the last two thirds,
        // and every spark dims a little at the very end.
        float a = 1f;
        if (strobe) a = ((age / 3) % 2 == 0) ? 1f : 0.05f;
        else if (twinkle && part > 0.33f) a = random.nextFloat() < 0.5f ? 1f : 0.25f;
        if (part > 0.85f) a *= (1f - part) / 0.15f;
        setAlpha(Math.max(0f, a));

        if (wiggle > 0f) {
            xd += (random.nextFloat() - 0.5f) * wiggle;
            zd += (random.nextFloat() - 0.5f) * wiggle;
            yd += (random.nextFloat() - 0.5f) * wiggle * 0.5f;
        }

        if (trail && part < trailUntil && (age % trailEvery) == 0) {
            CodexSpark t = spawn(level, x, y, z, 0, 0, 0, rgb());
            if (t != null) {
                t.sparkScale = sparkScale;
                t.life(10 + random.nextInt(6)).fall(0.02f);
                t.quadSize = quadSize * 0.8f;
                if (hasFade) t.fade(((int) (fadeR * 255) << 16) | ((int) (fadeG * 255) << 8) | (int) (fadeB * 255));
                t.setAlpha(0.7f);
            }
        }
    }

    /** Crackle: two or three tiny flashes where the spark died. */
    private void pop() {
        crackle = false;
        int n = 2 + random.nextInt(2);
        for (int i = 0; i < n; i++) {
            CodexSpark s = spawn(level, x, y, z,
                    (random.nextFloat() - 0.5f) * 0.12f, (random.nextFloat() - 0.5f) * 0.12f, (random.nextFloat() - 0.5f) * 0.12f,
                    0xFFF4D6);
            if (s != null) {
                s.life(3 + random.nextInt(3)).fall(0f).drag(0.7f);
                s.quadSize = quadSize * 0.6f;
            }
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0; // full bright
    }
}
