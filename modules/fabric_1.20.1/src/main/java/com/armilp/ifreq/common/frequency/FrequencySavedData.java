package com.armilp.ifreq.common.frequency;

import com.armilp.ifreq.MainEZ;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FrequencySavedData extends PersistentState {

    private static final String DATA_NAME = "ifrequency";
    private static final int CURRENT_VERSION = 1;

    // NBT Keys
    private static final String KEY_VERSION = "version";
    private static final String KEY_GROUP_TO_FREQUENCY = "group_to_frequency";
    private static final String KEY_FREQUENCY_TO_GROUP = "frequency_to_group";
    private static final String KEY_ACTIVE_GROUPS = "active_groups";
    private static final String KEY_GROUP_NAME = "group_name";
    private static final String KEY_FREQUENCY = "frequency";

    private final Map<String, Double> groupToFrequency;
    private final Map<Double, String> frequencyToGroup;
    private final Set<String> activeGroups;

    public FrequencySavedData() {
        this.groupToFrequency = new ConcurrentHashMap<>();
        this.frequencyToGroup = new ConcurrentHashMap<>();
        this.activeGroups = ConcurrentHashMap.newKeySet();
    }

    private FrequencySavedData(Map<String, Double> groupToFrequency,
                               Map<Double, String> frequencyToGroup,
                               Set<String> activeGroups) {
        this.groupToFrequency = new ConcurrentHashMap<>(groupToFrequency);
        this.frequencyToGroup = new ConcurrentHashMap<>(frequencyToGroup);
        this.activeGroups = ConcurrentHashMap.newKeySet();
        this.activeGroups.addAll(activeGroups);
    }

    public static FrequencySavedData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                FrequencySavedData::createFromNbt,
                FrequencySavedData::new,
                DATA_NAME
        );
    }

    public static FrequencySavedData createFromNbt(NbtCompound nbt) {
        FrequencySavedData data = new FrequencySavedData();

        int version = nbt.getInt(KEY_VERSION);

        // Load group_to_frequency
        NbtCompound groupToFreqNbt = nbt.getCompound(KEY_GROUP_TO_FREQUENCY);
        for (String groupName : groupToFreqNbt.getKeys()) {
            double frequency = groupToFreqNbt.getDouble(groupName);
            data.groupToFrequency.put(groupName, frequency);
        }

        // Load frequency_to_group
        NbtCompound freqToGroupNbt = nbt.getCompound(KEY_FREQUENCY_TO_GROUP);
        for (String freqStr : freqToGroupNbt.getKeys()) {
            double frequency = Double.parseDouble(freqStr);
            String groupName = freqToGroupNbt.getString(freqStr);
            data.frequencyToGroup.put(frequency, groupName);
        }

        // Load active_groups
        NbtList activeGroupsList = nbt.getList(KEY_ACTIVE_GROUPS, NbtElement.STRING_TYPE);
        for (int i = 0; i < activeGroupsList.size(); i++) {
            data.activeGroups.add(activeGroupsList.getString(i));
        }

        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt(KEY_VERSION, CURRENT_VERSION);

        // Save group_to_frequency
        NbtCompound groupToFreqNbt = new NbtCompound();
        for (Map.Entry<String, Double> entry : groupToFrequency.entrySet()) {
            groupToFreqNbt.putDouble(entry.getKey(), entry.getValue());
        }
        nbt.put(KEY_GROUP_TO_FREQUENCY, groupToFreqNbt);

        // Save frequency_to_group (convert double keys to strings for NBT)
        NbtCompound freqToGroupNbt = new NbtCompound();
        for (Map.Entry<Double, String> entry : frequencyToGroup.entrySet()) {
            freqToGroupNbt.putString(String.valueOf(entry.getKey()), entry.getValue());
        }
        nbt.put(KEY_FREQUENCY_TO_GROUP, freqToGroupNbt);

        // Save active_groups
        NbtList activeGroupsList = new NbtList();
        for (String groupName : activeGroups) {
            activeGroupsList.add(NbtString.of(groupName));
        }
        nbt.put(KEY_ACTIVE_GROUPS, activeGroupsList);

        return nbt;
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
}