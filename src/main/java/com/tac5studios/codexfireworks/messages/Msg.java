package com.tac5studios.codexfireworks.messages;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.TomlFile;
import com.tac5studios.codexfireworks.util.Text;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Every player-facing line, read from config/codex_fireworks/messages.toml.
 *
 * <p>Usage:  Msg.send(source, "give.done", "rocket", name, "player", target);
 * Placeholders are given as name/value pairs and fill {name} in the text.
 * An empty message sends nothing. A missing key uses the built-in text from the jar.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class Msg {

    private static final TomlFile FILE = new TomlFile("messages.toml");

    private Msg() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SuppressWarnings("unused") // the event bus needs the parameter
    public static void onAboutToStart(ServerAboutToStartEvent e) {
        load();
    }

    /** Read messages.toml. Called at server start and by /fireworks reload. */
    public static void load() {
        FILE.load();
    }

    /** The raw text with placeholders filled, colors not applied. "" when the message is off. */
    public static String raw(String key, Object... pairs) {
        String s = FILE.str(key, key); // an unknown key shows itself, so typos are easy to spot
        if (s.isEmpty()) return s;
        s = s.replace("{prefix}", FILE.str("general.prefix", ""));
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            s = s.replace("{" + pairs[i] + "}", String.valueOf(pairs[i + 1]));
        }
        return s;
    }

    /** The message as colored text. Empty component when the message is off. */
    public static MutableComponent text(String key, Object... pairs) {
        return Text.color(raw(key, pairs));
    }

    /** True when the message has text (owners can switch a line off with ""). */
    public static boolean on(String key) {
        return !FILE.str(key, key).isEmpty();
    }

    public static void send(ServerPlayer player, String key, Object... pairs) {
        if (player != null && on(key)) player.sendSystemMessage(text(key, pairs));
    }

    /** Success line to a command source (player or console). */
    public static void send(CommandSourceStack source, String key, Object... pairs) {
        if (on(key)) source.sendSuccess(() -> text(key, pairs), false);
    }

    /** Success line that admins with command feedback also see. */
    public static void sendLogged(CommandSourceStack source, String key, Object... pairs) {
        if (on(key)) source.sendSuccess(() -> text(key, pairs), true);
    }

    /** Red failure line to a command source. */
    public static void fail(CommandSourceStack source, String key, Object... pairs) {
        if (on(key)) source.sendFailure(text(key, pairs));
    }
}
