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
 * Asynchronously deletes a player from the database by UUID.
 */
@AllArgsConstructor
public class PlayerDeleter extends BukkitRunnable {

    private final UUID uuid;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        final String QUERY = "DELETE FROM " + DatabaseConfig.TABLE_PLAYERS + " WHERE `uuid`=?";
        try (Connection connection = hikari.getConnection();
             PreparedStatement delete = connection.prepareStatement(QUERY)) {
            delete.setString(1, uuid.toString());
            delete.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}