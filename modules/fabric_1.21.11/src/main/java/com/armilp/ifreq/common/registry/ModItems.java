package com.armilp.ifreq.common.registry;


import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static com.armilp.ifreq.MainEZ.MODID;
import static net.minecraft.registry.RegistryKeys.ITEM;

public class ModItems {

    public static final ItemWalkieTalkie WALKIE_TALKIE = register(
            "walkie_talkie",
            settings -> new ItemWalkieTalkie(settings, 1000.0),
            new Item.Settings().maxCount(1)
    );


    public static <T extends Item> T register(String name, Function<Item.Settings, T> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(ITEM, Identifier.of(MODID, name));
        T item = itemFactory.apply(settings.registryKey(itemKey)); // apply key first
        Registry.register(Registries.ITEM, itemKey, item);
        return item;
    }


    public static void register() {
        // Triggers class loading which registers all items above
    }

}