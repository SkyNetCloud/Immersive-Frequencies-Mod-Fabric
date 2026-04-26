package com.armilp.ifreq.common.frequency;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignalRegistry {

    public record Signal(ResourceKey<Level> dimension, BlockPos pos, double frequency, double strength) {}

    private static final Map<Double, List<Signal>> signalsByFrequency = new HashMap<>();

    public static void registerSignal(Signal signal) {
        signalsByFrequency.computeIfAbsent(signal.frequency(), f -> new ArrayList<>()).add(signal);
    }

    public static void unregisterSignal(Signal signal) {
        List<Signal> signals = signalsByFrequency.get(signal.frequency());
        if (signals != null) {
            signals.remove(signal);
            if (signals.isEmpty()) signalsByFrequency.remove(signal.frequency());
        }
    }

    public static List<Signal> getSignalsNear(ServerLevel world, BlockPos pos, double frequency, double maxDistance) {
        List<Signal> result = new ArrayList<>();
        List<Signal> signals = signalsByFrequency.get(frequency);

        if (signals != null) {
            double maxDistSq = maxDistance * maxDistance;
            for (Signal s : signals) {
                if (s.dimension().equals(world.dimension()) && s.pos().distSqr(pos) <= maxDistSq) {
                    result.add(s);
                }
            }
        }
        return result;
    }
}