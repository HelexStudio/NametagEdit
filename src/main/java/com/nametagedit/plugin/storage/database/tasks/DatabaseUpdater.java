package com.nametagedit.plugin.storage.database.tasks;

import com.nametagedit.plugin.NametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.storage.database.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles creation and migration of NametagEdit database tables.
 * Runs async, then triggers a DataDownloader to load fresh data.
 */
@AllArgsConstructor
public class DatabaseUpdater extends BukkitRunnable {

    private final NametagHandler handler;
    private final HikariDataSource hikari;
    private final NametagEdit plugin;

    private static final int CURRENT_DATABASE_VERSION = 5;

    @Override
    public void run() {
        try (Connection connection = hikari.getConnection()) {
            int currentVersion = getCurrentDatabaseVersion(connection);

            createTablesIfNotExists(connection);

            while (currentVersion < CURRENT_DATABASE_VERSION) {
                switch (currentVersion) {
                    case 1: handleUpdate1(connection); break;
                    case 2: handleUpdate2(connection); break;
                    case 3: handleUpdate3(connection); break;
                    case 4: handleUpdate4(connection); break;
                }
                currentVersion++;
            }

            setCurrentDatabaseVersion(connection, CURRENT_DATABASE_VERSION);
        } catch (SQLException e) {
            handleError(e);
        } finally {
            new DataDownloader(handler, hikari).runTaskAsynchronously(plugin);
        }
    }

    private void createTablesIfNotExists(Connection connection) {
        execute(connection, "CREATE TABLE IF NOT EXISTS " + DatabaseConfig.TABLE_CONFIG +
                " (`setting` varchar(16) NOT NULL, `value` varchar(200) NOT NULL, PRIMARY KEY (`setting`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        execute(connection, "CREATE TABLE IF NOT EXISTS " + DatabaseConfig.TABLE_GROUPS +
                " (`name` varchar(64) NOT NULL, `permission` varchar(64) DEFAULT NULL, `prefix` varchar(256) NOT NULL, `suffix` varchar(256) NOT NULL, `priority` int(11) NOT NULL, PRIMARY KEY (`name`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        execute(connection, "CREATE TABLE IF NOT EXISTS " + DatabaseConfig.TABLE_PLAYERS +
                " (`uuid` varchar(64) NOT NULL, `name` varchar(16) NOT NULL, `prefix` varchar(256) NOT NULL, `suffix` varchar(256) NOT NULL, `priority` int(11) NOT NULL, PRIMARY KEY (`uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void handleUpdate1(Connection connection) {
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_PLAYERS + " ADD COLUMN `priority` INT NOT NULL DEFAULT 0");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " ADD COLUMN `priority` INT NOT NULL DEFAULT 0");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " MODIFY COLUMN `permission` VARCHAR(64)");
    }

    private void handleUpdate2(Connection connection) {
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " MODIFY COLUMN `prefix` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " MODIFY COLUMN `suffix` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_PLAYERS + " MODIFY COLUMN `prefix` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_PLAYERS + " MODIFY COLUMN `suffix` VARCHAR(64) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL");
    }

    private void handleUpdate3(Connection connection) {
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " CONVERT TO CHARACTER SET utf8mb4");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_PLAYERS + " CONVERT TO CHARACTER SET utf8mb4");
        // TODO: Add more queries for Issue #230 if needed
    }

    private void handleUpdate4(Connection connection) {
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " MODIFY COLUMN `prefix` VARCHAR(256)");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_GROUPS + " MODIFY COLUMN `suffix` VARCHAR(256)");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_PLAYERS + " MODIFY COLUMN `prefix` VARCHAR(256)");
        execute(connection, "ALTER TABLE " + DatabaseConfig.TABLE_PLAYERS + " MODIFY COLUMN `suffix` VARCHAR(256)");
    }

    private void setCurrentDatabaseVersion(Connection connection, int currentVersion) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO " + DatabaseConfig.TABLE_CONFIG + " (`setting`, `value`) VALUES ('db_version', ?) ON DUPLICATE KEY UPDATE `value`=?")) {
            ps.setInt(1, currentVersion);
            ps.setInt(2, currentVersion);
            ps.executeUpdate();
        } catch (SQLException e) {
            handleError(e);
        }
    }

    private int getCurrentDatabaseVersion(Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT `value` FROM " + DatabaseConfig.TABLE_CONFIG + " WHERE `setting`='db_version'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("value");
                }
            }
        } catch (SQLException e) {
            handleError(e);
        }
        return 1;
    }

    private void execute(Connection connection, String query) {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.execute();
        } catch (SQLException e) {
            handleError(e);
        }
    }

    private void handleError(SQLException e) {
        if (handler.isDebug()) {
            e.printStackTrace();
        } else {
            plugin.getLogger().severe("NametagEdit Query Failed - Reason: " + e.getMessage());
            plugin.getLogger().severe("If this is not a connection error, please enable debug with /nte debug and post the error on our GitHub Issue Tracker.");
        }
    }

}