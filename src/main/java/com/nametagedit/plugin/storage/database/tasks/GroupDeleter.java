package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Asynchronously deletes a group from the database.
 */
@AllArgsConstructor
public class GroupDeleter extends BukkitRunnable {

    private final String groupName;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        final String QUERY = "DELETE FROM " + DatabaseConfig.TABLE_GROUPS + " WHERE `name`=?";
        try (Connection connection = hikari.getConnection();
             PreparedStatement delete = connection.prepareStatement(QUERY)) {
            delete.setString(1, groupName);
            delete.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}