package com.armilp.ifreq.common.frequency;

import com.armilp.ifreq.MainEZ;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FrequencySavedData extends SavedData {

    private static final String DATA_NAME = "ifrequency";
    private static final String MAPPINGS_KEY = "mappings";
    private static final String METADATA_KEY = "metadata";
    private static final String VERSION_KEY = "version";
    private static final int CURRENT_VERSION = 1;

    private final Map<String, Double> groupToFrequency;
    private final Map<Double, String> frequencyToGroup;
    private final Set<String> activeGroups;

    public FrequencySavedData() {
        this.groupToFrequency = new ConcurrentHashMap<>();
        this.frequencyToGroup = new ConcurrentHashMap<>();
        this.activeGroups = ConcurrentHashMap.newKeySet();
    }

    public static FrequencySavedData get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("Cannot access world data from client side!");
        }
        return serverLevel.getDataStorage().computeIfAbsent(
                FrequencySavedData::load,
                FrequencySavedData::new,
                DATA_NAME
        );
    }

    public Optional<String> getGroupName(double freq) {
        return Optional.ofNullable(frequencyToGroup.get(FrequencyManager.roundToTenth(freq)));
    }

    public Optional<Double> getFrequency(String groupName) {
        return Optional.ofNullable(groupToFrequency.get(groupName));
    }

    private static FrequencySavedData load(CompoundTag tag) {
        FrequencySavedData data = new FrequencySavedData();

        int version = tag.getInt(VERSION_KEY);
        if (version > CURRENT_VERSION) {
            throw new IllegalStateException("Unsupported data version: " + version);
        }

        if (tag.contains(MAPPINGS_KEY)) {
            CompoundTag mappings = tag.getCompound(MAPPINGS_KEY);
            for (String key : mappings.getAllKeys()) {
                data.addMapping(key, mappings.getDouble(key), false);
            }
        }

        if (tag.contains("active_groups")) {
            ListTag activeGroupsList = tag.getList("active_groups", Tag.TAG_STRING);
            for (int i = 0; i < activeGroupsList.size(); i++) {
                data.activeGroups.add(activeGroupsList.getString(i));
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(VERSION_KEY, CURRENT_VERSION);

        CompoundTag mappings = new CompoundTag();
        for (Map.Entry<String, Double> entry : groupToFrequency.entrySet()) {
            mappings.putDouble(entry.getKey(), entry.getValue());
        }
        tag.put(MAPPINGS_KEY, mappings);

        ListTag activeGroupsList = new ListTag();
        for (String group : activeGroups) {
            activeGroupsList.add(StringTag.valueOf(group));
        }
        tag.put("active_groups", activeGroupsList);

        CompoundTag metadata = new CompoundTag();
        metadata.putLong("last_saved", System.currentTimeMillis());
        metadata.putInt("total_groups", groupToFrequency.size());
        tag.put(METADATA_KEY, metadata);

        return tag;
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

        if (markDirty) setDirty();
    }

    public boolean removeMapping(String groupName) {
        if (groupName == null) return false;

        Double frequency = groupToFrequency.remove(groupName);
        if (frequency != null) {
            frequencyToGroup.remove(frequency);
            activeGroups.remove(groupName);
            setDirty();
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