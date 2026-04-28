package com.armilp.ifreq.common.platform.fabric;


import com.armilp.ifreq.common.platform.fabric.IfreqFabricPayload;
import com.armilp.ifreq.network.IfreqNetworkService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.armilp.ifreq.MainEZ.MODID;

public class IfreqFabricNetworkService implements IfreqNetworkService {
    private final LinkedHashMap<Class<?>, PacketInfo<?>> packetRegistrations = new LinkedHashMap<>();

    public IfreqFabricNetworkService() {
    }

    @Override
    public <M, B extends PacketByteBuf> void registerPacket(
            Class<M> var1,
            Direction var2,
            BiConsumer<M, RegistryByteBuf> var3,
            Function<RegistryByteBuf, M> var4,
            BiConsumer<M, MessageContext> var5) {

        PacketCodec<RegistryByteBuf, M> mCodec = PacketCodec.of(
                (ValueFirstEncoder<RegistryByteBuf, M>) var3::accept,
                var4::apply
        );

        PacketCodec<RegistryByteBuf, IfreqFabricPayload<M>> codec =
                PacketCodec.tuple(mCodec, IfreqFabricPayload::getData, IfreqFabricPayload::new);

        CustomPayload.Id<IfreqFabricPayload<M>> type =
                new CustomPayload.Id<>(Identifier.of(MODID, var1.getSimpleName().toLowerCase()));

        this.packetRegistrations.put(var1, new PacketInfo<>(var2, codec, type,
                (p, c) -> var5.accept(p.getData(), c)));
    }

    @Override
    public void onRegisteringPacketsCompleted() {
        for (Map.Entry<Class<?>, PacketInfo<?>> entry : this.packetRegistrations.entrySet()) {
            registerPayload(entry.getValue());
        }
    }

    @Override
    public void onRegisteringClientPacketsCompleted() {
        for (Map.Entry<Class<?>, PacketInfo<?>> entry : this.packetRegistrations.entrySet()) {
            registerClientReceiver(entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void registerPayload(PacketInfo<T> packetInfo) {
        if (packetInfo.direction == Direction.CLIENT_TO_SERVER) {
            PayloadTypeRegistry.playC2S().register(packetInfo.type, packetInfo.codec);
            ServerPlayNetworking.registerGlobalReceiver(
                    packetInfo.type,
                    (p, c) -> packetInfo.handler.accept(p, new FabricC2SMessageContext(c))
            );
        } else if (packetInfo.direction == Direction.SERVER_TO_CLIENT) {
            PayloadTypeRegistry.playS2C().register(packetInfo.type, packetInfo.codec);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void registerClientReceiver(PacketInfo<T> packetInfo) {
        if (packetInfo.direction == Direction.SERVER_TO_CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(
                    packetInfo.type,
                    (p, c) -> packetInfo.handler.accept(p, new FabricS2CMessageContext(c))
            );
        }
    }

    @Override
    public void sendToClient(Object message, PlayerEntity toPlayer) {
        ServerPlayNetworking.send((ServerPlayerEntity) toPlayer, new IfreqFabricPayload<>(message));
    }

    @Override
    public void sendToServer(Object message) {
        ClientPlayNetworking.send(new IfreqFabricPayload<>(message));
    }

    @Override
    public CustomPayload createCustomPayload(Object packet) {
        return new IfreqFabricPayload<>(packet);
    }

    private static record PacketInfo<T>(
            IfreqNetworkService.Direction direction,
            PacketCodec<RegistryByteBuf, IfreqFabricPayload<T>> codec,
            CustomPayload.Id<IfreqFabricPayload<T>> type,
            BiConsumer<IfreqFabricPayload<T>, IfreqNetworkService.MessageContext> handler
    ) {}

    static class FabricS2CMessageContext implements IfreqNetworkService.MessageContext {
        private final ClientPlayNetworking.Context delegate;

        FabricS2CMessageContext(ClientPlayNetworking.Context delegate) {
            this.delegate = delegate;
        }

        public void enqueueWork(Runnable runnable) {
            MinecraftClient client = this.delegate.client();
            client.execute(runnable);
        }

        public void setPacketHandled(boolean isPacketHandled) {
        }

        public PlayerEntity getSender() {
            return this.delegate.player();
        }

        public boolean isClientSide() {
            return false;
        }
    }

    static class FabricC2SMessageContext implements IfreqNetworkService.MessageContext {
        private final ServerPlayNetworking.Context delegate;

        FabricC2SMessageContext(ServerPlayNetworking.Context delegate) {
            this.delegate = delegate;
        }

        public void enqueueWork(Runnable runnable) {
            this.delegate.player().getEntityWorld().getServer().execute(runnable);
        }

        public void setPacketHandled(boolean isPacketHandled) {
        }

        public PlayerEntity getSender() {
            return this.delegate.player();
        }

        public boolean isClientSide() {
            return false;
        }
    }
}
