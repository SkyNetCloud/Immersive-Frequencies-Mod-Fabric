package com.armilp.ifreq.common.registry;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static com.armilp.ifreq.MainEZ.MODID;

public class ModItems {

    public static final Item WALKIE_TALKIE = register(
            "walkie_talkie",
            settings -> new ItemWalkieTalkie(settings, 1000)
    );

    private static RegistryKey<Item> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, id));
    }

    public static Item register(String id, Function<Item.Settings, Item> factory) {
        RegistryKey<Item> key = keyOf(id);

        Item.Settings settings = new Item.Settings().registryKey(key);
        Item item = factory.apply(settings);

        return Registry.register(Registries.ITEM, key, item);
    }
    public static void register(){

    }
}