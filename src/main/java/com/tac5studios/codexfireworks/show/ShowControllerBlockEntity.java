package com.tac5studios.codexfireworks.show;

import com.mojang.serialization.Codec;
import com.tac5studios.codexfireworks.CodexFireworks;
import com.tac5studios.codexfireworks.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Show Controller: the show, the launchers linked to it (in order), the owner and the redstone state. */
public class ShowControllerBlockEntity extends BlockEntity {

    private static final Codec<List<BlockPos>> POS_LIST = BlockPos.CODEC.listOf();

    private ShowData show = ShowData.EMPTY;
    private final List<BlockPos> launchers = new ArrayList<>();
    @Nullable
    private UUID owner;
    private boolean powered;

    public ShowControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SHOW_CONTROLLER.get(), pos, state);
    }

    public ShowData show() {
        return show;
    }

    public void setShow(ShowData show) {
        this.show = show;
        setChanged();
    }

    /** Linked launchers in link order (cue launcher 1 = the first one). */
    public List<BlockPos> launchers() {
        return List.copyOf(launchers);
    }

    /** Link or unlink a launcher. Returns true when it's linked now. */
    public boolean toggle(BlockPos pos) {
        int i = launchers.indexOf(pos);
        if (i >= 0) {
            unlink(i);
            return false;
        }
        launchers.add(pos.immutable());
        setChanged();
        return true;
    }

    /** Remove launcher i (0-based) and renumber the cues that point past it. */
    public void unlink(int i) {
        if (i < 0 || i >= launchers.size()) return;
        launchers.remove(i);
        int removed = i + 1;
        List<Cue> fixed = new ArrayList<>();
        for (Cue c : show.cues()) {
            if (c.launcher() == removed) continue;
            fixed.add(c.launcher() > removed ? new Cue(c.time(), c.launcher() - 1, c.slot()) : c);
        }
        show = show.withCues(fixed);
        setChanged();
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    /** Returns true on a rising edge. */
    public boolean redstone(boolean now) {
        boolean rising = now && !powered;
        powered = now;
        setChanged();
        return rising;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ShowData.CODEC.encodeStart(NbtOps.INSTANCE, show).result().ifPresent(t -> tag.put("Show", t));
        POS_LIST.encodeStart(NbtOps.INSTANCE, launchers).result().ifPresent(t -> tag.put("Launchers", t));
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putBoolean("Powered", powered);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        Tag s = tag.get("Show");
        show = s == null ? ShowData.EMPTY : ShowData.CODEC.parse(NbtOps.INSTANCE, s)
                .resultOrPartial(err -> CodexFireworks.LOGGER.warn("[Fireworks] Show at {} could not be read: {}", worldPosition, err))
                .orElse(ShowData.EMPTY);
        launchers.clear();
        Tag l = tag.get("Launchers");
        if (l != null) POS_LIST.parse(NbtOps.INSTANCE, l).result().ifPresent(launchers::addAll);
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        powered = tag.getBoolean("Powered");
    }
}
