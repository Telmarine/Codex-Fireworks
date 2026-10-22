package com.tac5studios.codexfireworks.shell;

import java.util.List;

/**
 * Rocket rarity tags: Common, Rare and Epic. A rocket with a tag also gets the vanilla rarity component,
 * so its name shows in that color (white, aqua, light purple) with or without this mod.
 */
public final class Rarity {

    public static final List<String> TAGS = List.of("common", "rare", "epic");

    private Rarity() {}

    public static boolean isTag(String s) {
        return TAGS.contains(s);
    }

    /** The vanilla rarity used for the item name color. */
    public static net.minecraft.world.item.Rarity vanilla(String tag) {
        return switch (tag) {
            case "rare" -> net.minecraft.world.item.Rarity.RARE;
            case "epic" -> net.minecraft.world.item.Rarity.EPIC;
            default -> net.minecraft.world.item.Rarity.COMMON;
        };
    }

    public static String langKey(String tag) {
        return "rarity.codex_fireworks." + tag;
    }
}
