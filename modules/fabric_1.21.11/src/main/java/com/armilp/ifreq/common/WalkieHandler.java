package com.armilp.ifreq.common;

import com.armilp.ifreq.Plugin;
import com.armilp.ifreq.common.frequency.FrequencyManager;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WalkieHandler {
    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Double> lastFrequencies = new ConcurrentHashMap<>();

    public static void register() {
        // Player logout
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.player;
            Plugin.unsubscribeFromAll(player);
            forceCleanup(player.getUuid());
        });

        // Player changed dimension
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            evaluateWalkieState(player);
        });
    }

    private static void evaluateWalkieState(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        ItemStack walkie = getHeldWalkie(player);

        if (walkie == null || !ItemWalkieTalkie.isOn(walkie)) {
            if (activePlayers.contains(playerId)) {
                Plugin.unsubscribeFromAll(player);
                cleanupPlayer(playerId);
            }
            return;
        }

        double frequency = FrequencyManager.roundToTenth(ItemWalkieTalkie.getFrequency(walkie));
        double maxDistance = ((ItemWalkieTalkie) walkie.getItem()).getMaxDistance();
        Plugin.subscribeToFrequency(player, frequency, maxDistance);
        lastFrequencies.put(playerId, frequency);
        activePlayers.add(playerId);
    }

    public static void onPowerChanged(ServerPlayerEntity player, boolean on) {
        if (!on) {
            UUID playerId = player.getUuid();
            if (activePlayers.contains(playerId)) {
                Plugin.unsubscribeFromAll(player);
                cleanupPlayer(playerId);
            }
        } else {
            evaluateWalkieState(player);
        }
    }

    public static void onFrequencyChanged(ServerPlayerEntity player, double newFrequency) {
        UUID playerId = player.getUuid();
        if (!activePlayers.contains(playerId)) return;

        ItemStack walkie = getHeldWalkie(player);
        if (walkie == null) return;

        double maxDistance = ((ItemWalkieTalkie) walkie.getItem()).getMaxDistance();
        Plugin.subscribeToFrequency(player, newFrequency, maxDistance);
        lastFrequencies.put(playerId, newFrequency);
    }

    public static ItemStack getHeldWalkie(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (main.getItem() instanceof ItemWalkieTalkie) return main;
        ItemStack off = player.getOffHandStack();
        if (off.getItem() instanceof ItemWalkieTalkie) return off;
        return null;
    }

    private static void cleanupPlayer(UUID playerId) {
        activePlayers.remove(playerId);
        lastFrequencies.remove(playerId);
    }

    public static void forceCleanup(UUID playerId) {
        cleanupPlayer(playerId);
    }

    public static Optional<Double> getPlayerFrequency(UUID playerId) {
        return Optional.ofNullable(lastFrequencies.get(playerId));
    }
}