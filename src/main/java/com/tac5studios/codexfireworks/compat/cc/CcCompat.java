package com.tac5studios.codexfireworks.compat.cc;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.registry.ModBlockEntities;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** CC: Tweaked peripherals. Only loaded when CC: Tweaked is installed. */
public final class CcCompat {

    private CcCompat() {}

    public static void register(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(PeripheralCapability.get(), ModBlockEntities.LAUNCHER.get(),
                (be, side) -> Features.on(Feature.COMPUTERCRAFT) ? new LauncherPeripheral(be) : null);
        e.registerBlockEntity(PeripheralCapability.get(), ModBlockEntities.SHOW_CONTROLLER.get(),
                (be, side) -> Features.on(Feature.COMPUTERCRAFT) ? new ShowControllerPeripheral(be) : null);
        e.registerBlockEntity(PeripheralCapability.get(), ModBlockEntities.WORKBENCH.get(),
                (be, side) -> Features.on(Feature.COMPUTERCRAFT) ? new WorkbenchPeripheral(be) : null);
    }
}
