package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.pattern.Pattern;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * Player -> server: save this picture on the Pattern Card in hand.
 *
 * @param offhand the card is in the off hand
 * @param name    card name, may be empty
 */
public record SavePatternPayload(boolean offhand, Pattern pattern, String name) implements CustomPacketPayload {

    public static final Type<SavePatternPayload> TYPE = new Type<>(CodexFireworks.id("save_pattern"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SavePatternPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SavePatternPayload::offhand,
            Pattern.STREAM_CODEC, SavePatternPayload::pattern,
            ByteBufCodecs.stringUtf8(64), SavePatternPayload::name,
            SavePatternPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
