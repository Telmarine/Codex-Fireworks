package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.copy.RocketCopyRecipe;
import com.tac5studios.codexfireworks.pattern.PatternCopyRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The Pattern Card copy recipe (a drawn card + blank cards gives more copies) and the rocket copy recipe. */
public final class ModRecipes {

    static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CodexFireworks.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<PatternCopyRecipe>> PATTERN_COPY =
            SERIALIZERS.register("pattern_card_copy", () -> new SimpleCraftingRecipeSerializer<>(PatternCopyRecipe::new));

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RocketCopyRecipe>> ROCKET_COPY =
            SERIALIZERS.register("rocket_copy", () -> new SimpleCraftingRecipeSerializer<>(RocketCopyRecipe::new));

    private ModRecipes() {}

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }
}
