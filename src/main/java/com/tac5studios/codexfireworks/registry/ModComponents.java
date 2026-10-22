package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.pattern.Pattern;
import com.tac5studios.codexfireworks.show.ShowData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Data components on this mod's own items (Pattern Card, Show Card). Rockets only use vanilla components. */
public final class ModComponents {

    static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CodexFireworks.MOD_ID);

    /** The picture on a Pattern Card. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Pattern>> PATTERN =
            COMPONENTS.registerComponentType("pattern", b -> b.persistent(Pattern.CODEC).networkSynchronized(Pattern.STREAM_CODEC));

    /** The show stored on a Show Card. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ShowData>> SHOW =
            COMPONENTS.registerComponentType("show", b -> b.persistent(ShowData.CODEC).networkSynchronized(ShowData.STREAM_CODEC));

    private ModComponents() {}

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
