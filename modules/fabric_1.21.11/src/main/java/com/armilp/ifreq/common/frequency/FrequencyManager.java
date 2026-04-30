package com.armilp.ifreq.common.frequency;

import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FrequencyManager {

    private static FrequencyManager INSTANCE;

    private final Map<Double, Set<UUID>> frequencyListeners = new ConcurrentHashMap<>();
    private final Map<UUID, Double> playerFrequencies = new ConcurrentHashMap<>();

    public static FrequencyManager getInstance() {
        if (INSTANCE == null) INSTANCE = new FrequencyManager();
        return INSTANCE;
    }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                {
                    getInstance();
                    leaveAllFrequencies(handler.player);
                }
        );
    }

    public static boolean joinFrequency(ServerPlayerEntity player, double frequency) {
        return getInstance()._joinFrequency(player, frequency);
    }

    private boolean _joinFrequency(ServerPlayerEntity player, double frequency) {
        if (!hasWalkieTalkie(player)) return false;
        frequency = roundToTenth(frequency);
        UUID playerId = player.getUuid();
        _leaveAllFrequencies(player);
        frequencyListeners.computeIfAbsent(frequency, f -> ConcurrentHashMap.newKeySet()).add(playerId);
        playerFrequencies.put(playerId, frequency);
        return true;
    }

    public static void leaveAllFrequencies(ServerPlayerEntity player) {
        getInstance()._leaveAllFrequencies(player);
    }

    private void _leaveAllFrequencies(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        Double currentFrequency = playerFrequencies.remove(playerId);
        if (currentFrequency != null) {
            Set<UUID> listeners = frequencyListeners.get(currentFrequency);
            if (listeners != null) {
                listeners.remove(playerId);
                if (listeners.isEmpty()) {
                    frequencyListeners.remove(currentFrequency);
                    cleanupFrequencyData(player.getEntityWorld(), currentFrequency);
                }
            }
        }
    }

    public static Set<UUID> getListenersOnFrequency(double frequency) {
        return getInstance().frequencyListeners.getOrDefault(roundToTenth(frequency), Collections.emptySet());
    }

    public static double getFrequencyForPlayer(UUID playerId) {
        return getInstance().playerFrequencies.getOrDefault(playerId, -1.0);
    }

    public static boolean isPlayerOnFrequency(ServerPlayerEntity player, double frequency) {
        Double playerFreq = getInstance().playerFrequencies.get(player.getUuid());
        return playerFreq != null && Math.abs(playerFreq - roundToTenth(frequency)) < 0.01;
    }

    public static boolean hasWalkieTalkie(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (main.getItem() instanceof ItemWalkieTalkie) return true;
        ItemStack off = player.getOffHandStack();
        return off.getItem() instanceof ItemWalkieTalkie;
    }

    public static int getActivePlayersOnFrequency(double frequency) {
        return getListenersOnFrequency(frequency).size();
    }

    public static Set<Double> getActiveFrequencies() {
        return new HashSet<>(getInstance().frequencyListeners.keySet());
    }

    public static double roundToTenth(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static boolean isValidFrequency(double frequency) {
        return frequency >= 87.5 && frequency <= 108.0;
    }

    private static void cleanupFrequencyData(ServerWorld world, double frequency) {
        try {
            FrequencySavedData savedData = FrequencySavedData.get(world);
            savedData.getGroupName(frequency).ifPresent(savedData::removeMapping);
        } catch (Exception e) {
            System.err.println("Error cleaning frequency data " + frequency + ": " + e.getMessage());
        }
    }
}