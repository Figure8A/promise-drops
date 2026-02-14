package com.pdrops.sounds;


import com.pdrops.PromiseDrops;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class DropsSounds {

    public static final SoundEvent GET_UPGRADE = registerSoundEvent("sfx_getupgrade");
    public static final SoundEvent DROP_UPGRADE = registerSoundEvent("sfx_dropupgrade");
    public static final SoundEvent SFX_DEATHFACE_LAUGH = registerSoundEvent("sfx_deathface_laugh");
    public static final SoundEvent MU_DEATHMODE = registerSoundEvent("mu_deathmode");
    public static final SoundEvent MU_DEATHJAM = registerSoundEvent("mu_deathjam");
    public static final SoundEvent SFX_KILLENEMY = registerSoundEvent("sfx_killenemy");

    private static RegistryEntry.Reference<SoundEvent> registerReference(String id) {
        return DropsSounds.registerReference(PromiseDrops.of(id));
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(Identifier id) {
        return DropsSounds.registerReference(id, id);
    }

    private static RegistryEntry.Reference<SoundEvent> registerReference(Identifier id, Identifier soundId) {
        return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(soundId));
    }

    public static void init() {

    }

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = PromiseDrops.of(name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
