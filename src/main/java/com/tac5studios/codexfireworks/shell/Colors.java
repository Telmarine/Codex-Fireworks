package com.tac5studios.codexfireworks.shell;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.item.DyeColor;

/**
 * Colors in rocket files can be written three ways:
 *   a dye name        "light_blue"   (the vanilla firework color of that dye)
 *   a hex color       "#FFD700"
 *   a plain number    16766720
 * They are always saved on items as plain numbers.
 */
public final class Colors {

    public static final Codec<Integer> CODEC = Codec.either(Codec.INT, Codec.STRING).comapFlatMap(
            either -> either.map(DataResult::success, Colors::parse),
            Either::left);

    private Colors() {}

    private static DataResult<Integer> parse(String s) {
        String t = s.trim();
        if (t.startsWith("#") && t.length() == 7) {
            try {
                return DataResult.success(Integer.parseInt(t.substring(1), 16));
            } catch (NumberFormatException ignored) {
                return DataResult.error(() -> "Bad hex color: " + s);
            }
        }
        DyeColor dye = DyeColor.byName(t.toLowerCase(java.util.Locale.ROOT), null);
        if (dye != null) return DataResult.success(dye.getFireworkColor());
        return DataResult.error(() -> "Unknown color: " + s + " (use a dye name like \"orange\" or a hex color like \"#FFD700\")");
    }
}
