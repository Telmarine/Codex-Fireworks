package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * Server -> player: launcher linking started (or was extended) or finished. The client needs this so a
 * shift-right-click on a launcher links it instead of placing the held block.
 */
public record LinkingPayload(boolean active) implements CustomPacketPayload {

    public static final Type<LinkingPayload> TYPE = new Type<>(CodexFireworks.id("linking"));

    public static final StreamCodec<ByteBuf, LinkingPayload> CODEC =
            ByteBufCodecs.BOOL.map(LinkingPayload::new, LinkingPayload::active);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
