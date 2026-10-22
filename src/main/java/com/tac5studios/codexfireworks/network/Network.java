package com.tac5studios.codexfireworks.network;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.client.ClientHooks;
import com.tac5studios.codexfireworks.client.ClientSettings;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.pattern.PatternSaving;
import com.tac5studios.codexfireworks.shell.Shape;
import com.tac5studios.codexfireworks.show.LinkClicks;
import com.tac5studios.codexfireworks.show.ShowActions;
import com.tac5studios.codexfireworks.workbench.WorkbenchCrafting;
import com.tac5studios.codexfireworks.workbench.WorkbenchMenu;
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

/**
 * All packets of this mod.
 * Server -> player: drawing switches, open the pattern screen, open/refresh the show screen.
 * Player -> server: craft in the workbench, save a pattern, save a show, show buttons.
 */
@SuppressWarnings("unused") // nested classes are found by @EventBusSubscriber
public final class Network {

    private Network() {}

    @EventBusSubscriber(modid = CodexFireworks.MOD_ID)
    public static final class ModEvents {

        private ModEvents() {}

        @SubscribeEvent
        @SuppressWarnings("unused") // called by the event bus
        public static void onRegisterPayloads(RegisterPayloadHandlersEvent e) {
            PayloadRegistrar r = e.registrar("3");
            r.playToClient(SettingsPayload.TYPE, SettingsPayload.CODEC,
                    (payload, ctx) -> ctx.enqueueWork(() -> ClientSettings.apply(payload)));
            r.playToClient(OpenPatternPayload.TYPE, OpenPatternPayload.CODEC,
                    (payload, ctx) -> ctx.enqueueWork(() -> ClientHooks.openPattern(payload)));
            r.playToClient(LinkingPayload.TYPE, LinkingPayload.CODEC,
                    (payload, ctx) -> ctx.enqueueWork(() -> LinkClicks.setClientLinking(payload.active())));
            r.playToClient(OpenShowPayload.TYPE, OpenShowPayload.CODEC,
                    (payload, ctx) -> ctx.enqueueWork(() -> ClientHooks.openShow(payload)));

            r.playToServer(CraftRocketPayload.TYPE, CraftRocketPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer sp && sp.containerMenu instanceof WorkbenchMenu m && m.containerId == payload.containerId()) {
                    WorkbenchCrafting.craftRocket(sp, m, payload.layouts(), payload.flight(), payload.name());
                }
            }));
            r.playToServer(CraftPresetPayload.TYPE, CraftPresetPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer sp && sp.containerMenu instanceof WorkbenchMenu m && m.containerId == payload.containerId()) {
                    WorkbenchCrafting.craftPreset(sp, m, payload.id());
                }
            }));
            r.playToServer(SavePatternPayload.TYPE, SavePatternPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer sp) PatternSaving.save(sp, payload);
            }));
            r.playToServer(SaveShowPayload.TYPE, SaveShowPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer sp) ShowActions.save(sp, payload);
            }));
            r.playToServer(ShowActionPayload.TYPE, ShowActionPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> {
                if (ctx.player() instanceof ServerPlayer sp) ShowActions.action(sp, payload);
            }));
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
