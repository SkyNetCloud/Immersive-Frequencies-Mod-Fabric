package com.armilp.ifreq.network;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.menu.WalkieTalkieMenu;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.NonNull;

public record OpenWalkieGuiPacket() implements CustomPayload {

    public static final Identifier ID = Identifier.of(MainEZ.MODID, "open_walkie_gui");

    public static final CustomPayload.Id<OpenWalkieGuiPacket> TYPE =
            new CustomPayload.Id<>(ID);

    public static final PacketCodec<PacketByteBuf, OpenWalkieGuiPacket> CODEC =
            PacketCodec.unit(new OpenWalkieGuiPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return TYPE;
    }

    public static void receive(OpenWalkieGuiPacket packet, ServerPlayNetworking.Context context) {
        ServerPlayerEntity player = context.player();

        context.server().execute(() -> {
            player.openHandledScreen(new ExtendedScreenHandlerFactory<ItemStack>() {


                @Override
                public @NonNull ItemStack getScreenOpeningData(ServerPlayerEntity player) {
                    return player.getMainHandStack();
                }

                @Override
                public Text getDisplayName() {
                    return Text.literal("Walkie Talkie");
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new WalkieTalkieMenu(syncId, inv, player.getMainHandStack());
                }
            });
        });
    }
}