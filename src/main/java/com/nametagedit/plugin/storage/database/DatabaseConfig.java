package com.nametagedit.plugin.storage.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConfig {

    public static final String TABLE_GROUPS = "nametagedit_groups";
    public static final String TABLE_PLAYERS = "nametagedit_players";
    public static final String TABLE_CONFIG = "nametagedit_config";

    private final HikariDataSource dataSource;

    public DatabaseConfig(String host, int port, String database, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false");
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setPoolName("NametagEditHikariPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.dataSource = new HikariDataSource(config);
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}