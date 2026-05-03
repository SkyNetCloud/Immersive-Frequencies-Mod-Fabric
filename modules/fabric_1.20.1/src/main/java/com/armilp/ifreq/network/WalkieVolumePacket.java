package com.armilp.ifreq.network;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.WalkieHandler;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record WalkieVolumePacket(float volume) implements FabricPacket {

    public static final PacketType<WalkieVolumePacket> TYPE = PacketType.create(
            Identifier.of(MainEZ.MODID, "walkie_volume"),
            WalkieVolumePacket::new
    );

    public WalkieVolumePacket(PacketByteBuf buf) {
        this(buf.readFloat());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(volume);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static void receive(WalkieVolumePacket packet, ServerPlayerEntity player, PacketSender responseSender) {
        player.getServer().execute(() -> {
            ItemStack walkie = WalkieHandler.getHeldWalkie(player);
            if (walkie == null || walkie.isEmpty()) return;
            ItemWalkieTalkie.setVolume(walkie, packet.volume());
        });
    }
}