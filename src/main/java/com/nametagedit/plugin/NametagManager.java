package com.nametagedit.plugin;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class NametagManager {

    private final Map<UUID, PlayerData> playerData = new HashMap<>();
    private final Map<String, GroupData> groupData = new HashMap<>();

    // Store a player's nametag data
    public void storePlayerData(UUID uuid, PlayerData data) {
        playerData.put(uuid, data);
    }

    // Remove player data
    public void removePlayerData(UUID uuid) {
        playerData.remove(uuid);
    }

    // Get player data
    public PlayerData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    // Get all player data
    public Collection<PlayerData> getAllPlayerData() {
        return playerData.values();
    }

    // Store group data
    public void storeGroupData(String groupName, GroupData data) {
        groupData.put(groupName, data);
    }

    // Remove group data
    public void removeGroupData(String groupName) {
        groupData.remove(groupName);
    }

    // Get group data
    public GroupData getGroupData(String groupName) {
        return groupData.get(groupName);
    }

    // Get all group data
    public Collection<GroupData> getAllGroupData() {
        return groupData.values();
    }

    // Clear all in-memory data
    public void clearAllData() {
        playerData.clear();
        groupData.clear();
    }

    // Set a player's nametag (prefix/suffix). If prefix or suffix is null, only update the other.
    public void setNametag(String playerName, String prefix, String suffix) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        PlayerData data = playerData.get(uuid);
        if (data == null) {
            data = new PlayerData(playerName, uuid, prefix != null ? prefix : "", suffix != null ? suffix : "", -1);
            playerData.put(uuid, data);
        } else {
            if (prefix != null) data.setPrefix(prefix);
            if (suffix != null) data.setSuffix(suffix);
        }

        player.setDisplayName(
                (data.getPrefix() != null ? data.getPrefix() : "") +
                        player.getName() +
                        (data.getSuffix() != null ? data.getSuffix() : "")
        );
    }

    // Reset a player's nametag, reverting display name to their username.
    public void reset(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        playerData.remove(uuid);
        player.setDisplayName(player.getName());
    }
}