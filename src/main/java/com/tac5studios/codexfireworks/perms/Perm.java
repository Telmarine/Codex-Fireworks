package com.tac5studios.codexfireworks.perms;

import com.tac5studios.codexfireworks.CodexFireworks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Permission nodes, named "fireworks.<path>". They go through the NeoForge PermissionAPI, so they work
 * with any permission mod or plain OP levels.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class Perm {

    public enum Who {
        PLAYER(0), STAFF(2), ADMIN(3);

        final int opLevel;

        Who(int opLevel) {
            this.opLevel = opLevel;
        }
    }

    private record Entry(PermissionNode<Boolean> node, Who who) {}

    private static final Map<String, Entry> NODES = new LinkedHashMap<>();

    /** Firework Workbench and Pattern Cards. */
    public static final String DESIGN = node("design", Who.PLAYER);
    /** Use launchers. */
    public static final String LAUNCHER_USE = node("launcher.use", Who.PLAYER);
    /** Edit shows on a Show Controller. */
    public static final String SHOW_EDIT = node("show.edit", Who.PLAYER);
    /** Custom hex colors in patterns (when hex_for_everyone is off). */
    public static final String PATTERN_HEX = node("pattern.hex", Who.STAFF);
    /** /fireworks give rocket|pattern */
    public static final String GIVE = node("give", Who.STAFF);
    /** /fireworks show start|stop|list */
    public static final String SHOW_COMMAND = node("show.command", Who.STAFF);
    /** Edit anyone's launchers and controllers. */
    public static final String BYPASS = node("bypass", Who.STAFF);
    /** /fireworks reload */
    public static final String ADMIN = node("admin", Who.ADMIN);

    private Perm() {}

    private static String node(String path, Who who) {
        PermissionNode<Boolean> n = new PermissionNode<>("fireworks", path, PermissionTypes.BOOLEAN,
                (player, uuid, ctx) -> player != null && player.hasPermissions(who.opLevel));
        n.setInformation(Component.literal("fireworks." + path), Component.literal("Default: " + switch (who) {
            case PLAYER -> "everyone";
            case STAFF -> "OP level 2+";
            case ADMIN -> "OP level 3+";
        }));
        NODES.put(n.getNodeName(), new Entry(n, who));
        return n.getNodeName();
    }

    @SubscribeEvent
    @SuppressWarnings("unused") // called by the event bus
    public static void onGatherNodes(PermissionGatherEvent.Nodes event) {
        event.addNodes(NODES.values().stream().map(Entry::node).toArray(PermissionNode<?>[]::new));
        CodexFireworks.LOGGER.info("[Fireworks] {} permission nodes registered.", NODES.size());
    }

    public static boolean has(CommandSourceStack source, String node) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            Entry e = NODES.get(node);
            Who who = e == null ? Who.ADMIN : e.who();
            return source.hasPermission(Math.max(who.opLevel, 2));
        }
        return has(player, node);
    }

    public static boolean has(ServerPlayer player, String node) {
        Entry e = NODES.get(node);
        if (e == null) return false;
        try {
            return PermissionAPI.getPermission(player, e.node());
        } catch (RuntimeException ex) {
            CodexFireworks.LOGGER.warn("[Fireworks] Permission check failed for {}: {}", node, ex.toString());
            return player.hasPermissions(e.who().opLevel);
        }
    }
}
