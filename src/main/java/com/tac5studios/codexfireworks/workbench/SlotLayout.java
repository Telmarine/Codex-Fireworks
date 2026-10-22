package com.tac5studios.codexfireworks.workbench;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Where one burst slot of the Firework Workbench goes off.
 *
 * @param shape shape id for a firework star ("" = pick one from the star's vanilla shape); ignored for Pattern Cards
 * @param size  0.25-2.0
 * @param dx    offset in blocks, east/west
 * @param dy    offset in blocks, up/down
 * @param dz    offset in blocks, north/south
 * @param delay ticks after the rocket pops
 */
public record SlotLayout(String shape, float size, float dx, float dy, float dz, int delay) {

    public static final SlotLayout DEFAULT = new SlotLayout("", 1.0f, 0, 0, 0, 0);

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotLayout> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(32), SlotLayout::shape,
            ByteBufCodecs.FLOAT, SlotLayout::size,
            ByteBufCodecs.FLOAT, SlotLayout::dx,
            ByteBufCodecs.FLOAT, SlotLayout::dy,
            ByteBufCodecs.FLOAT, SlotLayout::dz,
            ByteBufCodecs.VAR_INT, SlotLayout::delay,
            SlotLayout::new);

    public SlotLayout withShape(String s) {
        return new SlotLayout(s, size, dx, dy, dz, delay);
    }

    public SlotLayout withSize(float s) {
        return new SlotLayout(shape, Math.max(0.25f, Math.min(2.0f, s)), dx, dy, dz, delay);
    }

    public SlotLayout withOffset(float x, float y, float z) {
        return new SlotLayout(shape, size, x, y, z, delay);
    }

    public SlotLayout withDelay(int d) {
        return new SlotLayout(shape, size, dx, dy, dz, Math.max(0, d));
    }
}
