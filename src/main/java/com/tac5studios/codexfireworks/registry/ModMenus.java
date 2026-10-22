package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.launcher.LauncherMenu;
import com.tac5studios.codexfireworks.workbench.WorkbenchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Menus with item slots: Firework Workbench and Launcher. */
public final class ModMenus {

    static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CodexFireworks.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<WorkbenchMenu>> WORKBENCH =
            MENUS.register("firework_workbench", () -> IMenuTypeExtension.create(WorkbenchMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<LauncherMenu>> LAUNCHER =
            MENUS.register("launcher", () -> IMenuTypeExtension.create(LauncherMenu::new));

    private ModMenus() {}

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
