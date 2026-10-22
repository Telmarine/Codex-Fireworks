package com.tac5studios.codexfireworks.shell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tac5studios.codexfireworks.pattern.Pattern;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * One explosion inside a rocket.
 *
 * @param shape   built-in shape id ("peony", "willow", ...) or "pattern"
 * @param colors  spark colors as 0xRRGGBB (at least one)
 * @param fades   colors the sparks fade to, may be empty
 * @param trail   sparks leave a trail
 * @param twinkle sparks flicker near the end
 * @param size    1.0 = normal; 0.5 small, 1.5 large
 * @param dx      offset from the rocket in blocks (east/west)
 * @param dy      offset up/down
 * @param dz      offset north/south
 * @param delay   ticks after the rocket pops before this burst goes off
 * @param pattern the drawn picture, only for the "pattern" shape
 */
public record Burst(String shape, List<Integer> colors, List<Integer> fades, boolean trail, boolean twinkle,
                    float size, float dx, float dy, float dz, int delay, @Nullable Pattern pattern) {

    public static final Codec<Burst> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("shape").forGetter(Burst::shape),
            Colors.CODEC.listOf(1, 8).fieldOf("colors").forGetter(Burst::colors),
            Colors.CODEC.listOf(0, 8).optionalFieldOf("fades", List.of()).forGetter(Burst::fades),
            Codec.BOOL.optionalFieldOf("trail", false).forGetter(Burst::trail),
            Codec.BOOL.optionalFieldOf("twinkle", false).forGetter(Burst::twinkle),
            Codec.floatRange(0.25f, 2.0f).optionalFieldOf("size", 1.0f).forGetter(Burst::size),
            Codec.FLOAT.optionalFieldOf("dx", 0f).forGetter(Burst::dx),
            Codec.FLOAT.optionalFieldOf("dy", 0f).forGetter(Burst::dy),
            Codec.FLOAT.optionalFieldOf("dz", 0f).forGetter(Burst::dz),
            Codec.intRange(0, 200).optionalFieldOf("delay", 0).forGetter(Burst::delay),
            Pattern.CODEC.optionalFieldOf("pattern").forGetter(b -> Optional.ofNullable(b.pattern()))
    ).apply(i, (shape, colors, fades, trail, twinkle, size, dx, dy, dz, delay, pattern) ->
            new Burst(shape, colors, fades, trail, twinkle, size, dx, dy, dz, delay, pattern.orElse(null))));

    public Burst {
        colors = List.copyOf(colors);
        fades = List.copyOf(fades);
    }

    /** A burst of a built-in shape (no pattern). */
    public Burst(String shape, List<Integer> colors, List<Integer> fades, boolean trail, boolean twinkle,
                 float size, float dx, float dy, float dz, int delay) {
        this(shape, colors, fades, trail, twinkle, size, dx, dy, dz, delay, null);
    }

    /** A copy with offsets and delay clamped to the server limits. */
    public Burst clamped(int maxOffset, int maxDelay) {
        return new Burst(shape, colors, fades, trail, twinkle, size,
                clamp(dx, maxOffset), clamp(dy, maxOffset), clamp(dz, maxOffset), Math.min(delay, maxDelay), pattern);
    }

    private static float clamp(float v, int max) {
        return Math.max(-max, Math.min(max, v));
    }
}
