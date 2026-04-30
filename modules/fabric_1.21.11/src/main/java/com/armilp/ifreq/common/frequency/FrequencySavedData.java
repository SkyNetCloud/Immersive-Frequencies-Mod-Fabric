package com.armilp.ifreq.common.frequency;

import com.armilp.ifreq.MainEZ;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FrequencySavedData extends PersistentState {

    private static final String DATA_NAME = "ifrequency";

    // ⚠️ NOTE: Using String for frequency keys avoids double precision issues
    private final Map<String, Double> groupToFrequency;
    private final Map<String, String> frequencyToGroup;
    private final Set<String> activeGroups;

    // =========================
    // Constructors
    // =========================

    public FrequencySavedData() {
        this.groupToFrequency = new ConcurrentHashMap<>();
        this.frequencyToGroup = new ConcurrentHashMap<>();
        this.activeGroups = ConcurrentHashMap.newKeySet();
    }

    private FrequencySavedData(Map<String, Double> groupToFrequency,
                               Map<String, String> frequencyToGroup,
                               Set<String> activeGroups) {
        this.groupToFrequency = new ConcurrentHashMap<>(groupToFrequency);
        this.frequencyToGroup = new ConcurrentHashMap<>(frequencyToGroup);
        this.activeGroups = ConcurrentHashMap.newKeySet();
        this.activeGroups.addAll(activeGroups);
    }

    // =========================
    // CODEC (replaces ALL NBT)
    // =========================

    public static final Codec<FrequencySavedData> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                            .fieldOf("group_to_frequency")
                            .forGetter(d -> d.groupToFrequency),

                    Codec.unboundedMap(Codec.STRING, Codec.STRING)
                            .fieldOf("frequency_to_group")
                            .forGetter(d -> d.frequencyToGroup),

                    Codec.STRING.listOf()
                            .fieldOf("active_groups")
                            .forGetter(d -> new ArrayList<>(d.activeGroups))
            ).apply(instance, (g2f, f2g, active) ->
                    new FrequencySavedData(g2f, f2g, new HashSet<>(active))
            ));

    // =========================
    // TYPE (new 1.21 system)
    // =========================

    public static final PersistentStateType<FrequencySavedData> TYPE =
            new PersistentStateType<>(
                    DATA_NAME,
                    FrequencySavedData::new,
                    CODEC,
                    DataFixTypes.LEVEL
            );

    // =========================
    // Access
    // =========================

    public static FrequencySavedData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    // =========================
    // Core Logic
    // =========================

    public String getOrCreateGroupName(double freq) {
        freq = FrequencyManager.roundToTenth(freq);
        String freqKey = toKey(freq);

        String existing = frequencyToGroup.get(freqKey);
        if (existing != null) {
            activeGroups.add(existing);
            return existing;
        }

        String name = generateGroupName(freq);
        addMapping(name, freq, true);
        activeGroups.add(name);

        return name;
    }

    private String generateGroupName(double freq) {
        String baseName = "freq_" + String.valueOf(freq).replace(".", "_");
        String name = baseName;
        int counter = 1;

        while (groupToFrequency.containsKey(name)) {
            name = baseName + "_" + counter++;
        }

        return name;
    }

    private void addMapping(String groupName, double frequency, boolean markDirty) {
        frequency = FrequencyManager.roundToTenth(frequency);
        String freqKey = toKey(frequency);

        String existingGroup = frequencyToGroup.get(freqKey);
        if (existingGroup != null && !existingGroup.equals(groupName)) {
            removeMapping(existingGroup);
        }

        groupToFrequency.put(groupName, frequency);
        frequencyToGroup.put(freqKey, groupName);

        if (markDirty) markDirty();
    }

    public boolean removeMapping(String groupName) {
        if (groupName == null) return false;

        Double frequency = groupToFrequency.remove(groupName);
        if (frequency != null) {
            frequencyToGroup.remove(toKey(frequency));
            activeGroups.remove(groupName);
            markDirty();
            return true;
        }
        return false;
    }

    public boolean hasGroup(double frequency) {
        return frequencyToGroup.containsKey(toKey(FrequencyManager.roundToTenth(frequency)));
    }

    public Set<Double> getAllFrequencies() {
        return new HashSet<>(groupToFrequency.values());
    }

    public Set<String> getAllGroupNames() {
        return new HashSet<>(groupToFrequency.keySet());
    }

    public Optional<String> getGroupName(double freq) {
        return Optional.ofNullable(frequencyToGroup.get(toKey(FrequencyManager.roundToTenth(freq))));
    }

    public Optional<Double> getFrequency(String groupName) {
        return Optional.ofNullable(groupToFrequency.get(groupName));
    }

    public void cleanupInactiveGroups() {
        Set<String> toRemove = new HashSet<>();

        for (String groupName : groupToFrequency.keySet()) {
            if (!activeGroups.contains(groupName)) {
                Double freq = groupToFrequency.get(groupName);
                if (freq != null && FrequencyManager.getActivePlayersOnFrequency(freq) == 0) {
                    toRemove.add(groupName);
                }
            }
        }

        int removed = 0;
        for (String groupName : toRemove) {
            if (removeMapping(groupName)) removed++;
        }

        if (removed > 0) {
            MainEZ.LOGGER.info("[iFreq] Cleaned up {} inactive frequency groups", removed);
        }
    }

    public void markGroupActive(String groupName) {
        if (groupToFrequency.containsKey(groupName)) {
            activeGroups.add(groupName);
            markDirty();
        }
    }

    public void markGroupInactive(String groupName) {
        if (activeGroups.remove(groupName)) {
            markDirty();
        }
    }

    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder("=== FrequencySavedData ===\n");
        sb.append("Mappings: ").append(groupToFrequency.size()).append("\n");
        sb.append("Active groups: ").append(activeGroups.size()).append("\n");

        for (Map.Entry<String, Double> entry : groupToFrequency.entrySet()) {
            sb.append("  '").append(entry.getKey()).append("' -> ").append(entry.getValue()).append(" MHz");
            if (activeGroups.contains(entry.getKey())) sb.append(" (active)");
            sb.append("\n");
        }

        return sb.toString();
    }

    // =========================
    // Helpers
    // =========================

    private static String toKey(double freq) {
        return String.format("%.1f", freq);
    }
}