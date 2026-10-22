package com.tac5studios.codexfireworks.registry;

import com.tac5studios.codexfireworks.CodexFireworks;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Shape sounds. The sound files are made for this mod (assets/codex_fireworks/sounds/).
 * Shapes without their own sound use the vanilla firework sounds.
 */
public final class ModSounds {

    static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, CodexFireworks.MOD_ID);

    /** Crackle: a run of small pops. */
    public static final DeferredHolder<SoundEvent, SoundEvent> CRACKLE = sound("shape.crackle");
    /** Comet: a rising whistle. */
    public static final DeferredHolder<SoundEvent, SoundEvent> COMET = sound("shape.comet");
    /** Willow: a deep, long boom. */
    public static final DeferredHolder<SoundEvent, SoundEvent> WILLOW = sound("shape.willow");
    /** Strobe: fast soft ticks. */
    public static final DeferredHolder<SoundEvent, SoundEvent> STROBE = sound("shape.strobe");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(CodexFireworks.id(name)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
