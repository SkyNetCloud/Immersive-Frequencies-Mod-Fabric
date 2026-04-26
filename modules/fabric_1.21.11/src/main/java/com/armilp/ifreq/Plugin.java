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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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

        if (!(senderConnection.getPlayer().getPlayer() instanceof ServerPlayer senderPlayer)) return;

        ItemStack senderWalkie = getWalkieTalkieInHand(senderPlayer);

        if (senderWalkie == null || !ItemWalkieTalkie.isOn(senderWalkie)) return;

        var server = senderPlayer.getServer();
        if (server == null) return;

        UUID senderId = senderPlayer.getUUID();
        double senderFreq = ItemWalkieTalkie.getFrequency(senderWalkie);

        double maxDist = ((ItemWalkieTalkie) senderWalkie.getItem()).getMaxDistance();
        double maxDistSq = maxDist * maxDist;

        var builtPacket = event.getPacket().staticSoundPacketBuilder().build();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        for (ServerPlayer receiverPlayer : players) {
            if (receiverPlayer.getUUID().equals(senderId)) continue;
            if (!receiverPlayer.level().dimension().equals(senderPlayer.level().dimension())) continue;

            ItemStack receiverWalkie = getWalkieTalkieInHand(receiverPlayer);

            if (receiverWalkie == null || !ItemWalkieTalkie.isOn(receiverWalkie)) continue;

            double receiverFreq = ItemWalkieTalkie.getFrequency(receiverWalkie);
            if (Double.compare(senderFreq, receiverFreq) != 0) continue;

            double dx = senderPlayer.getX() - receiverPlayer.getX();
            double dy = senderPlayer.getY() - receiverPlayer.getY();
            double dz = senderPlayer.getZ() - receiverPlayer.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq > maxDistSq) continue;

            var receiverConnection = api.getConnectionOf(receiverPlayer.getUUID());
            if (receiverConnection == null) continue;

            api.sendStaticSoundPacketTo(receiverConnection, builtPacket);
        }
    }

    private ItemStack getWalkieTalkieInHand(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ItemWalkieTalkie) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof ItemWalkieTalkie) return off;
        return null;
    }

    public static void subscribeToFrequency(ServerPlayer player, double frequency) {
        if (player == null) return;
        if (!hasWalkieTalkie(player)) return;
        frequency = FrequencyManager.roundToTenth(frequency);
        playerFrequencies.put(player.getUUID(), frequency);
    }

    public static void unsubscribeFromAll(ServerPlayer player) {
        if (player == null) return;
        UUID playerId = player.getUUID();
        Double frequency = playerFrequencies.remove(playerId);
    }

    public static boolean hasWalkieTalkie(ServerPlayer player) {
        if (player == null) return false;
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ItemWalkieTalkie) return true;
        ItemStack off = player.getOffhandItem();
        return off.getItem() instanceof ItemWalkieTalkie;
    }
}