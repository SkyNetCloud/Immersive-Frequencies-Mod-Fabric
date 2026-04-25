package com.armilp.ifreq.client;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.client.screen.WalkieTalkieScreen;
import com.armilp.ifreq.common.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MainEZ.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(ModMenus.WALKIE_TALKIE_MENU.get(), WalkieTalkieScreen::new);
    }
}
