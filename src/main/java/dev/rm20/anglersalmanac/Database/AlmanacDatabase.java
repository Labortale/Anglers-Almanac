package dev.rm20.anglersalmanac.Database;

import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlmanacDatabase {
    private static final String DB_PATH = "mods/AnglersAlmanac/Data/almanac.db";
    private Connection connection;

    public AlmanacDatabase() {
        init();
    }

    private void init() {
        try {
            File dir = new File("mods/AnglersAlmanac/Data/");
            if (!dir.exists()) dir.mkdirs();
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                System.err.println("[AnglersAlmanac] SQLite Driver not found in classpath!");
                return;
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Player's overall stats
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "total_catches INTEGER DEFAULT 0)");

            // fish counter per player
            stmt.execute("CREATE TABLE IF NOT EXISTS catches (" +
                    "player_uuid TEXT, " +
                    "fish_id TEXT, " +
                    "count INTEGER DEFAULT 0, " +
                    "PRIMARY KEY (player_uuid, fish_id))");
        }
    }

    public void saveCatch(String uuid, String fishId) {
        if (this.connection == null) {
            init();
            if (this.connection == null) {
                AnglersAlmanac.getInstance().getLogger().atSevere().log("CRITICAL: Could not save catch. Database connection is null.");
                return;
            }
        }
        try {
            connection.setAutoCommit(false);

            // global total
            var psPlayer = connection.prepareStatement(
                    "INSERT INTO players(uuid, total_catches) VALUES(?, 1) " +
                            "ON CONFLICT(uuid) DO UPDATE SET total_catches = total_catches + 1");
            psPlayer.setString(1, uuid);
            psPlayer.executeUpdate();

            // specific fish count
            var psFish = connection.prepareStatement(
                    "INSERT INTO catches(player_uuid, fish_id, count) VALUES(?, ?, 1) " +
                            "ON CONFLICT(player_uuid, fish_id) DO UPDATE SET count = count + 1");
            psFish.setString(1, uuid);
            psFish.setString(2, fishId);
            psFish.executeUpdate();

            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        }
    }
}