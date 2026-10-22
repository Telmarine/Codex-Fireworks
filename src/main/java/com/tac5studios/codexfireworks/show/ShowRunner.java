package com.tac5studios.codexfireworks.show;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.launcher.LauncherBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Runs the shows that are playing. Each tick, the cues whose time has come fire their launcher.
 * A show whose controller is in an unloaded chunk waits until it's loaded again.
 * A looping show stops once all its launchers are empty. Shows don't keep running through a server restart.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class ShowRunner {

    private static final class Running {
        ShowData show;
        int tick;

        Running(ShowData show) {
            this.show = show;
        }
    }

    private static final Map<GlobalPos, Running> RUNNING = new HashMap<>();

    private ShowRunner() {}

    /** Start the show on this controller. Returns a messages.toml key when it can't start, or null. */
    public static String start(ServerLevel level, BlockPos pos, ShowControllerBlockEntity be) {
        if (!Features.on(Feature.SHOWS)) return "show.off";
        if (be.show().cues().isEmpty()) return "show.no_cues";
        if (be.launchers().isEmpty()) return "show.no_launchers";
        GlobalPos key = GlobalPos.of(level.dimension(), pos);
        if (!RUNNING.containsKey(key)) {
            int near = 0;
            for (GlobalPos other : RUNNING.keySet()) {
                if (other.dimension() == level.dimension() && other.pos().distSqr(pos) <= 128 * 128) near++;
            }
            if (near >= Limits.MAX_SHOWS_PER_AREA.get()) return "show.too_many";
        }
        RUNNING.put(key, new Running(be.show()));
        return null;
    }

    public static boolean stop(GlobalPos key) {
        return RUNNING.remove(key) != null;
    }

    public static boolean running(GlobalPos key) {
        return RUNNING.containsKey(key);
    }

    /** The show on a running controller was edited: keep playing with the new cues. */
    public static void update(GlobalPos key, ShowData show) {
        Running r = RUNNING.get(key);
        if (r != null) r.show = show;
    }

    @SubscribeEvent
    @SuppressWarnings("unused") // called by the event bus
    public static void onTick(ServerTickEvent.Post e) {
        if (RUNNING.isEmpty()) return;
        MinecraftServer server = e.getServer();
        Iterator<Map.Entry<GlobalPos, Running>> it = RUNNING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<GlobalPos, Running> entry = it.next();
            GlobalPos key = entry.getKey();
            Running r = entry.getValue();
            ServerLevel level = server.getLevel(key.dimension());
            if (level == null || !Features.on(Feature.SHOWS)) {
                it.remove();
                continue;
            }
            if (!level.isLoaded(key.pos())) continue; // wait for the chunk
            if (!(level.getBlockEntity(key.pos()) instanceof ShowControllerBlockEntity be)) {
                it.remove();
                continue;
            }
            List<BlockPos> launchers = be.launchers();
            for (Cue c : r.show.cues()) {
                if (c.time() == r.tick) fire(level, launchers, c);
                else if (c.time() > r.tick) break;
            }
            r.tick++;
            if (r.tick > r.show.length()) {
                // A looping show ends by itself once every linked launcher is out of rockets.
                if (r.show.loop() && !outOfRockets(level, launchers)) r.tick = 0;
                else it.remove();
            }
        }
    }

    /** True when every linked launcher is loaded and empty (an unloaded one might still have rockets). */
    private static boolean outOfRockets(ServerLevel level, List<BlockPos> launchers) {
        for (BlockPos p : launchers) {
            if (!level.isLoaded(p)) return false;
            if (level.getBlockEntity(p) instanceof LauncherBlockEntity l && !l.items().isEmpty()) return false;
        }
        return true;
    }

    /** Fire one cue right away. */
    public static void fire(ServerLevel level, List<BlockPos> launchers, Cue c) {
        if (c.launcher() == 0) {
            for (BlockPos p : launchers) fireAt(level, p, c.slot());
        } else if (c.launcher() <= launchers.size()) {
            fireAt(level, launchers.get(c.launcher() - 1), c.slot());
        }
    }

    private static void fireAt(ServerLevel level, BlockPos pos, int slot) {
        if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof LauncherBlockEntity l) l.fire(slot - 1, 0);
    }

    @SubscribeEvent
    @SuppressWarnings("unused") // the event bus needs the parameter
    public static void onStopping(ServerStoppingEvent e) {
        RUNNING.clear();
    }
}
