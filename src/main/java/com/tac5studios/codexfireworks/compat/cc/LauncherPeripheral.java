package com.tac5studios.codexfireworks.compat.cc;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.launcher.LauncherBlockEntity;
import com.tac5studios.codexfireworks.messages.Msg;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** CC: Tweaked peripheral "firework_launcher". Slots are 1-9 like the cues in a show. */
public final class LauncherPeripheral implements IPeripheral {

    private final LauncherBlockEntity be;

    LauncherPeripheral(LauncherBlockEntity be) {
        this.be = be;
    }

    @Override
    public @NotNull String getType() {
        return "firework_launcher";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof LauncherPeripheral p && p.be == be;
    }

    @Override
    public Object getTarget() {
        return be;
    }

    private void check() throws LuaException {
        if (!Features.on(Feature.LAUNCHERS)) throw new LuaException(Msg.text("launcher.off").getString());
    }

    /** Fire one rocket: from the given slot (1-9), or the next rocket in turn. */
    @LuaFunction(mainThread = true)
    public void fire(IArguments args) throws LuaException {
        check();
        int s = args.optInt(0, 0);
        if (s < 0 || s > LauncherBlockEntity.SLOTS) throw new LuaException("Slot must be 1-" + LauncherBlockEntity.SLOTS);
        be.fire(s - 1, 0);
    }

    /** Fire a volley, like a redstone pulse. */
    @LuaFunction(mainThread = true)
    public void volley() throws LuaException {
        check();
        be.volley();
    }

    /** Each filled or remembered slot: name, count and the name of the rocket it remembers. */
    @LuaFunction(mainThread = true)
    public Map<Integer, Map<String, Object>> getRockets() {
        Map<Integer, Map<String, Object>> out = new HashMap<>();
        for (int i = 0; i < LauncherBlockEntity.SLOTS; i++) {
            ItemStack s = be.items().getItem(i);
            ItemStack mem = be.remembered(i);
            if (s.isEmpty() && mem.isEmpty()) continue;
            Map<String, Object> slot = new HashMap<>();
            slot.put("count", s.getCount());
            if (!s.isEmpty()) slot.put("name", s.getHoverName().getString());
            if (!mem.isEmpty()) slot.put("remembers", mem.getHoverName().getString());
            out.put(i + 1, slot);
        }
        return out;
    }

    @LuaFunction(mainThread = true)
    public int getRocketCount() {
        return be.rocketCount();
    }

    /** Make the empty slots forget their rocket. */
    @LuaFunction(mainThread = true)
    public void clearSlotMemory() {
        be.clearSlotMemory();
    }

    @LuaFunction(mainThread = true)
    public Map<String, Integer> getSettings() {
        return Map.of(
                "direction", be.setting(LauncherBlockEntity.YAW),
                "tilt", be.setting(LauncherBlockEntity.ANGLE),
                "spread", be.setting(LauncherBlockEntity.SPREAD),
                "count", be.setting(LauncherBlockEntity.COUNT),
                "interval", be.setting(LauncherBlockEntity.INTERVAL));
    }

    /** Direction in degrees: 0 south, 90 west, 180 north, 270 east. */
    @LuaFunction(mainThread = true)
    public void setDirection(int degrees) {
        be.setSetting(LauncherBlockEntity.YAW, degrees);
    }

    /** Tilt from straight up, 0-60 degrees. */
    @LuaFunction(mainThread = true)
    public void setTilt(int degrees) {
        be.setSetting(LauncherBlockEntity.ANGLE, degrees);
    }

    /** Random spread, 0-30 degrees. */
    @LuaFunction(mainThread = true)
    public void setSpread(int degrees) {
        be.setSetting(LauncherBlockEntity.SPREAD, degrees);
    }

    /** Rockets per volley, 1-9. */
    @LuaFunction(mainThread = true)
    public void setCount(int rockets) {
        be.setSetting(LauncherBlockEntity.COUNT, rockets);
    }

    /** Ticks between the rockets of a volley (server minimum up to 100). */
    @LuaFunction(mainThread = true)
    public void setInterval(int ticks) {
        be.setSetting(LauncherBlockEntity.INTERVAL, ticks);
    }
}
