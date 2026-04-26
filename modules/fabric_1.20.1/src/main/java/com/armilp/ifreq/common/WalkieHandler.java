package com.armilp.ifreq.common;

import com.armilp.ifreq.MainEZ;
import com.armilp.ifreq.Plugin;
import com.armilp.ifreq.common.items.ItemWalkieTalkie;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = MainEZ.MODID)
public class WalkieHandler {

    private static final Set<UUID> activePlayers = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Double> lastFrequencies = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> initTicks = new ConcurrentHashMap<>();

    private static final int INIT_DELAY_TICKS = 2;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();
        ItemStack walkie = getHeldWalkie(player);

        if (walkie != null) {
            handleWalkieHeld(player, playerId, walkie);
        } else {
            handleWalkieDropped(player, playerId);
        }
    }

    private static void handleWalkieHeld(ServerPlayer player, UUID playerId, ItemStack walkie) {
        // Si el walkie está apagado, desconectar y no hacer nada
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

    private static void handleWalkieDropped(ServerPlayer player, UUID playerId) {
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

    public static ItemStack getHeldWalkie(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ItemWalkieTalkie) return main;
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof ItemWalkieTalkie) return off;
        return null;
    }

    public static void onPowerChanged(ServerPlayer player, boolean on) {
        UUID playerId = player.getUUID();
        if (!on) {
            if (activePlayers.contains(playerId)) {
                Plugin.unsubscribeFromAll(player);
                cleanupPlayer(playerId);
            }
        } else {
            // Forzar reconexión en el siguiente tick
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