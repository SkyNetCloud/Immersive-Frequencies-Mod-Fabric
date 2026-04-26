package com.armilp.ifreq.common.registry;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MainEZ.MODID);

    public static final RegistryObject<Item> WALKIE_TALKIE = ITEMS.register("walkie_talkie",
            () -> new ItemWalkieTalkie(new Item.Properties(), 1000.0));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}