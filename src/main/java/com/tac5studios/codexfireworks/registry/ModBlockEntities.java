package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.launcher.LauncherBlockEntity;
import com.tac5studios.codexfireworks.show.ShowControllerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block entities: Launcher (rockets + settings) and Show Controller (show + linked launchers). */
public final class ModBlockEntities {

    static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CodexFireworks.MOD_ID);

    @SuppressWarnings("DataFlowIssue") // vanilla passes null here too (no data fixer)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LauncherBlockEntity>> LAUNCHER =
            TYPES.register("launcher", () -> BlockEntityType.Builder.of(LauncherBlockEntity::new, ModBlocks.LAUNCHER.get()).build(null));

    @SuppressWarnings("DataFlowIssue") // vanilla passes null here too (no data fixer)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ShowControllerBlockEntity>> SHOW_CONTROLLER =
            TYPES.register("show_controller", () -> BlockEntityType.Builder.of(ShowControllerBlockEntity::new, ModBlocks.SHOW_CONTROLLER.get()).build(null));

    private ModBlockEntities() {}

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
