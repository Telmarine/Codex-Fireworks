package com.tac5studios.codexfireworks.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.core.Mods;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.network.Network;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.shell.RocketFactory;
import com.tac5studios.codexfireworks.shell.RocketPreset;
import com.tac5studios.codexfireworks.shell.RocketPresets;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /fireworks give rocket <id> [players] [amount]   hand out a rocket preset (fireworks.give)
 * /fireworks reload                                re-read messages.toml, re-check the overlap guard (fireworks.admin)
 * Rocket presets themselves reload with vanilla /reload.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class Commands {

    private Commands() {}

    @SubscribeEvent
    @SuppressWarnings("unused") // called by the event bus
    public static void onRegister(RegisterCommandsEvent e) {
        e.getDispatcher().register(build());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> root = net.minecraft.commands.Commands.literal("fireworks")
                .requires(s -> Perm.has(s, Perm.GIVE) || Perm.has(s, Perm.ADMIN));

        root.then(net.minecraft.commands.Commands.literal("give")
                .requires(s -> Perm.has(s, Perm.GIVE))
                .then(net.minecraft.commands.Commands.literal("rocket")
                        .then(net.minecraft.commands.Commands.argument("id", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(RocketPresets.all().keySet(), b))
                                .executes(c -> give(c, List.of(c.getSource().getPlayerOrException()), 1))
                                .then(net.minecraft.commands.Commands.argument("players", EntityArgument.players())
                                        .executes(c -> give(c, EntityArgument.getPlayers(c, "players"), 1))
                                        .then(net.minecraft.commands.Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(c -> give(c, EntityArgument.getPlayers(c, "players"),
                                                        IntegerArgumentType.getInteger(c, "amount"))))))));

        root.then(net.minecraft.commands.Commands.literal("reload")
                .requires(s -> Perm.has(s, Perm.ADMIN))
                .executes(Commands::reload));
        return root;
    }

    private static int give(CommandContext<CommandSourceStack> c, Collection<ServerPlayer> players, int amount)
            throws CommandSyntaxException {
        CommandSourceStack source = c.getSource();
        if (!Features.enabled()) {
            Msg.fail(source, "cmd.off");
            return 0;
        }
        if (!Features.on(Feature.ROCKET_PRESETS)) {
            Msg.fail(source, "give.presets_off");
            return 0;
        }
        String id = StringArgumentType.getString(c, "id");
        RocketPreset preset = RocketPresets.get(id);
        if (preset == null) {
            Msg.fail(source, "give.unknown", "id", id);
            return 0;
        }
        String off = RocketFactory.disabledShape(preset);
        if (off != null) {
            Msg.fail(source, "give.shape_off", "rocket", preset.name(), "shape", off);
            return 0;
        }
        if (players.isEmpty()) throw EntityArgument.NO_PLAYERS_FOUND.create();
        for (ServerPlayer p : players) {
            ItemStack stack = RocketFactory.rocket(preset, amount);
            if (!p.getInventory().add(stack)) p.drop(stack, false);
        }
        String names = players.stream().map(p -> p.getGameProfile().getName()).collect(Collectors.joining(", "));
        Msg.sendLogged(source, "give.done", "amount", amount, "rocket", preset.name(), "players", names);
        return players.size();
    }

    private static int reload(CommandContext<CommandSourceStack> c) {
        Msg.load();
        Mods.detect();
        Mods.log();
        Network.sendToAll();
        Msg.sendLogged(c.getSource(), "cmd.reloaded", "presets", RocketPresets.all().size(), "guarded", Mods.guardedCount());
        return 1;
    }
}
