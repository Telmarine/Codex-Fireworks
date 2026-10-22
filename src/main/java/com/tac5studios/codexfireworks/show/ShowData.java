package com.tac5studios.codexfireworks.show;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * A show: a named, timed list of cues. Lives on a Show Controller and can be copied to a Show Card.
 *
 * @param name          used by /fireworks show start <name>; may be empty
 * @param loop          start again after the last cue
 * @param redstoneStart a redstone pulse on the controller starts the show
 * @param cues          sorted by time
 */
public record ShowData(String name, boolean loop, boolean redstoneStart, List<Cue> cues) {

    public static final int MAX_NAME = 32;
    public static final ShowData EMPTY = new ShowData("", false, true, List.of());

    public static final Codec<ShowData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(ShowData::name),
            Codec.BOOL.optionalFieldOf("loop", false).forGetter(ShowData::loop),
            Codec.BOOL.optionalFieldOf("redstone_start", true).forGetter(ShowData::redstoneStart),
            Cue.CODEC.listOf().optionalFieldOf("cues", List.of()).forGetter(ShowData::cues)
    ).apply(i, ShowData::new));

    public static final StreamCodec<ByteBuf, ShowData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(MAX_NAME * 4), ShowData::name,
            ByteBufCodecs.BOOL, ShowData::loop,
            ByteBufCodecs.BOOL, ShowData::redstoneStart,
            Cue.STREAM_CODEC.apply(ByteBufCodecs.list(4096)), ShowData::cues,
            ShowData::new);

    public ShowData {
        name = cleanName(name);
        List<Cue> sorted = new ArrayList<>(cues);
        sorted.sort(Comparator.comparingInt(Cue::time));
        cues = List.copyOf(sorted);
    }

    /** Ticks from the start to the last cue. */
    public int length() {
        return cues.isEmpty() ? 0 : cues.getLast().time();
    }

    public ShowData withCues(List<Cue> c) {
        return new ShowData(name, loop, redstoneStart, c);
    }

    public ShowData withName(String n) {
        return new ShowData(n, loop, redstoneStart, cues);
    }

    public ShowData withLoop(boolean l) {
        return new ShowData(name, l, redstoneStart, cues);
    }

    public ShowData withRedstoneStart(boolean r) {
        return new ShowData(name, loop, r, cues);
    }

    /** Names are lower case without spaces, so they can be typed in /fireworks show start. */
    public static String cleanName(String n) {
        if (n == null) return "";
        String s = n.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_\\-]", "_");
        return s.length() > MAX_NAME ? s.substring(0, MAX_NAME) : s;
    }
}
