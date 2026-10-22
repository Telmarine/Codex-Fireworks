package com.tac5studios.codexfireworks.show;

import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.launcher.LauncherBlockEntity;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.network.LinkingPayload;
import com.tac5studios.codexfireworks.protect.Protection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Linking launchers to a Show Controller: shift-right-click the controller (or press Link in its screen) to start,
 * shift-right-click launchers to add or remove them, then shift-right-click the controller again to finish.
 * Linking ends by itself after 2 minutes.
 */
@SuppressWarnings("resource") // ServerLevel is AutoCloseable, but these levels belong to the server and must not be closed
public final class Linking {

    private record Session(GlobalPos controller, long until) {}

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final long TIMEOUT = 20L * 120;

    private Linking() {}

    public static boolean active(ServerPlayer player) {
        Session s = SESSIONS.get(player.getUUID());
        if (s == null) return false;
        if (player.serverLevel().getGameTime() > s.until()) {
            SESSIONS.remove(player.getUUID());
            return false;
        }
        return true;
    }

    /** Start linking for this controller, or finish when it's already the one being linked. */
    public static void startOrFinish(ServerPlayer player, ShowControllerBlockEntity controller) {
        GlobalPos here = GlobalPos.of(player.serverLevel().dimension(), controller.getBlockPos());
        Session s = SESSIONS.get(player.getUUID());
        if (s != null && s.controller().equals(here) && active(player)) {
            SESSIONS.remove(player.getUUID());
            PacketDistributor.sendToPlayer(player, new LinkingPayload(false));
            Msg.send(player, "link.done", "count", controller.launchers().size());
            return;
        }
        start(player, controller);
    }

    public static void start(ServerPlayer player, ShowControllerBlockEntity controller) {
        GlobalPos here = GlobalPos.of(player.serverLevel().dimension(), controller.getBlockPos());
        SESSIONS.put(player.getUUID(), new Session(here, player.serverLevel().getGameTime() + TIMEOUT));
        PacketDistributor.sendToPlayer(player, new LinkingPayload(true));
        Msg.send(player, "link.start", "range", Limits.LINK_RANGE.get());
    }

    public static void toggleLauncher(ServerPlayer player, LauncherBlockEntity launcher) {
        Session s = SESSIONS.get(player.getUUID());
        if (s == null) return;
        ServerLevel level = player.serverLevel();
        BlockPos lp = launcher.getBlockPos();
        if (!s.controller().dimension().equals(level.dimension())) {
            Msg.send(player, "link.other_dimension");
            return;
        }
        if (!(level.getBlockEntity(s.controller().pos()) instanceof ShowControllerBlockEntity controller)) {
            SESSIONS.remove(player.getUUID());
            PacketDistributor.sendToPlayer(player, new LinkingPayload(false));
            Msg.send(player, "link.controller_gone");
            return;
        }
        if (!Protection.canEdit(player, launcher.owner(), level, lp) || !Protection.canEdit(player, controller.owner(), level, controller.getBlockPos())) {
            Msg.send(player, "general.not_yours");
            return;
        }
        int range = Limits.LINK_RANGE.get();
        if (lp.distSqr(controller.getBlockPos()) > (double) range * range) {
            Msg.send(player, "link.too_far", "range", range);
            return;
        }
        boolean linking = !controller.launchers().contains(lp);
        if (linking && controller.launchers().size() >= Limits.MAX_LAUNCHERS.get()) {
            Msg.send(player, "link.too_many", "max", Limits.MAX_LAUNCHERS.get());
            return;
        }
        boolean now = controller.toggle(lp);
        Msg.send(player, now ? "link.added" : "link.removed", "number", controller.launchers().indexOf(lp) + 1,
                "count", controller.launchers().size());
        SESSIONS.put(player.getUUID(), new Session(s.controller(), level.getGameTime() + TIMEOUT));
        PacketDistributor.sendToPlayer(player, new LinkingPayload(true)); // the client's timer starts over too
    }
}
