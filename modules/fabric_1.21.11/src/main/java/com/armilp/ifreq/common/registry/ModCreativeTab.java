package com.armilp.ifreq.common.registry;

import com.armilp.ifreq.MainEZ;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class ModCreativeTab {

    public static final ItemGroup EZFREQ_TAB = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.WALKIE_TALKIE))
            .displayName(Text.translatable("itemGroup.ifreq"))
            .entries((context, entries) -> {
                entries.add(ModItems.WALKIE_TALKIE);
            })
            .build();

    public static void register() {
        Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(MainEZ.MODID, "ezfreq_tab"),
                EZFREQ_TAB
        );
    }
}
