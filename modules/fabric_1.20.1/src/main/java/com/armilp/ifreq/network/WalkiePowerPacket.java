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

import java.util.Objects;

public record WalkiePowerPacket(boolean on) implements FabricPacket {

    public static final PacketType<WalkiePowerPacket> TYPE = PacketType.create(
            Identifier.of(MainEZ.MODID, "walkie_power"),
            WalkiePowerPacket::new
    );

    // Constructor for reading from buffer
    public WalkiePowerPacket(PacketByteBuf buf) {
        this(buf.readBoolean());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(on);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    // Handler for 1.20.1 Fabric API
    public static void receive(WalkiePowerPacket packet, ServerPlayerEntity player, PacketSender responseSender) {
        Objects.requireNonNull(player.getServer()).execute(() -> {
            ItemStack walkie = WalkieHandler.getHeldWalkie(player);
            assert walkie != null;
            if (walkie.isEmpty()) return;

            ItemWalkieTalkie.setOn(walkie, packet.on());
            WalkieHandler.onPowerChanged(player, packet.on());
        });
    }
}