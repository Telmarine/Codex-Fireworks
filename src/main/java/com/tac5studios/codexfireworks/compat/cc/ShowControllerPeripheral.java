package com.tac5studios.codexfireworks.compat.cc;

import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.messages.Msg;
import com.tac5studios.codexfireworks.show.ShowControllerBlockEntity;
import com.tac5studios.codexfireworks.show.ShowRunner;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** CC: Tweaked peripheral "show_controller". */
@SuppressWarnings("resource") // the level belongs to the server, it is never closed here
public final class ShowControllerPeripheral implements IPeripheral {

    private final ShowControllerBlockEntity be;

    ShowControllerPeripheral(ShowControllerBlockEntity be) {
        this.be = be;
    }

    @Override
    public @NotNull String getType() {
        return "show_controller";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof ShowControllerPeripheral p && p.be == be;
    }

    @Override
    public Object getTarget() {
        return be;
    }

    private ServerLevel level() throws LuaException {
        if (!(be.getLevel() instanceof ServerLevel level)) throw new LuaException("Show Controller is not loaded");
        return level;
    }

    /** Start the show (from the start if it's already running). */
    @LuaFunction(mainThread = true)
    public void startShow() throws LuaException {
        String err = ShowRunner.start(level(), be.getBlockPos(), be);
        if (err != null) throw new LuaException(Msg.text(err, "max", Limits.MAX_SHOWS_PER_AREA.get()).getString());
    }

    /** Stop the show. Returns false when it wasn't running. */
    @LuaFunction(mainThread = true)
    public boolean stopShow() throws LuaException {
        return ShowRunner.stop(GlobalPos.of(level().dimension(), be.getBlockPos()));
    }

    @LuaFunction(mainThread = true)
    public boolean isRunning() throws LuaException {
        return ShowRunner.running(GlobalPos.of(level().dimension(), be.getBlockPos()));
    }

    /** The show's name ("" when it has none). */
    @LuaFunction(mainThread = true)
    public String getName() {
        return be.show().name();
    }

    /** Number of cues and the show length in ticks. */
    @LuaFunction(mainThread = true)
    public Map<String, Object> getShow() {
        return Map.of("name", be.show().name(), "cues", be.show().cues().size(), "length", be.show().length(),
                "loop", be.show().loop());
    }

    /** Linked launchers in order (launcher 1 first), as {x, y, z}. */
    @LuaFunction(mainThread = true)
    public List<Map<String, Integer>> getLaunchers() {
        List<Map<String, Integer>> out = new ArrayList<>();
        for (BlockPos p : be.launchers()) out.add(Map.of("x", p.getX(), "y", p.getY(), "z", p.getZ()));
        return out;
    }
}
