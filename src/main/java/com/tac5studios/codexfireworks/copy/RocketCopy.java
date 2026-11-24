package com.tac5studios.codexfireworks.copy;

import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.config.Limits;
import com.tac5studios.codexfireworks.shell.RocketFactory;
import com.tac5studios.codexfireworks.shell.Shell;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Fireworks;

/** Which rockets can be copied (crafting recipe, workbench template, Create Deployer). */
public final class RocketCopy {

    private RocketCopy() {}

    /** A rocket with at least one burst, not a Mystery Rocket, with a rarity listed in limits.toml [copying]. */
    public static boolean copyable(ItemStack stack) {
        if (!Features.enabled() || !stack.is(Items.FIREWORK_ROCKET) || RocketFactory.isMystery(stack)) return false;
        if (!hasBursts(stack)) return false;
        return Limits.COPY_RARITIES.get().contains(rarity(stack));
    }

    /** True when the rocket pops: it has a shell or vanilla stars. A plain rocket only flies. */
    public static boolean hasBursts(ItemStack stack) {
        if (Shell.read(stack).isPresent()) return true;
        Fireworks fw = stack.get(DataComponents.FIREWORKS);
        return fw != null && !fw.explosions().isEmpty();
    }

    /** A firework rocket with no stars (paper + gunpowder). */
    public static boolean plain(ItemStack stack) {
        return stack.is(Items.FIREWORK_ROCKET) && !hasBursts(stack) && !RocketFactory.isMystery(stack);
    }

    /** "common", "rare" or "epic". The shell's own tag wins; otherwise the item's rarity color. */
    public static String rarity(ItemStack stack) {
        String tag = Shell.read(stack).map(Shell::rarity).orElse("");
        if (!tag.isEmpty()) return tag;
        Rarity r = stack.get(DataComponents.RARITY);
        if (r == Rarity.EPIC) return "epic";
        if (r == Rarity.RARE) return "rare";
        return "common";
    }
}
