package com.tac5studios.codexfireworks.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.core.Mods;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.network.Network;
import com.tac5studios.codexfireworks.pattern.PatternFile;
import com.tac5studios.codexfireworks.pattern.PatternPresets;
import com.tac5studios.codexfireworks.pattern.PatternSaving;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.registry.ModComponents;
import com.tac5studios.codexfireworks.registry.ModItems;
import com.tac5studios.codexfireworks.shell.RocketFactory;
import com.tac5studios.codexfireworks.shell.RocketPreset;
import com.tac5studios.codexfireworks.shell.RocketPresets;
import com.tac5studios.codexfireworks.show.ShowControllerBlockEntity;
import com.tac5studios.codexfireworks.show.ShowNames;
import com.tac5studios.codexfireworks.show.ShowRunner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * /fireworks give rocket <id> [players] [amount]    a rocket preset (fireworks.give)
 * /fireworks give pattern <id> [players] [amount]   a Pattern Card with a ready-made pattern (fireworks.give)
 * /fireworks give mystery [players] [amount]        Mystery Rockets (fireworks.give)
 * /fireworks show start|stop <name>, show list      named shows on Show Controllers (fireworks.show.command)
 * /fireworks reload                                 messages.toml, server patterns, overlap guard (fireworks.admin)
 * Rocket and pattern presets from datapacks reload with vanilla /reload.
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
                .requires(s -> Perm.has(s, Perm.GIVE) || Perm.has(s, Perm.ADMIN) || Perm.has(s, Perm.SHOW_COMMAND));

        LiteralArgumentBuilder<CommandSourceStack> give = net.minecraft.commands.Commands.literal("give").requires(s -> Perm.has(s, Perm.GIVE));
        give.then(net.minecraft.commands.Commands.literal("rocket")
                .then(net.minecraft.commands.Commands.argument("id", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(RocketPresets.all().keySet(), b))
                        .executes(c -> giveRocket(c, List.of(c.getSource().getPlayerOrException()), 1))
                        .then(net.minecraft.commands.Commands.argument("players", EntityArgument.players())
                                .executes(c -> giveRocket(c, EntityArgument.getPlayers(c, "players"), 1))
                                .then(net.minecraft.commands.Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(c -> giveRocket(c, EntityArgument.getPlayers(c, "players"),
                                                IntegerArgumentType.getInteger(c, "amount")))))));
        give.then(net.minecraft.commands.Commands.literal("pattern")
                .then(net.minecraft.commands.Commands.argument("id", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(PatternPresets.all().keySet(), b))
                        .executes(c -> givePattern(c, List.of(c.getSource().getPlayerOrException()), 1))
                        .then(net.minecraft.commands.Commands.argument("players", EntityArgument.players())
                                .executes(c -> givePattern(c, EntityArgument.getPlayers(c, "players"), 1))
                                .then(net.minecraft.commands.Commands.argument("amount", IntegerArgumentType.integer(1, 16))
                                        .executes(c -> givePattern(c, EntityArgument.getPlayers(c, "players"),
                                                IntegerArgumentType.getInteger(c, "amount")))))));
        give.then(net.minecraft.commands.Commands.literal("mystery")
                .executes(c -> giveMystery(c, List.of(c.getSource().getPlayerOrException()), 1))
                .then(net.minecraft.commands.Commands.argument("players", EntityArgument.players())
                        .executes(c -> giveMystery(c, EntityArgument.getPlayers(c, "players"), 1))
                        .then(net.minecraft.commands.Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                .executes(c -> giveMystery(c, EntityArgument.getPlayers(c, "players"),
                                        IntegerArgumentType.getInteger(c, "amount"))))));
        root.then(give);

        root.then(net.minecraft.commands.Commands.literal("show")
                .requires(s -> Perm.has(s, Perm.SHOW_COMMAND))
                .then(net.minecraft.commands.Commands.literal("start")
                        .then(net.minecraft.commands.Commands.argument("name", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(ShowNames.get(c.getSource().getServer()).all().keySet(), b))
                                .executes(c -> showStart(c.getSource(), StringArgumentType.getString(c, "name")))))
                .then(net.minecraft.commands.Commands.literal("stop")
                        .then(net.minecraft.commands.Commands.argument("name", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(ShowNames.get(c.getSource().getServer()).all().keySet(), b))
                                .executes(c -> showStop(c.getSource(), StringArgumentType.getString(c, "name")))))
                .then(net.minecraft.commands.Commands.literal("list").executes(c -> showList(c.getSource()))));

        root.then(net.minecraft.commands.Commands.literal("reload")
                .requires(s -> Perm.has(s, Perm.ADMIN))
                .executes(Commands::reload));
        return root;
    }

    // ---- give ----

    private static int giveRocket(CommandContext<CommandSourceStack> c, Collection<ServerPlayer> players, int amount)
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
        return give(source, players, amount, preset.name(), n -> RocketFactory.rocket(preset, n));
    }

    private static int givePattern(CommandContext<CommandSourceStack> c, Collection<ServerPlayer> players, int amount)
            throws CommandSyntaxException {
        CommandSourceStack source = c.getSource();
        if (!Features.on(Feature.PATTERNS) || !Features.on(Feature.PATTERN_PRESETS)) {
            Msg.fail(source, "give.patterns_off");
            return 0;
        }
        String id = StringArgumentType.getString(c, "id");
        PatternFile.Loaded p = PatternPresets.get(id);
        if (p == null) {
            Msg.fail(source, "give.unknown_pattern", "id", id);
            return 0;
        }
        return give(source, players, amount, p.name(), n -> {
            ItemStack card = new ItemStack(ModItems.PATTERN_CARD.get(), n);
            card.set(ModComponents.PATTERN.get(), p.pattern());
            card.set(DataComponents.CUSTOM_MODEL_DATA, PatternSaving.DRAWN_MODEL);
            card.set(DataComponents.ITEM_NAME, Component.literal(p.name()));
            return card;
        });
    }

    private static int giveMystery(CommandContext<CommandSourceStack> c, Collection<ServerPlayer> players, int amount)
            throws CommandSyntaxException {
        CommandSourceStack source = c.getSource();
        if (!Features.on(Feature.MYSTERY_ROCKET)) {
            Msg.fail(source, "give.mystery_off");
            return 0;
        }
        return give(source, players, amount, "Mystery Rocket", RocketFactory::mystery);
    }

    private static int give(CommandSourceStack source, Collection<ServerPlayer> players, int amount, String what,
                            IntFunction<ItemStack> make) throws CommandSyntaxException {
        if (players.isEmpty()) throw EntityArgument.NO_PLAYERS_FOUND.create();
        for (ServerPlayer p : players) {
            ItemStack stack = make.apply(amount);
            if (!p.getInventory().add(stack)) p.drop(stack, false);
        }
        String names = players.stream().map(p -> p.getGameProfile().getName()).collect(Collectors.joining(", "));
        Msg.sendLogged(source, "give.done", "amount", amount, "rocket", what, "players", names);
        return players.size();
    }

    // ---- shows ----

    private static int showStart(CommandSourceStack source, String name) {
        GlobalPos at = ShowNames.get(source.getServer()).find(name);
        if (at == null) {
            Msg.fail(source, "show.unknown", "name", name);
            return 0;
        }
        ServerLevel level = source.getServer().getLevel(at.dimension());
        if (level == null) {
            Msg.fail(source, "show.unknown", "name", name);
            return 0;
        }
        level.getChunk(at.pos()); // load the controller's chunk if nobody is near it
        if (!(level.getBlockEntity(at.pos()) instanceof ShowControllerBlockEntity be)) {
            ShowNames.get(source.getServer()).release(at);
            Msg.fail(source, "show.unknown", "name", name);
            return 0;
        }
        String err = ShowRunner.start(level, at.pos(), be);
        if (err != null) {
            Msg.fail(source, err, "max", Limits.MAX_SHOWS_PER_AREA.get());
            return 0;
        }
        Msg.sendLogged(source, "show.started_named", "name", name);
        return 1;
    }

    private static int showStop(CommandSourceStack source, String name) {
        GlobalPos at = ShowNames.get(source.getServer()).find(name);
        if (at == null || !ShowRunner.stop(at)) {
            Msg.fail(source, "show.not_running", "name", name);
            return 0;
        }
        Msg.sendLogged(source, "show.stopped_named", "name", name);
        return 1;
    }

    private static int showList(CommandSourceStack source) {
        Map<String, GlobalPos> all = ShowNames.get(source.getServer()).all();
        if (all.isEmpty()) {
            Msg.send(source, "show.list_empty");
            return 0;
        }
        Msg.send(source, "show.list_header", "count", all.size());
        all.forEach((name, at) -> Msg.send(source, ShowRunner.running(at) ? "show.list_running" : "show.list_entry",
                "name", name, "x", at.pos().getX(), "y", at.pos().getY(), "z", at.pos().getZ(),
                "dimension", at.dimension().location().toString()));
        return all.size();
    }

    // ---- reload ----

    private static int reload(CommandContext<CommandSourceStack> c) {
        Msg.load();
        PatternPresets.loadConfig();
        Mods.detect();
        Mods.log();
        Network.sendToAll();
        Msg.sendLogged(c.getSource(), "cmd.reloaded", "presets", RocketPresets.all().size(),
                "patterns", PatternPresets.serverCount(), "guarded", Mods.guardedCount());
        return 1;
    }
}
