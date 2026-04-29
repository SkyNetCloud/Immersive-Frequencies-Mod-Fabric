package com.armilp.ifreq.client;

import com.armilp.ifreq.client.accessory.renderer.WalkieAccessoryRenderer;
import com.armilp.ifreq.client.keys.ClientKeyHandler;
import com.armilp.ifreq.client.keys.KeyBindings;
import com.armilp.ifreq.client.screen.WalkieTalkieScreen;
import com.armilp.ifreq.common.registry.ModItems;
import com.armilp.ifreq.common.registry.ModMenus;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class ClientMainEZ implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindings.register();
        HandledScreens.register(ModMenus.WALKIE_TALKIE_MENU, WalkieTalkieScreen::new);
        ClientKeyHandler.register();
        AccessoriesRendererRegistry.registerRenderer(
                ModItems.WALKIE_TALKIE,
                WalkieAccessoryRenderer::new
        );
    }
}
