package com.armilp.ifreq.network;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.WalkieHandler;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;

public record WalkiePowerPacket(boolean on) implements CustomPayload {

    public static final Identifier ID = Identifier.of(MainEZ.MODID, "walkie_power");

    public static final CustomPayload.Id<WalkiePowerPacket> TYPE =
            new CustomPayload.Id<>(ID);

    public static final PacketCodec<PacketByteBuf, WalkiePowerPacket> CODEC =
            PacketCodec.of(
                    (buf, packet) -> packet.writeBoolean(buf.on),
                    buf -> new WalkiePowerPacket(buf.readBoolean())
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    // ✅ New handler signature
    public static void receive(WalkiePowerPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        context.server().execute(() -> {
            ItemStack walkie = WalkieHandler.getHeldWalkie(player);

            if (walkie == null || walkie.isEmpty()) return;

            ItemWalkieTalkie.setOn(walkie, packet.on());
            WalkieHandler.onPowerChanged(player, packet.on());
        });
    }
}