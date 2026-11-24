package com.tac5studios.codexfireworks.compat.create;

import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipeParams;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.copy.RocketCopy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

/**
 * Create: a Deployer holding a copyable rocket, pressing onto a plain firework rocket (paper + gunpowder),
 * turns it into a copy of the held rocket and keeps the held one. Only loaded when Create is installed.
 */
public final class CreateCompat {

    /** Above Create's own deploying recipes (50 and 100). */
    private static final int PRIORITY = 150;

    private CreateCompat() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener(CreateCompat::onDeployerSearch);
    }

    private static void onDeployerSearch(DeployerRecipeSearchEvent e) {
        if (!e.shouldAddRecipeWithPriority(PRIORITY) || !Features.on(Feature.CREATE_DEPLOYER)) return;
        ItemStack processed = e.getInventory().getItem(0);
        ItemStack held = e.getInventory().getItem(1);
        if (!RocketCopy.plain(processed) || !RocketCopy.copyable(held)) return;
        ItemStack copy = held.copyWithCount(1);
        e.addRecipe(() -> Optional.of(new RecipeHolder<>(CodexFireworks.id("deployer_rocket_copy"),
                new DeployerApplicationRecipe(new CopyParams(copy)))), PRIORITY);
    }

    /** Processed: a firework rocket; held: a firework rocket, kept; result: the held rocket's copy. */
    private static final class CopyParams extends ItemApplicationRecipeParams {
        CopyParams(ItemStack result) {
            ingredients.add(Ingredient.of(result.getItem()));
            ingredients.add(Ingredient.of(result.getItem()));
            results.add(new ProcessingOutput(result, 1f));
            keepHeldItem = true;
        }
    }
}
