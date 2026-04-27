package com.armilp.ifreq.common.events;

import com.armilp.ifreq.Plugin;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerEventHandler {

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            Plugin.unsubscribeFromAll(player);
        });
    }
}