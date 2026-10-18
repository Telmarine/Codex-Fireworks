package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.client.ClientSettings;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.shell.Shape;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;

/** Sends the server's drawing switches to each player when they join and whenever they change. */
@SuppressWarnings("unused") // nested classes are found by @EventBusSubscriber
public final class Network {

    private Network() {}

    @EventBusSubscriber(modid = CodexFireworks.MOD_ID)
    public static final class ModEvents {

        private ModEvents() {}

        @SubscribeEvent
        @SuppressWarnings("unused") // called by the event bus
        public static void onRegisterPayloads(RegisterPayloadHandlersEvent e) {
            PayloadRegistrar registrar = e.registrar("1");
            registrar.playToClient(SettingsPayload.TYPE, SettingsPayload.CODEC,
                    (payload, ctx) -> ctx.enqueueWork(() -> ClientSettings.apply(payload)));
        }

        @SubscribeEvent
        @SuppressWarnings("unused") // called by the event bus
        public static void onConfigReloaded(ModConfigEvent.Reloading e) {
            if (e.getConfig().getSpec() == Features.SPEC || e.getConfig().getSpec() == Limits.SPEC) sendToAll();
        }
    }

    @EventBusSubscriber(modid = CodexFireworks.MOD_ID)
    public static final class GameEvents {

        private GameEvents() {}

        @SubscribeEvent
        @SuppressWarnings("unused") // called by the event bus
        public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
            if (e.getEntity() instanceof ServerPlayer player) PacketDistributor.sendToPlayer(player, current());
        }
    }

    /** The current switches as a payload. */
    public static SettingsPayload current() {
        List<String> off = new ArrayList<>();
        for (Shape s : Shape.values()) if (!Features.on(s.feature)) off.add(s.id);
        return new SettingsPayload(Features.enabled(), off, Features.on(Feature.SHAPE_SOUNDS),
                Limits.BURST_SCALE.get().floatValue(), Limits.SPARK_SCALE.get().floatValue());
    }

    /** Send the switches to everyone online (after a reload or a config change). */
    public static void sendToAll() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        server.execute(() -> PacketDistributor.sendToAllPlayers(current()));
    }
}
