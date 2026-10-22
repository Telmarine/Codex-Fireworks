package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.workbench.SlotLayout;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Player -> server: craft a rocket in the open Firework Workbench with this formation.
 *
 * @param containerId the workbench menu it was sent from
 * @param layouts     one per burst slot (8)
 * @param flight      1-3
 * @param name        rocket name, may be empty
 */
public record CraftRocketPayload(int containerId, List<SlotLayout> layouts, int flight, String name) implements CustomPacketPayload {

    public static final Type<CraftRocketPayload> TYPE = new Type<>(CodexFireworks.id("craft_rocket"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftRocketPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CraftRocketPayload::containerId,
            SlotLayout.STREAM_CODEC.apply(ByteBufCodecs.list(8)), CraftRocketPayload::layouts,
            ByteBufCodecs.VAR_INT, CraftRocketPayload::flight,
            ByteBufCodecs.stringUtf8(64), CraftRocketPayload::name,
            CraftRocketPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
