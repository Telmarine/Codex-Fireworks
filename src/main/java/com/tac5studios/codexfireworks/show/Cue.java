package com.tac5studios.codexfireworks.show;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One moment in a show.
 *
 * @param time     ticks after the show starts (20 = 1 second)
 * @param launcher 1 = the first linked launcher, 2 = the second, ...; 0 = every linked launcher
 * @param slot     1-9 = that slot of the launcher; 0 = the next rocket in turn
 */
public record Cue(int time, int launcher, int slot) {

    public static final int MAX_TIME = 20 * 60 * 20; // 20 minutes

    public static final Codec<Cue> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, MAX_TIME).fieldOf("time").forGetter(Cue::time),
            Codec.intRange(0, 256).optionalFieldOf("launcher", 0).forGetter(Cue::launcher),
            Codec.intRange(0, 9).optionalFieldOf("slot", 0).forGetter(Cue::slot)
    ).apply(i, Cue::new));

    public static final StreamCodec<ByteBuf, Cue> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Cue::time,
            ByteBufCodecs.VAR_INT, Cue::launcher,
            ByteBufCodecs.VAR_INT, Cue::slot,
            Cue::new);

    public Cue {
        time = Math.max(0, Math.min(MAX_TIME, time));
        launcher = Math.max(0, Math.min(256, launcher));
        slot = Math.max(0, Math.min(9, slot));
    }
}
