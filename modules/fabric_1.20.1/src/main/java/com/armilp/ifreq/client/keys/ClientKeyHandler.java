package com.armilp.ifreq.client.keys;

import com.armilp.ifreq.common.registry.ModItems;
import com.armilp.ifreq.network.OpenWalkieGuiPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientKeyHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (KeyBindings.OPEN_WALKIE_GUI.wasPressed()) {
                if (client.player.getMainHandStack().getItem() == ModItems.WALKIE_TALKIE) {
                    ClientPlayNetworking.send(new OpenWalkieGuiPacket());
                }
            }
        });
    }
}