package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.api.data.PlayerData;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Asynchronously saves or updates multiple players in the database.
 * Uses batching and proper column assignment.
 */
@AllArgsConstructor
public class PlayerSaver extends BukkitRunnable {

    private final PlayerData[] playerData;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        final String QUERY = "INSERT INTO " + DatabaseConfig.TABLE_PLAYERS +
                " (`uuid`, `name`, `prefix`, `suffix`, `priority`) VALUES (?, ?, ?, ?, ?)" +
                " ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?, `priority`=?";
        try (Connection connection = hikari.getConnection();
             PreparedStatement insertOrUpdate = connection.prepareStatement(QUERY)) {

            for (PlayerData player : this.playerData) {
                insertOrUpdate.setString(1, player.getUuid().toString());
                insertOrUpdate.setString(2, player.getName());
                insertOrUpdate.setString(3, Utils.deformat(player.getPrefix()));
                insertOrUpdate.setString(4, Utils.deformat(player.getSuffix()));
                insertOrUpdate.setInt(5, player.getSortPriority());
                // ON DUPLICATE KEY UPDATE values
                insertOrUpdate.setString(6, Utils.deformat(player.getPrefix()));
                insertOrUpdate.setString(7, Utils.deformat(player.getSuffix()));
                insertOrUpdate.setInt(8, player.getSortPriority());
                insertOrUpdate.addBatch();
            }

            insertOrUpdate.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}