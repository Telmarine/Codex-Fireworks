package com.tac5studios.codexfireworks.shell;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.tac5studios.codexfireworks.CodexFireworks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loads the rocket presets from data/<namespace>/rocket_presets/*.json (server side).
 * Built-in presets ship in the mod's own data; a datapack file with the same path replaces one,
 * and "enabled": false hides it. Reloaded with vanilla /reload.
 */
@EventBusSubscriber(modid = CodexFireworks.MOD_ID)
public final class RocketPresets extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static volatile Map<String, RocketPreset> presets = Collections.emptyMap();

    private RocketPresets() {
        super(GSON, "rocket_presets");
    }

    @SubscribeEvent
    @SuppressWarnings("unused") // called by the event bus
    public static void onAddReloadListeners(AddReloadListenerEvent e) {
        e.addListener(new RocketPresets());
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> files, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        Map<String, RocketPreset> out = new TreeMap<>();
        files.forEach((id, json) -> RocketPreset.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(err -> CodexFireworks.LOGGER.error("[Fireworks] Rocket preset {} is broken: {}", id, err))
                .filter(RocketPreset::enabled)
                .ifPresent(p -> out.put(key(id), p)));
        presets = Collections.unmodifiableMap(out);
        CodexFireworks.LOGGER.info("[Fireworks] {} rocket preset(s) loaded.", out.size());
    }

    /** Presets of this mod are called by their plain name ("golden_willow"); others keep the namespace. */
    private static String key(ResourceLocation id) {
        return id.getNamespace().equals(CodexFireworks.MOD_ID) ? id.getPath() : id.toString();
    }

    public static Map<String, RocketPreset> all() {
        return presets;
    }

    public static RocketPreset get(String id) {
        return presets.get(id);
    }
}
