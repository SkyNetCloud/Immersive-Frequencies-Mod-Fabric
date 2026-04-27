package com.armilp.ifreq.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModPackets {
        public static void register() {
            ServerPlayNetworking.registerGlobalReceiver(
                    OpenWalkieGuiPacket.TYPE,
                    OpenWalkieGuiPacket::receive
            );

            ServerPlayNetworking.registerGlobalReceiver(
                    WalkieFrequencyPacket.TYPE,
                    WalkieFrequencyPacket::receive
            );

            ServerPlayNetworking.registerGlobalReceiver(
                    WalkiePowerPacket.TYPE,
                    WalkiePowerPacket::receive
            );
        }
}