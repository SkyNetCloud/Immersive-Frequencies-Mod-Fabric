package com.armilp.ifreq.network;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.Plugin;
import com.armilp.ifreq.common.frequency.FrequencyManager;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record WalkieFrequencyPacket(double frequency) implements FabricPacket {

    public static final PacketType<WalkieFrequencyPacket> TYPE = PacketType.create(
            Identifier.of(MainEZ.MODID, "walkie_frequency"),
            WalkieFrequencyPacket::new
    );

    // Constructor for reading from buffer
    public WalkieFrequencyPacket(PacketByteBuf buf) {
        this(buf.readDouble());
    }

    // Round on construction
    public WalkieFrequencyPacket(double frequency) {
        this.frequency = FrequencyManager.roundToTenth(frequency);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeDouble(frequency);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    // Handler for 1.20.1 Fabric API
    public static void receive(WalkieFrequencyPacket packet, ServerPlayerEntity player, PacketSender responseSender) {
        player.getServer().execute(() -> {
            ItemStack main = player.getMainHandStack();
            ItemStack off = player.getOffHandStack();

            boolean updated = updateWalkieFrequency(main, packet.frequency()) ||
                    updateWalkieFrequency(off, packet.frequency());

            if (updated) {
                Plugin.subscribeToFrequency(player, packet.frequency());
            }
        });
    }

    private static boolean updateWalkieFrequency(ItemStack stack, double frequency) {
        if (stack.getItem() instanceof ItemWalkieTalkie) {
            ItemWalkieTalkie.setFrequency(stack, frequency);
            return true;
        }
        return false;
    }
}