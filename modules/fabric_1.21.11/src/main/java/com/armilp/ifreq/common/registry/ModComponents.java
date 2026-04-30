package com.armilp.ifreq.common.registry;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModComponents {

    public static final ComponentType<Double> FREQUENCY =
            register("frequency", Codec.DOUBLE);

    public static final ComponentType<Boolean> POWER =
            register("power", Codec.BOOL);

    private static <T> ComponentType<T> register(String name, Codec<T> codec) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.of("ifreq", name),
                ComponentType.<T>builder().codec(codec).build()
        );
    }

}
