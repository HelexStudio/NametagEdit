package com.nametagedit.plugin.storage;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public interface AbstractConfig {

    void load();

    void reload();

    void shutdown();

    void savePlayer(String playerName, String prefix, String suffix);

    void clearPlayer(String playerName);

    void save(GroupData... groupData);

    void savePriority(boolean playerTag, String key, int priority);

    void delete(GroupData groupData);

    void addGroup(GroupData groupData);

    void orderGroups(CommandSender commandSender, List<String> order);

    void load(Player player, boolean loggedIn);
    void save(PlayerData... playerData);
    void clear(UUID uuid, String targetName);
}