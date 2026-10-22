package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.CodexFireworks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The creative tab. Rocket presets aren't listed (they're server data); use /fireworks give rocket. */
public final class ModTab {

    static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CodexFireworks.MOD_ID);

    @SuppressWarnings("unused") // registered through TABS; nothing reads it
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.codex_fireworks"))
            .icon(() -> new ItemStack(ModItems.LAUNCHER.get()))
            .displayItems((params, out) -> {
                out.accept(ModItems.WORKBENCH.get());
                out.accept(ModItems.LAUNCHER.get());
                out.accept(ModItems.SHOW_CONTROLLER.get());
                out.accept(ModItems.PATTERN_CARD.get());
                out.accept(ModItems.SHOW_CARD.get());
            })
            .build());

    private ModTab() {}

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
