package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Updates a config setting in the nametagedit_config table.
 */
@AllArgsConstructor
public class GroupConfigUpdater extends BukkitRunnable {

    private final String setting;
    private final String value;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        final String QUERY = "INSERT INTO " + DatabaseConfig.TABLE_CONFIG + " (`setting`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `value`=?";
        try (Connection connection = hikari.getConnection();
             PreparedStatement update = connection.prepareStatement(QUERY)) {
            update.setString(1, setting);
            update.setString(2, value);
            update.setString(3, value);
            update.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}