package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.nametagedit.plugin.utils.Utils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Asynchronously saves changes to multiple groups in the database.
 * Uses batching and try-with-resources.
 */
@AllArgsConstructor
public class GroupSaver extends BukkitRunnable {

    private final GroupData[] groupData;
    private final HikariDataSource hikari;

    @Override
    public void run() {
        final String QUERY = "UPDATE " + DatabaseConfig.TABLE_GROUPS +
                " SET `prefix`=?, `suffix`=?, `permission`=?, `priority`=? WHERE `name`=?";
        try (Connection connection = hikari.getConnection();
             PreparedStatement update = connection.prepareStatement(QUERY)) {

            for (GroupData group : this.groupData) {
                update.setString(1, Utils.deformat(group.getPrefix()));
                update.setString(2, Utils.deformat(group.getSuffix()));
                update.setString(3, group.getPermission());
                update.setInt(4, group.getSortPriority());
                update.setString(5, group.getGroupName());
                update.addBatch();
            }
            update.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
            // Optionally: log to plugin logger if available
        }
    }
}