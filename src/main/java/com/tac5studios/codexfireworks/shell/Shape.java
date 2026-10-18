package com.tac5studios.codexfireworks.shell;

import com.tac5studios.codexfireworks.config.Feature;
import net.minecraft.world.item.component.FireworkExplosion;

/**
 * The built-in burst shapes. Each has its switch in features.toml and a plain vanilla shape
 * that the rocket carries as its fallback star (used when the mod isn't there to draw ours).
 */
public enum Shape {

    PEONY("peony", Feature.PEONY, FireworkExplosion.Shape.LARGE_BALL),
    CHRYSANTHEMUM("chrysanthemum", Feature.CHRYSANTHEMUM, FireworkExplosion.Shape.LARGE_BALL),
    WILLOW("willow", Feature.WILLOW, FireworkExplosion.Shape.LARGE_BALL),
    PALM("palm", Feature.PALM, FireworkExplosion.Shape.BURST),
    RING("ring", Feature.RING, FireworkExplosion.Shape.SMALL_BALL),
    DOUBLE_RING("double_ring", Feature.DOUBLE_RING, FireworkExplosion.Shape.SMALL_BALL),
    HEART("heart", Feature.HEART, FireworkExplosion.Shape.STAR),
    SPIRAL("spiral", Feature.SPIRAL, FireworkExplosion.Shape.BURST),
    CROSSETTE("crossette", Feature.CROSSETTE, FireworkExplosion.Shape.STAR),
    CRACKLE("crackle", Feature.CRACKLE, FireworkExplosion.Shape.SMALL_BALL),
    STROBE("strobe", Feature.STROBE, FireworkExplosion.Shape.SMALL_BALL),
    COMET("comet", Feature.COMET, FireworkExplosion.Shape.BURST),
    FISH("fish", Feature.FISH, FireworkExplosion.Shape.SMALL_BALL),
    BROCADE("brocade", Feature.BROCADE, FireworkExplosion.Shape.LARGE_BALL);

    public final String id;
    public final Feature feature;
    public final FireworkExplosion.Shape fallback;

    Shape(String id, Feature feature, FireworkExplosion.Shape fallback) {
        this.id = id;
        this.feature = feature;
        this.fallback = fallback;
    }

    /** Translation key of the shape name, for tooltips. */
    public String langKey() {
        return "shape.codex_fireworks." + id;
    }

    /** The shape with this id, or null. */
    public static Shape byId(String id) {
        for (Shape s : values()) if (s.id.equals(id)) return s;
        return null;
    }
}
