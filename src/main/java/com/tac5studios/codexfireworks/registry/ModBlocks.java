package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.launcher.LauncherBlock;
import com.tac5studios.codexfireworks.show.ShowControllerBlock;
import com.tac5studios.codexfireworks.workbench.WorkbenchBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The three blocks: Firework Workbench, Launcher and Show Controller. */
public final class ModBlocks {

    static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CodexFireworks.MOD_ID);

    public static final DeferredBlock<WorkbenchBlock> WORKBENCH = BLOCKS.registerBlock("firework_workbench", WorkbenchBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5f).sound(SoundType.WOOD).ignitedByLava());

    public static final DeferredBlock<LauncherBlock> LAUNCHER = BLOCKS.registerBlock("launcher", LauncherBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f).sound(SoundType.METAL));

    public static final DeferredBlock<ShowControllerBlock> SHOW_CONTROLLER = BLOCKS.registerBlock("show_controller", ShowControllerBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3.0f).sound(SoundType.METAL));

    private ModBlocks() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
