package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DataDownloader extends BukkitRunnable {

    private final List<UUID> players = new ArrayList<>();
    private final NametagHandler handler;
    private final HikariDataSource hikari;

    public DataDownloader(NametagHandler handler, HikariDataSource hikari) {
        this.handler = handler;
        this.hikari = hikari;
        for (Player player : Utils.getOnline()) {
            players.add(player.getUniqueId());
        }
    }

    @Override
    public void run() {
        HashMap<String, String> settings = new HashMap<>();
        List<GroupData> groupDataUnordered = new ArrayList<>();
        final Map<UUID, PlayerData> playerData = new HashMap<>();

        try (Connection connection = hikari.getConnection()) {
            // Download groups
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT `name`, `prefix`, `suffix`, `permission`, `priority` FROM " + DatabaseConfig.TABLE_GROUPS);
                 ResultSet results = ps.executeQuery()) {
                while (results.next()) {
                    groupDataUnordered.add(new GroupData(
                            results.getString("name"),
                            results.getString("prefix"),
                            results.getString("suffix"),
                            results.getString("permission"),
                            new Permission(results.getString("permission"), PermissionDefault.FALSE),
                            results.getInt("priority")
                    ));
                }
            }

            // Download players
            try (PreparedStatement select = connection.prepareStatement(
                    "SELECT `uuid`, `prefix`, `suffix`, `priority` FROM " + DatabaseConfig.TABLE_PLAYERS + " WHERE uuid=?")) {
                for (UUID uuid : players) {
                    select.setString(1, uuid.toString());
                    try (ResultSet results = select.executeQuery()) {
                        if (results.next()) {
                            playerData.put(uuid, new PlayerData(
                                    "",
                                    uuid,
                                    Utils.format(results.getString("prefix")),
                                    Utils.format(results.getString("suffix")),
                                    results.getInt("priority")
                            ));
                        }
                    }
                }
            }

            // Download config/settings
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT `setting`,`value` FROM " + DatabaseConfig.TABLE_CONFIG);
                 ResultSet results = ps.executeQuery()) {
                while (results.next()) {
                    settings.put(results.getString("setting"), results.getString("value"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Order groups if needed
            String orderSetting = settings.get("order");
            if (orderSetting != null) {
                String[] order = orderSetting.split(" ");
                List<GroupData> ordered = new ArrayList<>();
                for (String group : order) {
                    Iterator<GroupData> itr = groupDataUnordered.iterator();
                    while (itr.hasNext()) {
                        GroupData groupData = itr.next();
                        if (groupData.getGroupName().equalsIgnoreCase(group)) {
                            ordered.add(groupData);
                            itr.remove();
                            break;
                        }
                    }
                }
                ordered.addAll(groupDataUnordered); // Remaining entries
                groupDataUnordered = ordered;
            }

            handler.assignData(groupDataUnordered, playerData);

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Utils.getOnline()) {
                        PlayerData data = playerData.get(player.getUniqueId());
                        if (data != null) {
                            data.setName(player.getName());
                        }
                    }
                    handler.applyTags();
                }
            }.runTask(handler.getPlugin());
        }
    }
}