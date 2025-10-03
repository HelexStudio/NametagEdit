package com.nametagedit.plugin.converter;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagMessages;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.utils.Utils;
import lombok.AllArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 * This class converts to and from Flatfile and MySQL.
 * Updated for modern PaperMC/Bukkit and codebase.
 */
@AllArgsConstructor
public class ConverterTask extends BukkitRunnable {

    private final boolean databaseToFile;
    private final CommandSender sender;
    private final NametagEdit plugin;

    @Override
    public void run() {
        FileConfiguration config = plugin.getConfig();
        String connectionString = "jdbc:mysql://" + config.getString("MySQL.Hostname") + ":" + config.getInt("MySQL.Port") + "/" + config.getString("MySQL.Database") + "?useSSL=false";
        try (Connection connection = DriverManager.getConnection(connectionString, config.getString("MySQL.Username"), config.getString("MySQL.Password"))) {
            if (databaseToFile) {
                convertDatabaseToFile(connection);
            } else {
                convertFilesToDatabase(connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getHandler().getStorage().reload();
                }
            }.runTask(plugin);
        }
    }

    private void convertDatabaseToFile(Connection connection) {
        try {
            final String GROUP_QUERY = "SELECT name, prefix, suffix, permission, priority FROM " + DatabaseConfig.TABLE_GROUPS;
            final String PLAYER_QUERY = "SELECT name, uuid, prefix, suffix, priority FROM " + DatabaseConfig.TABLE_PLAYERS;

            final File groupsFile = new File(plugin.getDataFolder(), "groups_CONVERTED.yml");
            final File playersFile = new File(plugin.getDataFolder(), "players_CONVERTED.yml");

            final YamlConfiguration groups = Utils.getConfig(groupsFile, "groups_CONVERTED.yml", plugin);
            final YamlConfiguration players = Utils.getConfig(playersFile, "players_CONVERTED.yml", plugin);

            try (ResultSet groupResults = connection.prepareStatement(GROUP_QUERY).executeQuery()) {
                while (groupResults.next()) {
                    groups.set("Groups." + groupResults.getString("name") + ".Permission", groupResults.getString("permission"));
                    groups.set("Groups." + groupResults.getString("name") + ".Prefix", groupResults.getString("prefix"));
                    groups.set("Groups." + groupResults.getString("name") + ".Suffix", groupResults.getString("suffix"));
                    groups.set("Groups." + groupResults.getString("name") + ".SortPriority", groupResults.getInt("priority"));
                }
            }

            try (ResultSet playerResults = connection.prepareStatement(PLAYER_QUERY).executeQuery()) {
                while (playerResults.next()) {
                    players.set("Players." + playerResults.getString("uuid") + ".Name", playerResults.getString("name"));
                    players.set("Players." + playerResults.getString("uuid") + ".Prefix", playerResults.getString("prefix"));
                    players.set("Players." + playerResults.getString("uuid") + ".Suffix", playerResults.getString("suffix"));
                    players.set("Players." + playerResults.getString("uuid") + ".SortPriority", playerResults.getInt("priority"));
                }
            }

            groups.save(groupsFile);
            players.save(playersFile);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void convertFilesToDatabase(Connection connection) {
        final File groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        final File playersFile = new File(plugin.getDataFolder(), "players.yml");

        final YamlConfiguration groups = Utils.getConfig(groupsFile, "groups.yml", plugin);
        final YamlConfiguration players = Utils.getConfig(playersFile, "players.yml", plugin);

        if (players != null && checkValid(players, "Players")) {
            try (PreparedStatement playerInsert = connection.prepareStatement(
                    "INSERT INTO " + DatabaseConfig.TABLE_PLAYERS + " (`uuid`, `name`, `prefix`, `suffix`, `priority`) VALUES (?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?, `priority`=?")) {
                for (String key : players.getConfigurationSection("Players").getKeys(false)) {
                    playerInsert.setString(1, key);
                    playerInsert.setString(2, players.getString("Players." + key + ".Name"));
                    playerInsert.setString(3, Utils.deformat(players.getString("Players." + key + ".Prefix", "")));
                    playerInsert.setString(4, Utils.deformat(players.getString("Players." + key + ".Suffix", "")));
                    playerInsert.setInt(5, players.getInt("Players." + key + ".SortPriority", -1));
                    playerInsert.setString(6, Utils.deformat(players.getString("Players." + key + ".Prefix", "")));
                    playerInsert.setString(7, Utils.deformat(players.getString("Players." + key + ".Suffix", "")));
                    playerInsert.setInt(8, players.getInt("Players." + key + ".SortPriority", -1));
                    playerInsert.addBatch();
                }
                playerInsert.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (groups != null && checkValid(groups, "Groups")) {
            try (PreparedStatement groupInsert = connection.prepareStatement(
                    "INSERT INTO " + DatabaseConfig.TABLE_GROUPS + " (`name`, `permission`, `prefix`, `suffix`, `priority`) VALUES (?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?, `permission`=?, `priority`=?")) {
                for (String key : groups.getConfigurationSection("Groups").getKeys(false)) {
                    groupInsert.setString(1, key);
                    groupInsert.setString(2, groups.getString("Groups." + key + ".Permission"));
                    groupInsert.setString(3, Utils.deformat(groups.getString("Groups." + key + ".Prefix", "")));
                    groupInsert.setString(4, Utils.deformat(groups.getString("Groups." + key + ".Suffix", "")));
                    groupInsert.setInt(5, groups.getInt("Groups." + key + ".SortPriority", -1));
                    groupInsert.setString(6, Utils.deformat(groups.getString("Groups." + key + ".Prefix", "")));
                    groupInsert.setString(7, Utils.deformat(groups.getString("Groups." + key + ".Suffix", "")));
                    groupInsert.setString(8, groups.getString("Groups." + key + ".Permission"));
                    groupInsert.setInt(9, groups.getInt("Groups." + key + ".SortPriority", -1));
                    groupInsert.addBatch();
                }
                groupInsert.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkValid(FileConfiguration configuration, String section) {
        if (!configuration.contains(section)) {
            NametagMessages.FILE_MISCONFIGURED.send(sender, section + ".yml");
            return false;
        }
        return true;
    }
}