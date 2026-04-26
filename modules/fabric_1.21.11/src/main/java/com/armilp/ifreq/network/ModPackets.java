package com.armilp.ifreq.network;

import com.armilp.ifreq.MainEZ;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class ModPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL;
    public static SimpleChannel CHANNEL2;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MainEZ.MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        CHANNEL2 = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(MainEZ.MODID, "main_channel"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();

        int id = 0;
        CHANNEL.registerMessage(
                id++,
                OpenWalkieGuiPacket.class,
                OpenWalkieGuiPacket::toBytes,
                OpenWalkieGuiPacket::new,
                OpenWalkieGuiPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL2.registerMessage(
                0,
                WalkieFrequencyPacket.class,
                WalkieFrequencyPacket::encode,
                WalkieFrequencyPacket::decode,
                WalkieFrequencyPacket::handle
        );
        CHANNEL2.registerMessage(
                0,
                WalkiePowerPacket.class,
                WalkiePowerPacket::encode,
                WalkiePowerPacket::decode,
                WalkiePowerPacket::handle
        );
    }
}