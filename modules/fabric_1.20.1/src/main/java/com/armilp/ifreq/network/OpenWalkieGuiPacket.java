package com.armilp.ifreq.network;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.common.menu.WalkieTalkieMenu;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record OpenWalkieGuiPacket() implements FabricPacket {

    public static final PacketType<OpenWalkieGuiPacket> TYPE = PacketType.create(
            Identifier.of(MainEZ.MODID, "open_walkie_gui"),
            OpenWalkieGuiPacket::new
    );

    public OpenWalkieGuiPacket(PacketByteBuf buf) {
        this();
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static void receive(OpenWalkieGuiPacket ignoredpacket, ServerPlayerEntity player, PacketSender ignored) {
        Objects.requireNonNull(player.getServer()).execute(() -> {
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                }

                @Override
                public Text getDisplayName() {
                    return Text.literal("Walkie Talkie");
                }

                @Override
                public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new WalkieTalkieMenu(syncId, inv, player.getMainHandStack());
                }
            });
        });
    }
}