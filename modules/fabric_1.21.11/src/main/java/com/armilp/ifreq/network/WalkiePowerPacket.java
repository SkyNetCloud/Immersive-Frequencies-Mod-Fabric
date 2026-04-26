package com.armilp.ifreq.network;

import com.armilp.ifreq.common.WalkieHandler;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class WalkiePowerPacket {

    private final boolean on;

    public WalkiePowerPacket(boolean on) {
        this.on = on;
    }

    public static WalkiePowerPacket decode(FriendlyByteBuf buf) {
        return new WalkiePowerPacket(buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(on);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack walkie = WalkieHandler.getHeldWalkie(player);
            if (walkie == null) return;

            ItemWalkieTalkie.setOn(walkie, on);
            WalkieHandler.onPowerChanged(player, on);
        });
        ctx.get().setPacketHandled(true);
    }
}