package com.tac5studios.codexfireworks.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Both sides. Lets the Mystery Rocket swap the rocket a firework entity carries. */
@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {

    @Accessor("DATA_ID_FIREWORKS_ITEM")
    static EntityDataAccessor<ItemStack> codex_fireworks$itemData() {
        throw new AssertionError();
    }

    @Accessor("lifetime")
    void codex_fireworks$setLifetime(int lifetime);
}
