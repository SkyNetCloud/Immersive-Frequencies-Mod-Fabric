package com.armilp.ifreq.common;

import com.armilp.ifreq.Plugin;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import com.armilp.ifreq.common.registry.ModItems;
import io.wispforest.accessories.api.AccessoriesCapability;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WalkieHandler {

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Double> lastFrequencies = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> initTicks = new ConcurrentHashMap<>();

    private static final int INIT_DELAY_TICKS = 2;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(WalkieHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            UUID playerId = player.getUuid();
            ItemStack walkie = getHeldWalkie(player);

            if (walkie != null) {
                handleWalkieHeld(player, playerId, walkie);
            } else {
                handleWalkieDropped(player, playerId);
            }
        }
    }

    private static void handleWalkieHeld(ServerPlayerEntity player, UUID playerId, ItemStack walkie) {
        if (!ItemWalkieTalkie.isOn(walkie)) {
            if (activePlayers.contains(playerId)) {
                Plugin.unsubscribeFromAll(player);
                cleanupPlayer(playerId);
            }
            return;
        }

        if (!activePlayers.contains(playerId)) {
            if (!initTicks.containsKey(playerId)) {
                initTicks.put(playerId, INIT_DELAY_TICKS);
                return;
            }

            int remaining = initTicks.get(playerId) - 1;
            if (remaining > 0) {
                initTicks.put(playerId, remaining);
                return;
            }
            initTicks.remove(playerId);
        }

        double frequency = ItemWalkieTalkie.getFrequency(walkie);
        Double prevFreq = lastFrequencies.get(playerId);

        if (shouldUpdateFrequency(playerId, frequency, prevFreq)) {
            Plugin.subscribeToFrequency(player, frequency);
            lastFrequencies.put(playerId, frequency);
            activePlayers.add(playerId);
        }
    }

    private static void handleWalkieDropped(ServerPlayerEntity player, UUID playerId) {
        if (activePlayers.contains(playerId)) {
            Plugin.unsubscribeFromAll(player);
            cleanupPlayer(playerId);
        }
    }

    private static boolean shouldUpdateFrequency(UUID playerId, double currentFreq, Double prevFreq) {
        return prevFreq == null ||
                Double.compare(currentFreq, prevFreq) != 0 ||
                !activePlayers.contains(playerId);
    }

    public static ItemStack getHeldWalkie(ServerPlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (main.getItem() instanceof ItemWalkieTalkie) return main;
        ItemStack off = player.getOffHandStack();
        if (off.getItem() instanceof ItemWalkieTalkie) return off;
        var cap = AccessoriesCapability.get(player);
        if (cap != null) {
            var equipped = cap.getEquipped(ModItems.WALKIE_TALKIE.asItem());
            for (var ref : equipped) {
                if (ref.reference().slotName().equals("belt")) {
                    return ref.stack();
                }
            }
        }


        return null;
    }

    public static void onPowerChanged(ServerPlayerEntity player, boolean on) {
        UUID playerId = player.getUuid();
        if (!on) {
            if (activePlayers.contains(playerId)) {
                Plugin.unsubscribeFromAll(player);
                cleanupPlayer(playerId);
            }
        } else {
            lastFrequencies.remove(playerId);
            activePlayers.remove(playerId);
            initTicks.remove(playerId);
        }
    }

    private static void cleanupPlayer(UUID playerId) {
        activePlayers.remove(playerId);
        lastFrequencies.remove(playerId);
        initTicks.remove(playerId);
    }

    public static boolean isPlayerActive(UUID playerId) {
        return activePlayers.contains(playerId);
    }

    public static Optional<Double> getPlayerFrequency(UUID playerId) {
        return Optional.ofNullable(lastFrequencies.get(playerId));
    }
}