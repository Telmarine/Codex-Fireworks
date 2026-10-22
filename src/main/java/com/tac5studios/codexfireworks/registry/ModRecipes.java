package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.pattern.PatternCopyRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Pattern Card copy recipe (a drawn card + blank cards gives more copies). */
public final class ModRecipes {

    static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CodexFireworks.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<PatternCopyRecipe>> PATTERN_COPY =
            SERIALIZERS.register("pattern_card_copy", () -> new SimpleCraftingRecipeSerializer<>(PatternCopyRecipe::new));

    private ModRecipes() {}

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
