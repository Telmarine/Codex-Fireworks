package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.show.ShowData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/** Player -> server: save the edited show on this Show Controller. */
public record SaveShowPayload(BlockPos pos, ShowData show) implements CustomPacketPayload {

    public static final Type<SaveShowPayload> TYPE = new Type<>(CodexFireworks.id("save_show"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveShowPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SaveShowPayload::pos,
            ShowData.STREAM_CODEC, SaveShowPayload::show,
            SaveShowPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
