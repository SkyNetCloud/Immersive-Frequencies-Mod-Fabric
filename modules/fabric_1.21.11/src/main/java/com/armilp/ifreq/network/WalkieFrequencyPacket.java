package com.armilp.ifreq.network;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.Plugin;
import com.armilp.ifreq.common.frequency.FrequencyManager;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public record WalkieFrequencyPacket(double frequency) implements CustomPayload {

    public static final Identifier ID = Identifier.of(MainEZ.MODID, "walkie_frequency");

    public static final CustomPayload.Id<WalkieFrequencyPacket> TYPE =
            new CustomPayload.Id<>(ID);

    public static final PacketCodec<PacketByteBuf, WalkieFrequencyPacket> CODEC =
            PacketCodec.of(
                    (buf, packet) -> packet.writeDouble(buf.frequency),
                    buf -> new WalkieFrequencyPacket(buf.readDouble())
            );

    public WalkieFrequencyPacket(double frequency) {
        this.frequency = FrequencyManager.roundToTenth(frequency);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    // ✅ NEW handler signature
    public static void receive(WalkieFrequencyPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        context.server().execute(() -> {
            ItemStack main = player.getMainHandStack();
            ItemStack off = player.getOffHandStack();

            boolean updated =
                    updateWalkieFrequency(main, packet.frequency()) ||
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