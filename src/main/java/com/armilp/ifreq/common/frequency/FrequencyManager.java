package com.armilp.ifreq.common.frequency;

import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "ifreq")
public class FrequencyManager {

    private static final Map<Double, Set<UUID>> frequencyListeners = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> playerFrequencies = new ConcurrentHashMap<>();

    public static boolean joinFrequency(ServerPlayer player, double frequency) {
        if (!hasWalkieTalkie(player)) return false;

        frequency = roundToTenth(frequency);
        UUID playerId = player.getUUID();

        leaveAllFrequencies(player);

        frequencyListeners.computeIfAbsent(frequency, f -> ConcurrentHashMap.newKeySet()).add(playerId);
        playerFrequencies.put(playerId, frequency);

        return true;
    }

    public static void leaveAllFrequencies(ServerPlayer player) {
        UUID playerId = player.getUUID();
        Double currentFrequency = playerFrequencies.remove(playerId);

        if (currentFrequency != null) {
            Set<UUID> listeners = frequencyListeners.get(currentFrequency);
            if (listeners != null) {
                listeners.remove(playerId);
                if (listeners.isEmpty()) {
                    frequencyListeners.remove(currentFrequency);
                    cleanupFrequencyData(player.level(), currentFrequency);
                }
            }
        }
    }

    public static Set<UUID> getListenersOnFrequency(double frequency) {
        return frequencyListeners.getOrDefault(roundToTenth(frequency), Collections.emptySet());
    }

    public static double getFrequencyForPlayer(UUID playerId) {
        return playerFrequencies.getOrDefault(playerId, -1.0);
    }

    public static boolean isPlayerOnFrequency(ServerPlayer player, double frequency) {
        Double playerFreq = playerFrequencies.get(player.getUUID());
        return playerFreq != null && Math.abs(playerFreq - roundToTenth(frequency)) < 0.01;
    }

    public static boolean hasWalkieTalkie(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ItemWalkieTalkie) return true;
        ItemStack off = player.getOffhandItem();
        return off.getItem() instanceof ItemWalkieTalkie;
    }

    public static int getActivePlayersOnFrequency(double frequency) {
        return getListenersOnFrequency(frequency).size();
    }

    public static Set<Double> getActiveFrequencies() {
        return new HashSet<>(frequencyListeners.keySet());
    }

    public static double roundToTenth(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static boolean isValidFrequency(double frequency) {
        return frequency >= 87.5 && frequency <= 108.0;
    }

    private static void cleanupFrequencyData(Level level, double frequency) {
        try {
            FrequencySavedData savedData = FrequencySavedData.get(level);
            savedData.getGroupName(frequency).ifPresent(savedData::removeMapping);
        } catch (Exception e) {
            System.err.println("Error cleaning frequency data " + frequency + ": " + e.getMessage());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            leaveAllFrequencies(serverPlayer);
        }
    }
}