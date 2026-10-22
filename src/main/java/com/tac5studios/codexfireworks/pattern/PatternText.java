package com.tac5studios.codexfireworks.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Text mode: turns up to 12 characters into a pattern with the 5x7 font, on one or two lines.
 * Write "|" to choose where the second line starts; otherwise a long text is split at the space
 * nearest the middle. A line holds up to 8 characters, so a pattern can be up to 47 pixels wide.
 */
public final class PatternText {

    public static final int MAX_CHARS = 12, MAX_LINE = 8;

    private PatternText() {}

    /** The text split into its lines, or null with a reason in {@code error[0]} when it doesn't fit. */
    public static List<String> lines(String text, String[] error) {
        String t = text.toUpperCase(Locale.ROOT).trim();
        int letters = 0;
        for (char c : t.toCharArray()) {
            if (c == '|' || c == ' ') continue;
            if (!Font5x7.has(c)) {
                error[0] = "char:" + c;
                return null;
            }
            letters++;
        }
        if (letters == 0) {
            error[0] = "empty";
            return null;
        }
        if (letters > MAX_CHARS) {
            error[0] = "too_long";
            return null;
        }
        List<String> lines = new ArrayList<>();
        if (t.contains("|")) {
            for (String part : t.split("\\|", -1)) lines.add(part.trim());
        } else if (t.length() > MAX_LINE) {
            int best = -1;
            for (int i = 0; i < t.length(); i++) {
                if (t.charAt(i) == ' ' && (best < 0 || Math.abs(i - t.length() / 2) < Math.abs(best - t.length() / 2))) best = i;
            }
            if (best < 0) {
                error[0] = "line_too_long";
                return null;
            }
            lines.add(t.substring(0, best).trim());
            lines.add(t.substring(best + 1).trim());
        } else {
            lines.add(t);
        }
        lines.removeIf(String::isEmpty);
        if (lines.isEmpty()) {
            error[0] = "empty";
            return null;
        }
        if (lines.size() > 2) {
            error[0] = "too_many_lines";
            return null;
        }
        for (String l : lines) {
            if (l.length() > MAX_LINE) {
                error[0] = "line_too_long";
                return null;
            }
        }
        return lines;
    }

    /**
     * Draw the lines into a pattern, keeping the options of {@code base}.
     *
     * @param colors palette for line 1 and line 2 (one color is used for both lines)
     */
    public static Pattern render(List<String> lines, List<Integer> colors, Pattern base) {
        int longest = 0;
        for (String l : lines) longest = Math.max(longest, l.length());
        int width = Math.max(1, longest * (Font5x7.W + 1) - 1);
        int height = lines.size() == 1 ? Font5x7.H : Pattern.MAX_HEIGHT;
        char[] grid = new char[width * height];
        Arrays.fill(grid, Pattern.OFF);
        List<Integer> palette = colors.isEmpty() ? List.of(0xFFFFFF) : List.copyOf(colors.subList(0, Math.min(2, colors.size())));

        for (int line = 0; line < lines.size(); line++) {
            String l = lines.get(line);
            int lineWidth = l.length() * (Font5x7.W + 1) - 1;
            int left = (width - lineWidth) / 2;
            int top = line == 0 ? 0 : height - Font5x7.H;
            char color = Pattern.indexChar(Math.min(line, palette.size() - 1));
            for (int i = 0; i < l.length(); i++) {
                char c = l.charAt(i);
                for (int y = 0; y < Font5x7.H; y++) {
                    for (int x = 0; x < Font5x7.W; x++) {
                        if (Font5x7.lit(c, x, y)) grid[(top + y) * width + left + i * (Font5x7.W + 1) + x] = color;
                    }
                }
            }
        }
        return base.withGrid(width, height, new String(grid), palette);
    }
}
