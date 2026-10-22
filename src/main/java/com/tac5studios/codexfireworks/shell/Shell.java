package com.tac5studios.codexfireworks.shell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.FireworkExplosion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Everything this mod adds to a firework rocket: the list of bursts, the rocket's own scale
 * (1.0 = normal; multiplies spread, offsets and spark size on top of the server's burst_scale)
 * and its rarity tag ("common", "rare", "epic", or "" for none).
 *
 * <p>It's stored inside the rocket's vanilla custom_data, under the key "codex_fireworks".
 * The rocket also keeps normal vanilla stars in its fireworks data, so it still flies and pops
 * where the mod isn't installed or is turned off, and removing the mod never breaks the item.
 */
public record Shell(List<Burst> bursts, float scale, String rarity) {

    public static final String KEY = "codex_fireworks";

    public static final Codec<Shell> CODEC = RecordCodecBuilder.create(i -> i.group(
            Burst.CODEC.listOf().fieldOf("bursts").forGetter(Shell::bursts),
            Codec.floatRange(0.25f, 4.0f).optionalFieldOf("scale", 1.0f).forGetter(Shell::scale),
            Codec.STRING.optionalFieldOf("rarity", "").forGetter(Shell::rarity)
    ).apply(i, Shell::new));

    public Shell {
        bursts = List.copyOf(bursts);
        if (!Rarity.isTag(rarity)) rarity = "";
    }

    public Shell(List<Burst> bursts, float scale) {
        this(bursts, scale, "");
    }

    /** The shell on this rocket, if it has one. */
    public static Optional<Shell> read(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(KEY)) return Optional.empty();
        Tag tag = data.copyTag().get(KEY);
        if (tag == null) return Optional.empty();
        return CODEC.parse(NbtOps.INSTANCE, tag).result().filter(s -> !s.bursts().isEmpty());
    }

    /** Put this shell on the rocket (keeps any other custom data). */
    public void write(ItemStack stack) {
        Tag tag = CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, (CompoundTag root) -> root.put(KEY, tag));
    }

    /** Plain vanilla stars matching this shell, used as the fallback. */
    public List<FireworkExplosion> fallbackStars() {
        List<FireworkExplosion> out = new ArrayList<>();
        for (Burst b : bursts) {
            Shape shape = Shape.byId(b.shape());
            FireworkExplosion.Shape vanilla = shape == null ? FireworkExplosion.Shape.SMALL_BALL : shape.fallback;
            out.add(new FireworkExplosion(vanilla, new IntArrayList(b.colors()), new IntArrayList(b.fades()),
                    b.trail(), b.twinkle() || (b.pattern() != null && b.pattern().sparkle())));
        }
        return out;
    }
}
