package com.armilp.ifreq.network;

import com.armilp.ifreq.Plugin;
import com.armilp.ifreq.common.frequency.FrequencyManager;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WalkieFrequencyPacket {

    private final double frequency;

    public WalkieFrequencyPacket(double frequency) {
        this.frequency = FrequencyManager.roundToTenth(frequency);
    }

    public static void encode(WalkieFrequencyPacket pkt, FriendlyByteBuf buf) {
        buf.writeDouble(pkt.frequency);
    }

    public static WalkieFrequencyPacket decode(FriendlyByteBuf buf) {
        return new WalkieFrequencyPacket(buf.readDouble());
    }

    public static void handle(WalkieFrequencyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();

            boolean updated = updateWalkieFrequency(main, msg.frequency) ||
                    updateWalkieFrequency(off, msg.frequency);

            if (updated) {
                Plugin.subscribeToFrequency(player, msg.frequency);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean updateWalkieFrequency(ItemStack stack, double frequency) {
        if (stack.getItem() instanceof ItemWalkieTalkie) {
            ItemWalkieTalkie.setFrequency(stack, frequency);
            return true;
        }
        return false;
    }
}