package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.pattern.PatternCardItem;
import com.tac5studios.codexfireworks.show.ShowCardItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Block items, Pattern Card and Show Card. Rockets are normal vanilla firework rockets. */
public final class ModItems {

    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CodexFireworks.MOD_ID);

    public static final DeferredItem<BlockItem> WORKBENCH = ITEMS.registerSimpleBlockItem(ModBlocks.WORKBENCH);
    public static final DeferredItem<BlockItem> LAUNCHER = ITEMS.registerSimpleBlockItem(ModBlocks.LAUNCHER);
    public static final DeferredItem<BlockItem> SHOW_CONTROLLER = ITEMS.registerSimpleBlockItem(ModBlocks.SHOW_CONTROLLER);

    public static final DeferredItem<PatternCardItem> PATTERN_CARD = ITEMS.registerItem("pattern_card", PatternCardItem::new,
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<ShowCardItem> SHOW_CARD = ITEMS.registerItem("show_card", ShowCardItem::new,
            new Item.Properties().stacksTo(16));

    private ModItems() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
