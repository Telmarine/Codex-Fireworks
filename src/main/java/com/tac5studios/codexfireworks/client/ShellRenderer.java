package com.tac5studios.codexfireworks.client;

import com.tac5studios.codexfireworks.shell.Shell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Called by the rocket mixin when a rocket explodes on this player's screen.
 * Draws the shell if the rocket has one and the server has the mod switched on;
 * otherwise vanilla draws the rocket's normal stars.
 */
public final class ShellRenderer {

    private ShellRenderer() {}

    /** True when this mod drew the explosion, false to let vanilla draw it. */
    public static boolean tryDraw(FireworkRocketEntity rocket, Level level, double x, double y, double z) {
        if (!ClientSettings.enabled()) return false;
        if (!(level instanceof ClientLevel client)) return false;
        Optional<Shell> shell = Shell.read(rocket.getItem());
        if (shell.isEmpty()) return false;
        Minecraft.getInstance().particleEngine.add(new ShellStarter(client, x, y, z, shell.get()));
        return true;
    }
}
