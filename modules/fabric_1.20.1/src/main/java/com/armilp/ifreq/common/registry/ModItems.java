package com.armilp.ifreq.common.registry;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item WALKIE_TALKIE = new ItemWalkieTalkie(
            new Item.Settings(), 1000.0
    );

    public static void register() {
        Registry.register(
                Registries.ITEM,
                Identifier.of(MainEZ.MODID, "walkie_talkie"),
                WALKIE_TALKIE
        );
    }
}