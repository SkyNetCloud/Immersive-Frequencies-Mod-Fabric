package com.armilp.ifreq.network;


import com.armilp.ifreq.IfreqPlatform;
import com.armilp.ifreq.network.packets.OpenWalkieGuiPacket;
import com.armilp.ifreq.network.packets.WalkieFrequencyPacket;
import com.armilp.ifreq.network.packets.WalkiePowerPacket;

public final class IfreqNetwork {

    public static IfreqNetworkService ifreqNetworkService = IfreqPlatform.getInstance().getNetworkService();

    public IfreqNetwork() {

    }

    public static void register() {
        ifreqNetworkService.registerPacket(OpenWalkieGuiPacket.class, IfreqNetworkService.Direction.CLIENT_TO_SERVER, OpenWalkieGuiPacket::encode, OpenWalkieGuiPacket::decode, OpenWalkieGuiPacket::handle);
        ifreqNetworkService.registerPacket(WalkieFrequencyPacket.class, IfreqNetworkService.Direction.CLIENT_TO_SERVER, WalkieFrequencyPacket::encode, WalkieFrequencyPacket::decode, WalkieFrequencyPacket::handle);
        ifreqNetworkService.registerPacket(WalkiePowerPacket.class, IfreqNetworkService.Direction.CLIENT_TO_SERVER, WalkiePowerPacket::encode, WalkiePowerPacket::decode, WalkiePowerPacket::handle);
    }
}