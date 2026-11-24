package com.tac5studios.codexfireworks.compat.cc;

import com.tac5studios.codexfireworks.workbench.WorkbenchBlockEntity;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/** CC: Tweaked peripheral "firework_workbench": what it's copying and what it has in stock. */
public final class WorkbenchPeripheral implements IPeripheral {

    private final WorkbenchBlockEntity be;

    WorkbenchPeripheral(WorkbenchBlockEntity be) {
        this.be = be;
    }

    @Override
    public @NotNull String getType() {
        return "firework_workbench";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof WorkbenchPeripheral p && p.be == be;
    }

    @Override
    public Object getTarget() {
        return be;
    }

    /** Name of the template rocket, or nil. */
    @LuaFunction(mainThread = true)
    public @Nullable String getTemplate() {
        return be.template().isEmpty() ? null : be.template().getHoverName().getString();
    }

    /** Paper, gunpowder, firework stars and finished copies in the workbench. */
    @LuaFunction(mainThread = true)
    public Map<String, Integer> getStock() {
        return Map.of(
                "paper", be.stock().getItem(WorkbenchBlockEntity.PAPER).getCount(),
                "gunpowder", be.stock().getItem(WorkbenchBlockEntity.GUNPOWDER).getCount(),
                "stars", be.stock().getItem(WorkbenchBlockEntity.STARS).getCount(),
                "copies", be.stock().getItem(WorkbenchBlockEntity.OUTPUT).getCount());
    }

    /** True when it has a template, enough materials and room for another copy. */
    @LuaFunction(mainThread = true)
    public boolean canCopy() {
        return be.canCopy();
    }
}
