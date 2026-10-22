package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * Player -> server: a button in the Show Controller screen.
 *
 * @param action one of the constants below
 * @param arg    cue or launcher index for TEST_CUE / UNLINK
 */
public record ShowActionPayload(BlockPos pos, int action, int arg) implements CustomPacketPayload {

    public static final int START = 0, STOP = 1, WRITE_CARD = 2, READ_CARD = 3, UNLINK = 4, LINK = 5, TEST_CUE = 6;

    public static final Type<ShowActionPayload> TYPE = new Type<>(CodexFireworks.id("show_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShowActionPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ShowActionPayload::pos,
            ByteBufCodecs.VAR_INT, ShowActionPayload::action,
            ByteBufCodecs.VAR_INT, ShowActionPayload::arg,
            ShowActionPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
