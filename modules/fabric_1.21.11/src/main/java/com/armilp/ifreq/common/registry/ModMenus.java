package com.armilp.ifreq.common.registry;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.menu.WalkieTalkieMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MainEZ.MODID);

    public static final RegistryObject<MenuType<WalkieTalkieMenu>> WALKIE_TALKIE_MENU =
            MENUS.register("walkie_talkie_menu",
                    () -> IForgeMenuType.create(WalkieTalkieMenu::new)
            );
}
