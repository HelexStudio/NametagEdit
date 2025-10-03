package com.nametagedit.plugin.storage.flatfile;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.AbstractConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FlatFileConfig implements AbstractConfig {

    private final Plugin plugin;
    private final NametagHandler handler;
    private File playersFile;
    private YamlConfiguration playersConfig;
    private File groupsFile;
    private YamlConfiguration groupsConfig;
    private final Yaml yaml;

    public FlatFileConfig(Plugin plugin, NametagHandler handler) {
        this.plugin = plugin;
        this.handler = handler;

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.SINGLE_QUOTED);
        options.setPrettyFlow(true);
        this.yaml = new Yaml(options);

        loadFiles();
    }

    private void saveConfiguration(YamlConfiguration config, File file) {
        try (Writer writer = new FileWriter(file)) {
            yaml.dump(config.getValues(false), writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + file.getName() + ": " + e.getMessage());
        }
    }

    private void loadFiles() {
        playersFile = new File(plugin.getDataFolder(), "players.yml");
        if (!playersFile.exists()) {
            plugin.saveResource("players.yml", false);
        }
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);

        groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        if (!groupsFile.exists()) {
            plugin.saveResource("groups.yml", false);
        }
        groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);
    }

    @Override
    public void load() {
        loadFiles();
        List<GroupData> groups = new ArrayList<>();
        if (groupsConfig.isConfigurationSection("Groups")) {
            for (String groupName : groupsConfig.getConfigurationSection("Groups").getKeys(false)) {
                String path = "Groups." + groupName;
                GroupData group = new GroupData();
                group.setGroupName(groupName);
                group.setPrefix(groupsConfig.getString(path + ".Prefix", ""));
                group.setSuffix(groupsConfig.getString(path + ".Suffix", ""));
                group.setPermission(groupsConfig.getString(path + ".Permission", "nte." + groupName));
                group.setSortPriority(groupsConfig.getInt(path + ".SortPriority", 99));
                groups.add(group);
            }
        }
        handler.assignGroupData(groups);

        if (playersConfig.isConfigurationSection("Players")) {
            for (String uuid : playersConfig.getConfigurationSection("Players").getKeys(false)) {
                String path = "Players." + uuid;
                String name = playersConfig.getString(path + ".Name", "");
                String prefix = playersConfig.getString(path + ".Prefix", "");
                String suffix = playersConfig.getString(path + ".Suffix", "");
                int priority = playersConfig.getInt(path + ".SortPriority", 99);
                try {
                    PlayerData data = new PlayerData(name, UUID.fromString(uuid), prefix, suffix, priority);
                    handler.storePlayerData(data.getUuid(), data);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    @Override
    public void reload() {
        handler.clearMemoryData();
        load();
        handler.applyTags();
    }

    @Override
    public void savePlayer(String playerName, String prefix, String suffix) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return;
        String uuid = player.getUniqueId().toString();
        String path = "Players." + uuid;

        playersConfig.set(path + ".Name", playerName);
        if (prefix != null) playersConfig.set(path + ".Prefix", prefix);
        if (suffix != null) playersConfig.set(path + ".Suffix", suffix);

        saveConfiguration(playersConfig, playersFile);
    }

    @Override
    public void clearPlayer(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return;
        String uuid = player.getUniqueId().toString();
        playersConfig.set("Players." + uuid, null);
        saveConfiguration(playersConfig, playersFile);
    }

    @Override
    public void save(GroupData... groupData) {
        for (GroupData group : groupData) {
            String path = "Groups." + group.getGroupName();
            groupsConfig.set(path + ".Prefix", group.getPrefix());
            groupsConfig.set(path + ".Suffix", group.getSuffix());
            groupsConfig.set(path + ".Permission", group.getPermission());
            groupsConfig.set(path + ".SortPriority", group.getSortPriority());
        }
        saveConfiguration(groupsConfig, groupsFile);
    }

    @Override
    public void savePriority(boolean playerTag, String key, int priority) {
        if (playerTag) {
            Player player = Bukkit.getPlayerExact(key);
            if (player == null) return;
            String uuid = player.getUniqueId().toString();
            playersConfig.set("Players." + uuid + ".SortPriority", priority);
            saveConfiguration(playersConfig, playersFile);
        } else {
            groupsConfig.set("Groups." + key + ".SortPriority", priority);
            saveConfiguration(groupsConfig, groupsFile);
        }
    }

    @Override
    public void delete(GroupData groupData) {
        groupsConfig.set("Groups." + groupData.getGroupName(), null);
        saveConfiguration(groupsConfig, groupsFile);
    }

    @Override
    public void addGroup(GroupData groupData) {
        save(groupData);
    }

    @Override
    public void orderGroups(CommandSender commandSender, List<String> order) {
        ConfigurationSection section = groupsConfig.getConfigurationSection("Groups");
        if (section == null) {
            commandSender.sendMessage("§cNo groups found in groups.yml!");
            return;
        }
        int priority = 1;
        for (String groupName : order) {
            if (section.contains(groupName)) {
                section.set(groupName + ".SortPriority", priority++);
            }
        }
        saveConfiguration(groupsConfig, groupsFile);
    }

    @Override public void shutdown() {}
    @Override public void load(Player player, boolean loggedIn) {}
    @Override public void save(PlayerData... playerData) {}
    @Override public void clear(UUID uuid, String targetName) {}
}