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
        MainEZ.LOGGER.info("[eZFreq] VoiceChat plugin initialized");
    }

    private void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null || api == null) return;

        if (!(senderConnection.getPlayer().getPlayer() instanceof ServerPlayerEntity senderPlayer)) return;

        ItemStack senderWalkie = getWalkieTalkieInHand(senderPlayer);

        if (senderWalkie == null || !ItemWalkieTalkie.isOn(senderWalkie)) return;

        var server = senderPlayer.getEntityWorld();
        if (server == null) return;

        UUID senderId = senderPlayer.getUuid();
        double senderFreq = ItemWalkieTalkie.getFrequency(senderWalkie);

        double maxDist = ((ItemWalkieTalkie) senderWalkie.getItem()).getMaxDistance();
        double maxDistSq = maxDist * maxDist;

        var builtPacket = event.getPacket().staticSoundPacketBuilder().build();
        List<ServerPlayerEntity> players = server.getServer().getPlayerManager().getPlayerList();

        for (ServerPlayerEntity receiverPlayer : players) {
            if (receiverPlayer.getUuid().equals(senderId)) continue;
            if (!receiverPlayer.getEntityWorld().getDimensionEntry().equals(senderPlayer.getEntityWorld().getDimensionEntry())) continue;

            ItemStack receiverWalkie = getWalkieTalkieInHand(receiverPlayer);

            if (receiverWalkie == null || !ItemWalkieTalkie.isOn(receiverWalkie)) continue;

            double receiverFreq = ItemWalkieTalkie.getFrequency(receiverWalkie);
            if (Double.compare(senderFreq, receiverFreq) != 0) continue;

            double dx = senderPlayer.getX() - receiverPlayer.getX();
            double dy = senderPlayer.getY() - receiverPlayer.getY();
            double dz = senderPlayer.getZ() - receiverPlayer.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > maxDistSq) continue;

            var receiverConnection = api.getConnectionOf(receiverPlayer.getUuid());
            if (receiverConnection == null) continue;

            api.sendStaticSoundPacketTo(receiverConnection, builtPacket);
        }
    }

    private ItemStack getWalkieTalkieInHand(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (main.getItem() instanceof ItemWalkieTalkie) return main;
        ItemStack off = player.getOffHandStack();
        if (off.getItem() instanceof ItemWalkieTalkie) return off;
        return null;
    }

    public static void subscribeToFrequency(ServerPlayerEntity player, double frequency) {
        if (player == null) return;
        if (!hasWalkieTalkie(player)) return;
        frequency = FrequencyManager.roundToTenth(frequency);
        playerFrequencies.put(player.getUuid(), frequency);
    }

    public static void unsubscribeFromAll(ServerPlayerEntity player) {
        if (player == null) return;
        UUID playerId = player.getUuid();
        Double frequency = playerFrequencies.remove(playerId);
    }

    public static boolean hasWalkieTalkie(ServerPlayerEntity player) {
        if (player == null) return false;
        ItemStack main = player.getMainHandStack();
        if (main.getItem() instanceof ItemWalkieTalkie) return true;
        ItemStack off = player.getOffHandStack();
        return off.getItem() instanceof ItemWalkieTalkie;
    }
}