package com.armilp.ifreq.network;

import com.armilp.ifreq.common.menu.WalkieTalkieMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class OpenWalkieGuiPacket {

    public OpenWalkieGuiPacket() {
    }

    // Deserialización: no enviamos datos extra, así que no leemos nada
    public OpenWalkieGuiPacket(FriendlyByteBuf buf) {
        // No hay datos que leer
    }

    // Serialización: no enviamos datos, así que no escribimos nada
    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ServerPlayer serverPlayer = context.getSender();
        if (serverPlayer == null) {
            context.setPacketHandled(true);
            return;
        }

        context.enqueueWork(() -> {
            // Creamos un MenuProvider sencillo que Forge entiende
            SimpleMenuProvider provider = new SimpleMenuProvider(
                    (id, inv, player) -> new WalkieTalkieMenu(id, inv, player.getMainHandItem()),
                    Component.literal("Walkie Talkie")
            );

            // Usamos NetworkHooks.openGui para que Forge abra el contenedor
            NetworkHooks.openScreen(serverPlayer, provider);
        });

        context.setPacketHandled(true);
    }
}
