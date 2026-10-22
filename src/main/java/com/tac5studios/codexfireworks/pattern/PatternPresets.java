package com.tac5studios.codexfireworks.pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tac5studios.codexfireworks.CodexFireworks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Ready-made patterns, from two places:
 *   data/<namespace>/pattern_presets/*.json   built-in ones ship in the mod; datapacks can add or replace them (/reload)
 *   config/codex_fireworks/patterns/*.json    the server's own patterns (/fireworks reload)
 * A server file with the same id as a datapack one wins.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class PatternPresets extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static volatile Map<String, PatternFile.Loaded> fromData = Collections.emptyMap();
    private static volatile Map<String, PatternFile.Loaded> fromConfig = Collections.emptyMap();
    private static volatile Map<String, PatternFile.Loaded> all = Collections.emptyMap();

    private PatternPresets() {
        super(GSON, "pattern_presets");
    }

    @SubscribeEvent
    @SuppressWarnings("unused") // called by the event bus
    public static void onAddReloadListeners(AddReloadListenerEvent e) {
        e.addListener(new PatternPresets());
    }

    @SubscribeEvent
    @SuppressWarnings("unused") // the event bus needs the parameter
    public static void onAboutToStart(ServerAboutToStartEvent e) {
        loadConfig();
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> files, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        Map<String, PatternFile.Loaded> out = new TreeMap<>();
        files.forEach((id, json) -> {
            try {
                out.put(id.getNamespace().equals(CodexFireworks.MOD_ID) ? id.getPath() : id.toString(), PatternFile.read(json));
            } catch (RuntimeException ex) {
                CodexFireworks.LOGGER.error("[Fireworks] Pattern preset {} is broken: {}", id, ex.getMessage());
            }
        });
        fromData = Collections.unmodifiableMap(out);
        merge();
    }

    /** Read config/codex_fireworks/patterns/. Called at server start and by /fireworks reload. */
    public static void loadConfig() {
        Path dir = FMLPaths.CONFIGDIR.get().resolve(CodexFireworks.MOD_ID).resolve("patterns");
        Map<String, PatternFile.Loaded> out = new TreeMap<>();
        try {
            Files.createDirectories(dir);
            try (Stream<Path> list = Files.list(dir)) {
                for (Path p : list.filter(f -> f.toString().endsWith(".json")).sorted().toList()) {
                    String id = p.getFileName().toString();
                    id = id.substring(0, id.length() - 5).toLowerCase(Locale.ROOT).replace(' ', '_');
                    try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                        out.put(id, PatternFile.read(JsonParser.parseReader(r)));
                    } catch (Exception ex) {
                        CodexFireworks.LOGGER.error("[Fireworks] patterns/{} is broken: {}", p.getFileName(), ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            CodexFireworks.LOGGER.error("[Fireworks] Could not read {}.", dir, ex);
        }
        fromConfig = Collections.unmodifiableMap(out);
        merge();
    }

    private static void merge() {
        Map<String, PatternFile.Loaded> m = new TreeMap<>(fromData);
        m.putAll(fromConfig);
        all = Collections.unmodifiableMap(m);
    }

    public static Map<String, PatternFile.Loaded> all() {
        return all;
    }

    public static int serverCount() {
        return fromConfig.size();
    }

    public static PatternFile.Loaded get(String id) {
        return all.get(id);
    }
}
