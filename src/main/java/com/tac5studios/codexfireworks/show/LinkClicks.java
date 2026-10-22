package com.tac5studios.codexfireworks.show;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.launcher.LauncherBlock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Shift-right-click on a Show Controller, or on a launcher while linking, uses the block even when the
 * player holds an item. Without this, sneaking with a block in hand would place it instead of linking.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class LinkClicks {

    /** Client side: linking is on for this player (set by LinkingPayload), and when it runs out. */
    private static volatile long clientLinkingUntil;

    private static final long CLIENT_TIMEOUT_MS = 120_000;

    private LinkClicks() {}

    public static void setClientLinking(boolean active) {
        clientLinkingUntil = active ? System.currentTimeMillis() + CLIENT_TIMEOUT_MS : 0;
    }

    @SubscribeEvent
    @SuppressWarnings("unused") // called by the event bus
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        Player player = e.getEntity();
        if (!player.isShiftKeyDown() || player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) return;
        BlockState state = e.getLevel().getBlockState(e.getPos());
        boolean use;
        if (state.getBlock() instanceof ShowControllerBlock) {
            use = true;
        } else if (state.getBlock() instanceof LauncherBlock) {
            use = e.getLevel().isClientSide
                    ? System.currentTimeMillis() < clientLinkingUntil
                    : player instanceof ServerPlayer sp && Linking.active(sp);
        } else {
            use = false;
        }
        if (use) {
            e.setUseBlock(TriState.TRUE);
            e.setUseItem(TriState.FALSE);
        }
    }
}
