package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.show.ShowData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Server -> player: open (or refresh) the Show Controller screen.
 *
 * @param pos       the controller
 * @param show      its show
 * @param launchers linked launchers in order
 * @param running   the show is playing
 * @param maxCues   limits.toml max_cues
 */
public record OpenShowPayload(BlockPos pos, ShowData show, List<BlockPos> launchers, boolean running, int maxCues)
        implements CustomPacketPayload {

    public static final Type<OpenShowPayload> TYPE = new Type<>(CodexFireworks.id("open_show"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenShowPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenShowPayload::pos,
            ShowData.STREAM_CODEC, OpenShowPayload::show,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(256)), OpenShowPayload::launchers,
            ByteBufCodecs.BOOL, OpenShowPayload::running,
            ByteBufCodecs.VAR_INT, OpenShowPayload::maxCues,
            OpenShowPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
