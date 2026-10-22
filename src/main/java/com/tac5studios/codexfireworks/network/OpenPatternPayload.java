package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.pattern.Pattern;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Server -> player: open the pattern design screen for the card in hand.
 *
 * @param offhand   the card is in the off hand
 * @param hex       this player may use custom hex colors
 * @param textMode  text mode is switched on
 * @param maxSparks most lit pixels allowed
 * @param presets   ready-made patterns the player can load (empty when pattern presets are off)
 */
public record OpenPatternPayload(boolean offhand, boolean hex, boolean textMode, int maxSparks, List<Preset> presets)
        implements CustomPacketPayload {

    /** One ready-made pattern. */
    public record Preset(String id, String name, Pattern pattern) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Preset> CODEC = StreamCodec.composite(
                ByteBufCodecs.stringUtf8(256), Preset::id,
                ByteBufCodecs.stringUtf8(64), Preset::name,
                Pattern.STREAM_CODEC, Preset::pattern,
                Preset::new);
    }

    public static final Type<OpenPatternPayload> TYPE = new Type<>(CodexFireworks.id("open_pattern"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPatternPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, OpenPatternPayload::offhand,
            ByteBufCodecs.BOOL, OpenPatternPayload::hex,
            ByteBufCodecs.BOOL, OpenPatternPayload::textMode,
            ByteBufCodecs.VAR_INT, OpenPatternPayload::maxSparks,
            Preset.CODEC.apply(ByteBufCodecs.list(512)), OpenPatternPayload::presets,
            OpenPatternPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
