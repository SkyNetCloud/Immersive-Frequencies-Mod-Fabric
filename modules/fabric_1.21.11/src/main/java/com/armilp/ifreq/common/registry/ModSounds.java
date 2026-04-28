package com.armilp.ifreq.common.registry;

import com.armilp.ifreq.MainEZ;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {

    public static final SoundEvent RADIO_BEEP = registerSound("walkie_join");
    public static final SoundEvent RADIO_NOISE = registerSound("radio_noise");

    private static SoundEvent registerSound(String name) {
        Identifier id = Identifier.of(MainEZ.MODID, name);
        return Registry.register(
                Registries.SOUND_EVENT,
                id,
                SoundEvent.of(id)
        );
    }

    public static void register() {
    }
}