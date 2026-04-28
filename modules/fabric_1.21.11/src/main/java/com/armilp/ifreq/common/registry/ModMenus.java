package com.armilp.ifreq.common.registry;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.menu.WalkieTalkieMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;


public class ModMenus {

    public static final ScreenHandlerType<WalkieTalkieMenu> WALKIE_TALKIE_MENU =
            new ExtendedScreenHandlerType<>(
                    WalkieTalkieMenu::new,
                    ItemStack.PACKET_CODEC
            );

    public static void register() {
        Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(MainEZ.MODID, "walkie_talkie_menu"),
                WALKIE_TALKIE_MENU
        );
    }
}
