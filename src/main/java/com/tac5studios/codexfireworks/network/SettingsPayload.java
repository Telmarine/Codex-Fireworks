package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Server -> player: the server's switches that change how bursts are drawn.
 *
 * @param enabled        master switch; off = rockets pop with their vanilla stars
 * @param disabledShapes shapes switched off; those bursts are drawn as a peony
 * @param sounds         shape_sounds switch
 * @param burstScale     limits.toml [display] burst_scale
 * @param sparkScale     limits.toml [display] spark_scale
 */
public record SettingsPayload(boolean enabled, List<String> disabledShapes, boolean sounds, float burstScale, float sparkScale)
        implements CustomPacketPayload {

    public static final Type<SettingsPayload> TYPE = new Type<>(CodexFireworks.id("settings"));

    public static final StreamCodec<ByteBuf, SettingsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SettingsPayload::enabled,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list(64)), SettingsPayload::disabledShapes,
            ByteBufCodecs.BOOL, SettingsPayload::sounds,
            ByteBufCodecs.FLOAT, SettingsPayload::burstScale,
            ByteBufCodecs.FLOAT, SettingsPayload::sparkScale,
            SettingsPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
