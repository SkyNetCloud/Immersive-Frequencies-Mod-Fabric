package com.armilp.ifreq.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;


public class ModPackets {

    public static void register() {

        PayloadTypeRegistry.playC2S().register(
                OpenWalkieGuiPacket.TYPE,
                OpenWalkieGuiPacket.CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                WalkieFrequencyPacket.TYPE,
                WalkieFrequencyPacket.CODEC
        );

        PayloadTypeRegistry.playC2S().register(
                WalkiePowerPacket.TYPE,
                WalkiePowerPacket.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                OpenWalkieGuiPacket.TYPE,
                OpenWalkieGuiPacket.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                WalkieFrequencyPacket.TYPE,
                WalkieFrequencyPacket.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
                WalkiePowerPacket.TYPE,
                WalkiePowerPacket.CODEC
        );

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