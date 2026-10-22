package com.tac5studios.codexfireworks.pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.tac5studios.codexfireworks.shell.Colors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads one pattern preset file. Two kinds:
 *
 * <pre>
 * Drawn:  { "name": "Smiley",
 *           "rows": [ "....yyyy....", ... ],          up to 48 wide and 16 high, '.' = off
 *           "colors": { "y": "yellow", "k": "#202020" } }
 *
 * Text:   { "name": "GG", "text": "GG", "colors": [ "yellow", "orange" ] }   line 1 / line 2 color
 * </pre>
 *
 * Both take the optional pattern options: "size", "face_viewer", "yaw", "spin", "sparkle", "fade".
 */
public final class PatternFile {

    public record Loaded(String name, Pattern pattern) {}

    private PatternFile() {}

    /** Throws IllegalArgumentException with a readable reason when the file is wrong. */
    public static Loaded read(JsonElement json) {
        if (!json.isJsonObject()) throw new IllegalArgumentException("expected a JSON object");
        JsonObject o = json.getAsJsonObject();
        String name = o.has("name") ? o.get("name").getAsString() : "Pattern";

        String size = o.has("size") ? o.get("size").getAsString() : "medium";
        boolean face = !o.has("face_viewer") || o.get("face_viewer").getAsBoolean();
        int yaw = o.has("yaw") ? o.get("yaw").getAsInt() : 0;
        boolean spin = o.has("spin") && o.get("spin").getAsBoolean();
        boolean sparkle = o.has("sparkle") && o.get("sparkle").getAsBoolean();
        int fade = o.has("fade") ? color(o.get("fade")) : -1;
        Pattern base = Pattern.blank().withOptions(size, face, yaw, spin, sparkle, fade);

        if (o.has("text")) {
            List<Integer> colors = new ArrayList<>();
            if (o.has("colors") && o.get("colors").isJsonArray()) {
                for (JsonElement e : o.getAsJsonArray("colors")) colors.add(color(e));
            }
            if (colors.isEmpty()) colors.add(0xFFFFFF);
            String[] err = new String[1];
            List<String> lines = PatternText.lines(o.get("text").getAsString(), err);
            if (lines == null) throw new IllegalArgumentException("text doesn't fit (" + err[0] + ")");
            return new Loaded(name, PatternText.render(lines, colors, base));
        }

        if (!o.has("rows") || !o.get("rows").isJsonArray()) throw new IllegalArgumentException("needs \"rows\" or \"text\"");
        JsonArray rows = o.getAsJsonArray("rows");
        int height = rows.size();
        int width = 0;
        for (JsonElement r : rows) width = Math.max(width, r.getAsString().length());
        if (height < 1 || height > Pattern.MAX_HEIGHT || width < 1 || width > Pattern.MAX_WIDTH) {
            throw new IllegalArgumentException("rows must be 1-" + Pattern.MAX_WIDTH + " wide and 1-" + Pattern.MAX_HEIGHT + " high");
        }

        Map<Character, Integer> index = new HashMap<>();
        List<Integer> palette = new ArrayList<>();
        JsonObject colors = o.has("colors") && o.get("colors").isJsonObject() ? o.getAsJsonObject("colors") : new JsonObject();
        for (Map.Entry<String, JsonElement> e : colors.entrySet()) {
            if (e.getKey().length() != 1) throw new IllegalArgumentException("color keys must be one character: " + e.getKey());
            if (palette.size() >= Pattern.MAX_COLORS) throw new IllegalArgumentException("too many colors");
            index.put(e.getKey().charAt(0), palette.size());
            palette.add(color(e.getValue()));
        }

        char[] grid = new char[width * height];
        Arrays.fill(grid, Pattern.OFF);
        for (int y = 0; y < height; y++) {
            String row = rows.get(y).getAsString();
            for (int x = 0; x < row.length(); x++) {
                char c = row.charAt(x);
                if (c == '.' || c == ' ') continue;
                Integer idx = index.get(c);
                if (idx == null) throw new IllegalArgumentException("row " + (y + 1) + " uses '" + c + "', which has no color");
                grid[y * width + x] = Pattern.indexChar(idx);
            }
        }
        if (palette.isEmpty()) palette.add(0xFFFFFF);
        Pattern p = base.withGrid(width, height, new String(grid), palette);
        Pattern.CODEC.encodeStart(JsonOps.INSTANCE, p).getOrThrow(IllegalArgumentException::new); // run the checks
        return new Loaded(name, p.compact());
    }

    private static int color(JsonElement e) {
        return Colors.CODEC.parse(JsonOps.INSTANCE, e).getOrThrow(IllegalArgumentException::new);
    }
}
