package com.tac5studios.codexfireworks.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tac5studios.codexfireworks.shell.Colors;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * A drawn burst: a grid of lit pixels, each one a spark.
 * Drawn patterns are 16x16. Text mode makes wider grids (up to 48x16) so two lines of letters fit.
 *
 * @param width      1-48
 * @param height     1-16
 * @param grid       one character per pixel, row by row from the top: '.' is off,
 *                   '0'-'9' and 'a'-'v' point into the palette
 * @param palette    the colors used, 0xRRGGBB (1-32)
 * @param size       "small", "medium" or "large"
 * @param faceViewer true: turns to face whoever watches it; false: faces a fixed direction (yaw)
 * @param yaw        fixed direction in degrees (0 = faces south, 90 = west, 180 = north, 270 = east)
 * @param spin       slowly turns around while it hangs in the sky
 * @param sparkle    sparks flicker near the end
 * @param fade       color the sparks fade to, or -1 for none
 */
public record Pattern(int width, int height, String grid, List<Integer> palette, String size,
                      boolean faceViewer, int yaw, boolean spin, boolean sparkle, int fade) {

    public static final int MAX_WIDTH = 48, MAX_HEIGHT = 16, DRAW_SIZE = 16, MAX_COLORS = 32;
    public static final char OFF = '.';
    private static final String INDEX = "0123456789abcdefghijklmnopqrstuv";
    public static final List<String> SIZES = List.of("small", "medium", "large");

    public static final Codec<Pattern> CODEC = RecordCodecBuilder.<Pattern>create(i -> i.group(
            Codec.intRange(1, MAX_WIDTH).fieldOf("width").forGetter(Pattern::width),
            Codec.intRange(1, MAX_HEIGHT).fieldOf("height").forGetter(Pattern::height),
            Codec.STRING.fieldOf("grid").forGetter(Pattern::grid),
            Colors.CODEC.listOf(1, MAX_COLORS).fieldOf("palette").forGetter(Pattern::palette),
            Codec.STRING.optionalFieldOf("size", "medium").forGetter(Pattern::size),
            Codec.BOOL.optionalFieldOf("face_viewer", true).forGetter(Pattern::faceViewer),
            Codec.INT.optionalFieldOf("yaw", 0).forGetter(Pattern::yaw),
            Codec.BOOL.optionalFieldOf("spin", false).forGetter(Pattern::spin),
            Codec.BOOL.optionalFieldOf("sparkle", false).forGetter(Pattern::sparkle),
            Codec.INT.optionalFieldOf("fade", -1).forGetter(Pattern::fade)
    ).apply(i, Pattern::new)).validate(Pattern::check);

    /** Sent with items and payloads. Same data as the codec. */
    public static final StreamCodec<ByteBuf, Pattern> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public Pattern {
        palette = List.copyOf(palette);
        if (!SIZES.contains(size)) size = "medium";
        yaw = Math.floorMod(yaw, 360);
    }

    private static DataResult<Pattern> check(Pattern p) {
        if (p.grid.length() != p.width * p.height) {
            return DataResult.error(() -> "Pattern grid has " + p.grid.length() + " pixels, expected " + p.width * p.height);
        }
        for (int i = 0; i < p.grid.length(); i++) {
            char c = p.grid.charAt(i);
            if (c == OFF) continue;
            int idx = INDEX.indexOf(c);
            if (idx < 0 || idx >= p.palette.size()) {
                return DataResult.error(() -> "Pattern grid uses an unknown color '" + c + "'");
            }
        }
        return DataResult.success(p);
    }

    /** An empty 16x16 pattern with the 16 dye colors as its palette. */
    public static Pattern blank() {
        char[] g = new char[DRAW_SIZE * DRAW_SIZE];
        Arrays.fill(g, OFF);
        return new Pattern(DRAW_SIZE, DRAW_SIZE, new String(g), dyePalette(), "medium", true, 0, false, false, -1);
    }

    /** The 16 vanilla firework dye colors, in dye order (white first, black last). */
    public static List<Integer> dyePalette() {
        List<Integer> out = new ArrayList<>();
        for (DyeColor d : DyeColor.values()) out.add(d.getFireworkColor());
        return out;
    }

    /** True when every color in the palette that is actually used is a dye color. */
    public boolean dyeColorsOnly() {
        List<Integer> dyes = dyePalette();
        boolean[] used = new boolean[palette.size()];
        for (int i = 0; i < grid.length(); i++) {
            int idx = colorIndex(i);
            if (idx >= 0) used[idx] = true;
        }
        for (int i = 0; i < palette.size(); i++) if (used[i] && !dyes.contains(palette.get(i))) return false;
        return fade < 0 || dyes.contains(fade);
    }

    /** Palette index of pixel i, or -1 when it's off. */
    public int colorIndex(int i) {
        char c = grid.charAt(i);
        return c == OFF ? -1 : INDEX.indexOf(c);
    }

    /** Color of the pixel at (x, y), or -1 when it's off. */
    public int color(int x, int y) {
        int idx = colorIndex(y * width + x);
        return idx < 0 ? -1 : palette.get(idx);
    }

    /** Number of lit pixels. */
    public int sparks() {
        int n = 0;
        for (int i = 0; i < grid.length(); i++) if (grid.charAt(i) != OFF) n++;
        return n;
    }

    public boolean isEmpty() {
        return sparks() == 0;
    }

    /** The colors that are used by lit pixels, up to 8 (for the vanilla fallback star). */
    public List<Integer> usedColors() {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < grid.length() && out.size() < 8; i++) {
            int idx = colorIndex(i);
            if (idx >= 0 && !out.contains(palette.get(idx))) out.add(palette.get(idx));
        }
        if (out.isEmpty()) out.add(0xFFFFFF);
        return out;
    }

    /** Spacing between two sparks in blocks, before the burst's own size and the server's burst_scale. */
    public float spacing() {
        return switch (size) {
            case "small" -> 0.3f;
            case "large" -> 0.6f;
            default -> 0.45f;
        };
    }

    public static char indexChar(int idx) {
        return INDEX.charAt(idx);
    }

    // ---- copies with one thing changed (the design screen uses these) ----

    public Pattern withGrid(int width, int height, String grid, List<Integer> palette) {
        return new Pattern(width, height, grid, palette, size, faceViewer, yaw, spin, sparkle, fade);
    }

    public Pattern withOptions(String size, boolean faceViewer, int yaw, boolean spin, boolean sparkle, int fade) {
        return new Pattern(width, height, grid, palette, size, faceViewer, yaw, spin, sparkle, fade);
    }

    /**
     * The same picture with unused palette colors dropped and the indexes packed, so saved cards stay small.
     * Colors keep their order.
     */
    public Pattern compact() {
        boolean[] used = new boolean[palette.size()];
        for (int i = 0; i < grid.length(); i++) {
            int idx = colorIndex(i);
            if (idx >= 0) used[idx] = true;
        }
        List<Integer> keep = new ArrayList<>();
        int[] remap = new int[palette.size()];
        for (int i = 0; i < palette.size(); i++) {
            if (used[i]) {
                remap[i] = keep.size();
                keep.add(palette.get(i));
            }
        }
        if (keep.isEmpty()) keep.add(palette.isEmpty() ? 0xFFFFFF : palette.getFirst());
        StringBuilder g = new StringBuilder(grid.length());
        for (int i = 0; i < grid.length(); i++) {
            int idx = colorIndex(i);
            g.append(idx < 0 ? OFF : indexChar(remap[idx]));
        }
        return withGrid(width, height, g.toString(), keep);
    }

    /** Size name with a capital letter, for buttons. */
    public String sizeLabel() {
        return size.substring(0, 1).toUpperCase(Locale.ROOT) + size.substring(1);
    }
}
