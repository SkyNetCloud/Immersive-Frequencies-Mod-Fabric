package com.armilp.ifreq.network.packets;

import com.armilp.ifreq.common.WalkieHandler;
import com.armilp.ifreq.common.frequency.FrequencyManager;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import com.armilp.ifreq.network.IfreqNetworkService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class WalkieFrequencyPacket {

    private final double frequency;

    public WalkieFrequencyPacket(double frequency) {
        this.frequency = FrequencyManager.roundToTenth(frequency);
    }

    public void encode(RegistryByteBuf buf) {
        buf.writeDouble(this.frequency);
    }

    public static WalkieFrequencyPacket decode(RegistryByteBuf buf) {
        return new WalkieFrequencyPacket(buf.readDouble());
    }

    public void handle(IfreqNetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> {
            PlayerEntity sender = ctx.getSender();
            if (!(sender instanceof ServerPlayerEntity player)) return;

            ItemStack main = player.getMainHandStack();
            ItemStack off = player.getOffHandStack();

            boolean updated = updateWalkieFrequency(main, frequency) ||
                    updateWalkieFrequency(off, frequency);

            if (updated) {
                WalkieHandler.onFrequencyChanged(player, frequency);
            }
        });
        ctx.setPacketHandled(true);
    }

    private static boolean updateWalkieFrequency(ItemStack stack, double frequency) {
        if (stack.getItem() instanceof ItemWalkieTalkie) {
            ItemWalkieTalkie.setFrequency(stack, frequency);
            return true;
        }
        return false;
    }
}