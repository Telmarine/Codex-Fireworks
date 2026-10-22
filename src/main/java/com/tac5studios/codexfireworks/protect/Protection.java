package com.tac5studios.codexfireworks.protect;

import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.config.Feature;
import com.tac5studios.codexfireworks.config.Features;
import com.tac5studios.codexfireworks.core.Mods;
import com.tac5studios.codexfireworks.perms.Perm;
import com.tac5studios.codexfireworks.util.Reflect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Who may change a Launcher or Show Controller: the player who placed it, the owner of the land
 * it stands on, and staff with fireworks.bypass.
 *
 * <p>Land owners come from claim mods, checked by reflection so none of them is needed:
 * Elements: Claims (ClaimsAPI) first, then Open Parties and Claims, then FTB Chunks.
 * Switched off with features.toml [rules] claims = false.
 */
public final class Protection {

    private static boolean elementsWarned, opacWarned, ftbWarned;

    private Protection() {}

    /** True when this player may change the block at pos that was placed by {@code owner} (null = nobody). */
    public static boolean canEdit(ServerPlayer player, @Nullable UUID owner, Level level, BlockPos pos) {
        if (Perm.has(player, Perm.BYPASS)) return true;
        if (owner == null || owner.equals(player.getUUID())) return true;
        return Features.on(Feature.CLAIMS) && ownsLand(player, level, pos);
    }

    /** True when a claim mod says this player owns the land at pos. */
    public static boolean ownsLand(ServerPlayer player, Level level, BlockPos pos) {
        UUID me = player.getUUID();
        if (Mods.elementsClaims() && elements(level, pos, me)) return true;
        if (Mods.opac() && opac(level, pos, me)) return true;
        return Mods.ftbChunks() && ftb(level, pos, me);
    }

    private static boolean elements(Level level, BlockPos pos, UUID me) {
        try {
            Object owner = Reflect.callStatic("com.tac5studios.elementsclaims.api.ClaimsAPI", "owner", level, pos);
            return owner instanceof Optional<?> o && o.isPresent() && me.equals(o.get());
        } catch (RuntimeException e) {
            if (!elementsWarned) {
                elementsWarned = true;
                CodexFireworks.LOGGER.warn("[Fireworks] Elements: Claims check failed: {}", e.toString());
            }
            return false;
        }
    }

    private static boolean opac(Level level, BlockPos pos, UUID me) {
        try {
            Object api = Reflect.callStatic("xaero.pac.common.server.api.OpenPACServerAPI", "get", level.getServer());
            Object claims = Reflect.call(api, "getServerClaimsManager");
            Object claim = Reflect.call(claims, "get", level.dimension().location(), pos.getX() >> 4, pos.getZ() >> 4);
            return claim != null && me.equals(Reflect.call(claim, "getPlayerId"));
        } catch (RuntimeException e) {
            if (!opacWarned) {
                opacWarned = true;
                CodexFireworks.LOGGER.warn("[Fireworks] Open Parties and Claims check failed: {}", e.toString());
            }
            return false;
        }
    }

    private static boolean ftb(Level level, BlockPos pos, UUID me) {
        try {
            Object api = Reflect.callStatic("dev.ftb.mods.ftbchunks.api.FTBChunksAPI", "api");
            if (!Boolean.TRUE.equals(Reflect.call(api, "isManagerLoaded"))) return false;
            Object manager = Reflect.call(api, "getManager");
            Object dimPos = Reflect.create("dev.ftb.mods.ftblibrary.math.ChunkDimPos", level, pos);
            Object chunk = Reflect.call(manager, "getChunk", dimPos);
            if (chunk == null) return false;
            Object team = Reflect.call(chunk, "getTeamData");
            return Boolean.TRUE.equals(Reflect.call(team, "isTeamMember", me));
        } catch (RuntimeException e) {
            if (!ftbWarned) {
                ftbWarned = true;
                CodexFireworks.LOGGER.warn("[Fireworks] FTB Chunks check failed: {}", e.toString());
            }
            return false;
        }
    }
}
