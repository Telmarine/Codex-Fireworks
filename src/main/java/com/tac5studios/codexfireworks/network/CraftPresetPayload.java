package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/** Player -> server: make this rocket preset in the open Firework Workbench. */
public record CraftPresetPayload(int containerId, String id) implements CustomPacketPayload {

    public static final Type<CraftPresetPayload> TYPE = new Type<>(CodexFireworks.id("craft_preset"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftPresetPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CraftPresetPayload::containerId,
            ByteBufCodecs.stringUtf8(256), CraftPresetPayload::id,
            CraftPresetPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
