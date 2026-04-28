package com.armilp.ifreq.network.packets;


import com.armilp.ifreq.common.WalkieHandler;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import com.armilp.ifreq.network.IfreqNetworkService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class WalkiePowerPacket {

    private final boolean on;

    public WalkiePowerPacket(boolean on) {
        this.on = on;
    }

    public void encode(RegistryByteBuf buf) {
        buf.writeBoolean(this.on);
    }

    public static WalkiePowerPacket decode(RegistryByteBuf buf) {
        return new WalkiePowerPacket(buf.readBoolean());
    }

    public void handle(IfreqNetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> {
            PlayerEntity sender = ctx.getSender();
            if (!(sender instanceof ServerPlayerEntity player)) return;

            ItemStack walkie = WalkieHandler.getHeldWalkie(player);
            if (walkie == null) return;

            ItemWalkieTalkie.setOn(walkie, on);
            WalkieHandler.onPowerChanged(player, on);
        });
        ctx.setPacketHandled(true);
    }
}