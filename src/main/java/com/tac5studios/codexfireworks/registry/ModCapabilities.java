package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.compat.cc.CcCompat;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Item handlers for hoppers and pipes (Launcher, Firework Workbench) and, with CC: Tweaked, the peripherals. */
public final class ModCapabilities {

    private ModCapabilities() {}

    public static void register(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.LAUNCHER.get(), (be, side) -> be.pipes());
        e.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.WORKBENCH.get(), (be, side) -> be.pipes());
        if (ModList.get().isLoaded("computercraft")) CcCompat.register(e);
    }
}
