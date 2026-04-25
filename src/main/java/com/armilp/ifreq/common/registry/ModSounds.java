package com.armilp.ifreq.common.registry;

import com.armilp.ifreq.MainEZ;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MainEZ.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MainEZ.MODID);

    public static final RegistryObject<SoundEvent> RADIO_BEEP = registerSound("walkie_join");
    public static final RegistryObject<SoundEvent> RADIO_NOISE = registerSound("radio_noise");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () ->
                SoundEvent.createVariableRangeEvent(new ResourceLocation(MainEZ.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
