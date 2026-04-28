package com.armilp.ifreq;

import com.armilp.ifreq.common.frequency.FrequencyManager;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;


import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ForgeVoicechatPlugin
public class Plugin implements VoicechatPlugin {

    public static final String PLUGIN_ID = "ifreq";
    private static VoicechatServerApi api;

    private static final Map<UUID, Double> playerFrequencies = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> playerMaxDistances = new ConcurrentHashMap<>();

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicPacket);
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        api = event.getVoicechat();
        MainEZ.LOGGER.info("[ImmersiveFrequencies] VoiceChat plugin initialized");
    }

    private void onMicPacket(MicrophonePacketEvent event) {
        if (api == null) return;

        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null) return;
        if (!(senderConnection.getPlayer().getPlayer() instanceof ServerPlayerEntity senderPlayer)) return;

        UUID senderId = senderPlayer.getUuid();

        Double senderFreq = playerFrequencies.get(senderId);
        if (senderFreq == null) return;

        Double maxDist = playerMaxDistances.get(senderId);
        if (maxDist == null) return;

        double maxDistSq = maxDist * maxDist;

        var server = senderPlayer.getEntityWorld().getServer();
        if (server == null) return;

        var builtPacket = event.getPacket().staticSoundPacketBuilder().build();

        for (Map.Entry<UUID, Double> entry : playerFrequencies.entrySet()) {
            UUID receiverId = entry.getKey();
            if (receiverId.equals(senderId)) continue;
            if (Double.compare(senderFreq, entry.getValue()) != 0) continue;

            ServerPlayerEntity receiverPlayer = server.getPlayerManager().getPlayer(receiverId);
            if (receiverPlayer == null) continue;
            if (!receiverPlayer.getEntityWorld().getDimensionEntry().equals(senderPlayer.getEntityWorld().getDimensionEntry())) continue;

            double dx = senderPlayer.getX() - receiverPlayer.getX();
            double dy = senderPlayer.getY() - receiverPlayer.getY();
            double dz = senderPlayer.getZ() - receiverPlayer.getZ();
            if (dx * dx + dy * dy + dz * dz > maxDistSq) continue;

            VoicechatConnection receiverConnection = api.getConnectionOf(receiverId);
            if (receiverConnection == null) continue;

            api.sendStaticSoundPacketTo(receiverConnection, builtPacket);
        }
    }

    public static void subscribeToFrequency(ServerPlayerEntity player, double frequency, double maxDistance) {
        if (player == null) return;
        UUID id = player.getUuid();
        playerFrequencies.put(id, frequency);
        playerMaxDistances.put(id, maxDistance);
    }

    public static void unsubscribeFromAll(ServerPlayerEntity player) {
        if (player == null) return;
        UUID id = player.getUuid();
        playerFrequencies.remove(id);
        playerMaxDistances.remove(id);
    }

    public static int getSubscribedCount(double frequency) {
        frequency = Math.round(frequency * 10.0) / 10.0;
        int count = 0;
        for (double freq : playerFrequencies.values()) {
            if (Double.compare(freq, frequency) == 0) count++;
        }
        return count;
    }
}