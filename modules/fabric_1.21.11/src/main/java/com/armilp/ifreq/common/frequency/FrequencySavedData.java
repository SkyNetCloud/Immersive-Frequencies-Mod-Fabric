package com.armilp.ifreq.common.frequency;

import com.armilp.ifreq.MainEZ;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FrequencySavedData extends PersistentState {

    private static final String DATA_NAME = "ifrequency";
    private static final int CURRENT_VERSION = 1;

    private final Map<String, Double> groupToFrequency;
    private final Map<Double, String> frequencyToGroup;
    private final Set<String> activeGroups;

    // Codec for serialization/deserialization
    public static final Codec<FrequencySavedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                            .fieldOf("group_to_frequency")
                            .forGetter(data -> data.groupToFrequency),
                    Codec.unboundedMap(Codec.DOUBLE, Codec.STRING)
                            .fieldOf("frequency_to_group")
                            .forGetter(data -> data.frequencyToGroup),
                    Codec.list(Codec.STRING)
                            .xmap(
                                    HashSet::new,
                                    ArrayList::new
                            )
                            .fieldOf("active_groups")
                            .forGetter(data -> (HashSet<String>) data.activeGroups)
            ).apply(instance, FrequencySavedData::new)
    );

    // Constructor for codec
    public FrequencySavedData(Map<String, Double> groupToFrequency,
                              Map<Double, String> frequencyToGroup,
                              Set<String> activeGroups) {
        this.groupToFrequency = new ConcurrentHashMap<>(groupToFrequency);
        this.frequencyToGroup = new ConcurrentHashMap<>(frequencyToGroup);
        this.activeGroups = ConcurrentHashMap.newKeySet();
        this.activeGroups.addAll(activeGroups);
    }

    public FrequencySavedData() {
        this.groupToFrequency = new ConcurrentHashMap<>();
        this.frequencyToGroup = new ConcurrentHashMap<>();
        this.activeGroups = ConcurrentHashMap.newKeySet();
    }

    public static PersistentStateType<FrequencySavedData> getType() {
        return new PersistentStateType<>(
                DATA_NAME,                    // String name
                FrequencySavedData::new,      // Supplier<T> constructor
                CODEC, // Codec<T>
                null // datafixer (not needed since we handle versioning manually)
        );
    }

    public static FrequencySavedData get(World world) {
        if (!(world instanceof ServerWorld serverWorld)) {
            throw new IllegalStateException("Cannot access world data from client side!");
        }
        PersistentStateManager manager = serverWorld.getPersistentStateManager();
        return manager.getOrCreate(getType());
    }

    public String getOrCreateGroupName(double freq) {
        freq = FrequencyManager.roundToTenth(freq);

        String existing = frequencyToGroup.get(freq);
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

        String existingGroup = frequencyToGroup.get(frequency);
        if (existingGroup != null && !existingGroup.equals(groupName)) {
            removeMapping(existingGroup);
        }

        groupToFrequency.put(groupName, frequency);
        frequencyToGroup.put(frequency, groupName);

        if (markDirty) markDirty();
    }

    public boolean removeMapping(String groupName) {
        if (groupName == null) return false;

        Double frequency = groupToFrequency.remove(groupName);
        if (frequency != null) {
            frequencyToGroup.remove(frequency);
            activeGroups.remove(groupName);
            markDirty();
            return true;
        }
        return false;
    }

    public boolean hasGroup(double frequency) {
        return frequencyToGroup.containsKey(FrequencyManager.roundToTenth(frequency));
    }

    public Set<Double> getAllFrequencies() {
        return new HashSet<>(frequencyToGroup.keySet());
    }

    public Set<String> getAllGroupNames() {
        return new HashSet<>(groupToFrequency.keySet());
    }

    public Optional<String> getGroupName(double freq) {
        return Optional.ofNullable(frequencyToGroup.get(FrequencyManager.roundToTenth(freq)));
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
        if (groupToFrequency.containsKey(groupName)) activeGroups.add(groupName);
    }

    public void markGroupInactive(String groupName) {
        activeGroups.remove(groupName);
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
}