package com.tac5studios.codexfireworks.pattern;

import com.tac5studios.codexfireworks.registry.ModComponents;
import com.tac5studios.codexfireworks.registry.ModItems;
import com.tac5studios.codexfireworks.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * One drawn Pattern Card plus one or more blank cards: the blank cards become copies, and the
 * original stays in the grid. Works like copying a written book.
 */
public class PatternCopyRecipe extends CustomRecipe {

    public PatternCopyRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static boolean drawn(ItemStack s) {
        Pattern p = s.get(ModComponents.PATTERN.get());
        return p != null && !p.isEmpty();
    }

    /** Index of the drawn card and the number of blank cards, or null when the grid doesn't match. */
    private static int[] scan(CraftingInput input) {
        int source = -1, blanks = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (!s.is(ModItems.PATTERN_CARD.get())) return null;
            if (drawn(s)) {
                if (source >= 0) return null;
                source = i;
            } else {
                blanks++;
            }
        }
        return source >= 0 && blanks > 0 ? new int[]{source, blanks} : null;
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return scan(input) != null;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, @NotNull HolderLookup.Provider registries) {
        int[] r = scan(input);
        if (r == null) return ItemStack.EMPTY;
        return input.getItem(r[0]).copyWithCount(r[1]);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput input) {
        NonNullList<ItemStack> out = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < out.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.hasCraftingRemainingItem()) {
                out.set(i, s.getCraftingRemainingItem());
            } else if (s.is(ModItems.PATTERN_CARD.get()) && drawn(s)) {
                out.set(i, s.copyWithCount(1));
                break;
            }
        }
        return out;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.PATTERN_COPY.get();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }
}
