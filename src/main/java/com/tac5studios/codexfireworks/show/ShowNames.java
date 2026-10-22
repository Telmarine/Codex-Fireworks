package com.tac5studios.codexfireworks.show;

import com.tac5studios.codexfireworks.CodexFireworks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Which Show Controller holds which named show, so /fireworks show start <name> can find it.
 * Saved with the world (data/codex_fireworks_shows.dat).
 */
public class ShowNames extends SavedData {

    private static final String FILE = CodexFireworks.MOD_ID + "_shows";
    private final Map<String, GlobalPos> names = new TreeMap<>();

    public static ShowNames get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new Factory<>(ShowNames::new, ShowNames::load), FILE);
    }

    private static ShowNames load(CompoundTag tag, HolderLookup.Provider registries) {
        ShowNames n = new ShowNames();
        ListTag list = tag.getList("Shows", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag e = list.getCompound(i);
            ResourceLocation dim = ResourceLocation.tryParse(e.getString("Dim"));
            if (dim == null) continue;
            n.names.put(e.getString("Name"), GlobalPos.of(ResourceKey.create(Registries.DIMENSION, dim), BlockPos.of(e.getLong("Pos"))));
        }
        return n;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        names.forEach((name, pos) -> {
            CompoundTag e = new CompoundTag();
            e.putString("Name", name);
            e.putString("Dim", pos.dimension().location().toString());
            e.putLong("Pos", pos.pos().asLong());
            list.add(e);
        });
        tag.put("Shows", list);
        return tag;
    }

    @Nullable
    public GlobalPos find(String name) {
        return names.get(name);
    }

    /** The name that this controller is registered under, or null. */
    @Nullable
    public String nameAt(GlobalPos pos) {
        for (Map.Entry<String, GlobalPos> e : names.entrySet()) if (e.getValue().equals(pos)) return e.getKey();
        return null;
    }

    /** Point a name at a controller (and drop the controller's old name). False when another controller has it. */
    public boolean claim(String name, GlobalPos pos) {
        GlobalPos other = names.get(name);
        if (other != null && !other.equals(pos)) return false;
        release(pos);
        if (!name.isEmpty()) names.put(name, pos);
        setDirty();
        return true;
    }

    public void release(GlobalPos pos) {
        if (names.values().removeIf(p -> p.equals(pos))) setDirty();
    }

    public Map<String, GlobalPos> all() {
        return Collections.unmodifiableMap(names);
    }
}
