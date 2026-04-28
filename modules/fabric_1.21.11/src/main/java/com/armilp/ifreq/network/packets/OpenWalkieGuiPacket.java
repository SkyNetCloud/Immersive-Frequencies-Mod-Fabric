package com.armilp.ifreq.network.packets;

import com.armilp.ifreq.IfreqPlatform;
import com.armilp.ifreq.client.screen.WalkieTalkieScreen;
import com.armilp.ifreq.common.menu.WalkieTalkieMenu;
import com.armilp.ifreq.network.DistExecutor;
import com.armilp.ifreq.network.IfreqNetworkService;
import de.maxhenkel.voicechat.api.ServerPlayer;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;


public class OpenWalkieGuiPacket {

    private final ItemStack stack;

    public OpenWalkieGuiPacket(ItemStack stack) {
        this.stack = stack;
    }

    public OpenWalkieGuiPacket(RegistryByteBuf buf) {
        this.stack = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
    }

    public void encode(RegistryByteBuf buf) {
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, stack);
    }

    public static OpenWalkieGuiPacket decode(RegistryByteBuf buf) {
        return new OpenWalkieGuiPacket(buf);
    }


    public void handle(IfreqNetworkService.MessageContext ctx) {
        ctx.enqueueWork(() -> {
            DistExecutor.unsafeCallWhenOn(EnvType.CLIENT, () -> () -> {

                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player == null) return null;

                ItemStack stack = client.player.getMainHandStack();

                WalkieTalkieMenu menu = new WalkieTalkieMenu(
                        0,
                        client.player.getInventory(),
                        stack
                );

                client.execute(() -> {
                    client.setScreen(new WalkieTalkieScreen(
                            menu,
                            client.player.getInventory(),
                            Text.translatable("gui.ifreq.walkie_talkie.title")
                    ));
                });

                return null;
            });
        });

        ctx.setPacketHandled(true);
    }
}