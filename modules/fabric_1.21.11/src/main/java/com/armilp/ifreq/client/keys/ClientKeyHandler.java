package com.armilp.ifreq.client.keys;

import com.armilp.ifreq.common.registry.ModItems;
import com.armilp.ifreq.network.IfreqNetwork;
import com.armilp.ifreq.network.packets.OpenWalkieGuiPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class ClientKeyHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (KeyBindings.OPEN_WALKIE_GUI.wasPressed()) {
                if (client.player.getMainHandStack().getItem() == ModItems.WALKIE_TALKIE) {
                    IfreqNetwork.ifreqNetworkService.sendToServer(
                            new OpenWalkieGuiPacket(client.player.getMainHandStack())
                    );
                }
            }
        });
    }
}