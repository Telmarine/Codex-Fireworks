package com.tac5studios.codexfireworks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tac5studios.codexfireworks.client.ShellRenderer;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Client only. When a rocket explodes, vanilla reads the rocket's stars and calls Level.createFireworks.
 * If the rocket carries a Codex shell, this draws the shell instead; otherwise vanilla runs as normal.
 */
@Mixin(FireworkRocketEntity.class)
@SuppressWarnings({"unused", "DataFlowIssue"}) // called by Mixin; the cast of this is valid at runtime
public abstract class FireworkRocketEntityMixin {

    @WrapOperation(method = "handleEntityEvent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;createFireworks(DDDDDDLjava/util/List;)V"))
    private void codex_fireworks$drawShell(Level level, double x, double y, double z, double dx, double dy, double dz,
                                           List<FireworkExplosion> explosions, Operation<Void> original) {
        if (!ShellRenderer.tryDraw((FireworkRocketEntity) (Object) this, level, x, y, z)) {
            original.call(level, x, y, z, dx, dy, dz, explosions);
        }
    }
}
