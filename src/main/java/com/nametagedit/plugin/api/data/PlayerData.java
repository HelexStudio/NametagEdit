package com.nametagedit.plugin.api.data;

import org.bukkit.configuration.file.YamlConfiguration;
import java.util.UUID;

public class PlayerData implements INametag {

    private String name;
    private UUID uuid;
    private String prefix;
    private String suffix;
    private int sortPriority;

    public PlayerData() {}

    public PlayerData(String name, UUID uuid, String prefix, String suffix, int sortPriority) {
        this.name = name;
        this.uuid = uuid;
        this.prefix = prefix;
        this.suffix = suffix;
        this.sortPriority = sortPriority;
    }

    public static PlayerData fromFile(String uuid, YamlConfiguration config) {
        String path = "Players." + uuid;
        if (!config.contains(path)) return null;
        String name = config.getString(path + ".Name", "");
        String prefix = config.getString(path + ".Prefix", "");
        String suffix = config.getString(path + ".Suffix", "");
        int sortPriority = config.getInt(path + ".SortPriority", -1);
        try {
            return new PlayerData(name, UUID.fromString(uuid), prefix, suffix, sortPriority);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public boolean isPlayerTag() { return true; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public int getSortPriority() { return sortPriority; }
    public void setSortPriority(int sortPriority) { this.sortPriority = sortPriority; }
}