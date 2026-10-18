package com.tac5studios.codexfireworks.shell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * A ready-made rocket, read from data/<namespace>/rocket_presets/<id>.json.
 * Servers can replace or switch off any built-in preset with a datapack file of the same name.
 *
 * @param name    item name shown to players
 * @param flight  flight duration 1-3 (like gunpowder in vanilla)
 * @param enabled false hides the preset
 * @param bursts  1-8 bursts
 * @param scale   size of this rocket, 1.0 = normal (0.25-4.0); multiplies spread, offsets and spark size
 */
public record RocketPreset(String name, int flight, boolean enabled, List<Burst> bursts, float scale) {

    public static final Codec<RocketPreset> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("name").forGetter(RocketPreset::name),
            Codec.intRange(1, 3).optionalFieldOf("flight", 2).forGetter(RocketPreset::flight),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(RocketPreset::enabled),
            Burst.CODEC.listOf(1, 8).fieldOf("bursts").forGetter(RocketPreset::bursts),
            Codec.floatRange(0.25f, 4.0f).optionalFieldOf("scale", 1.0f).forGetter(RocketPreset::scale)
    ).apply(i, RocketPreset::new));

    public RocketPreset {
        bursts = List.copyOf(bursts);
    }
}
