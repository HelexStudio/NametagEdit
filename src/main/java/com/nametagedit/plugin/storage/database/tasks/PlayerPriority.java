package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Asynchronously updates a player's priority value in the database.
 */
@AllArgsConstructor
public class PlayerPriority extends BukkitRunnable {

    private final UUID player;
    private final int priority;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        final String QUERY = "UPDATE " + DatabaseConfig.TABLE_PLAYERS + " SET `priority`=? WHERE `uuid`=?";
        try (Connection connection = hikari.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY)) {
            preparedStatement.setInt(1, priority);
            preparedStatement.setString(2, player.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}