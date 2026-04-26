package com.armilp.ifreq.client.keys;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.registry.ModItems;
import com.armilp.ifreq.network.ModPackets;
import com.armilp.ifreq.network.OpenWalkieGuiPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MainEZ.MODID, value = Dist.CLIENT)
public class ClientKeyHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (KeyBindings.OPEN_WALKIE_GUI.consumeClick()) {
            if (mc.player.getMainHandItem().getItem() == ModItems.WALKIE_TALKIE.get()) {
                ModPackets.CHANNEL.sendToServer(new OpenWalkieGuiPacket());
            }
        }
    }
}
