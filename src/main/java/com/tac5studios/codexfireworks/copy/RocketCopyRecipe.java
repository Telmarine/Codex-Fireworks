package com.tac5studios.codexfireworks.copy;

import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * One rocket plus paper, gunpowder and firework stars (amounts in limits.toml [copying]) gives two of that rocket:
 * the original back and one copy. The original goes into the result, so it also works in machines that
 * don't hand back items left in the grid.
 */
public class RocketCopyRecipe extends CustomRecipe {

    public RocketCopyRecipe(CraftingBookCategory category) {
        super(category);
    }

    /** Index of the rocket, or -1 when the grid doesn't match. */
    private static int scan(CraftingInput input) {
        int rocket = -1, paper = 0, gunpowder = 0, stars = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(Items.FIREWORK_ROCKET)) {
                if (rocket >= 0) return -1;
                rocket = i;
            } else if (s.is(Items.PAPER)) {
                paper++;
            } else if (s.is(Items.GUNPOWDER)) {
                gunpowder++;
            } else if (s.is(Items.FIREWORK_STAR)) {
                stars++;
            } else {
                return -1;
            }
        }
        if (rocket < 0 || paper != Limits.COPY_PAPER.get() || gunpowder != Limits.COPY_GUNPOWDER.get()
                || stars != Limits.COPY_STARS.get()) return -1;
        return RocketCopy.copyable(input.getItem(rocket)) ? rocket : -1;
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return Features.on(Feature.COPY_RECIPE) && scan(input) >= 0;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, @NotNull HolderLookup.Provider registries) {
        int r = scan(input);
        return r < 0 ? ItemStack.EMPTY : input.getItem(r).copyWithCount(2);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.ROCKET_COPY.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
}
