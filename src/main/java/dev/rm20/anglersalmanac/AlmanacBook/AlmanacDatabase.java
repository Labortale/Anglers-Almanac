package dev.rm20.anglersalmanac.AlmanacBook;

import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.MinigameManager.Minigame;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmanacDatabase {
    private static final String DB_PATH = "mods/dev.rm20_AnglersAlmanac/Data/almanac.db";
    private Connection connection;

    public AlmanacDatabase() {
        init();
    }

    private void init() {
        try {
            File dir = new File("mods/dev.rm20_AnglersAlmanac/Data/");
            if (!dir.exists()) dir.mkdirs();

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);

            // Optimization: Enable WAL mode to allow concurrent reads/writes
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;");
            }

            createTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Player's overall stats
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "total_catches INTEGER DEFAULT 0, " +
                    "legendary_catches INTEGER DEFAULT 0)");

            // fish counter per player
            stmt.execute("CREATE TABLE IF NOT EXISTS catches (" +
                    "player_uuid TEXT, " +
                    "fish_id TEXT, " +
                    "count INTEGER DEFAULT 0, " +
                    "PRIMARY KEY (player_uuid, fish_id))");

            // Performance
            stmt.execute("CREATE TABLE IF NOT EXISTS performance_stats (" +
                    "player_uuid TEXT, " +
                    "rating TEXT, " + // FAIL, GOOD, GREAT, PERFECT
                    "count INTEGER DEFAULT 0, " +
                    "PRIMARY KEY (player_uuid, rating))");
        }


    }

    public boolean saveCatch(String uuid, String fishId, boolean isLegendary, Minigame.PerformanceRating rating) {
        boolean isFirstTime = false;
        if (this.connection == null) {
            init();
            if (this.connection == null) {
                AnglersAlmanac.getInstance().getLogger().atSevere().log("CRITICAL: Could not save catch. Database connection is null.");
                return false;
            }
        }
        try {
            connection.setAutoCommit(false);
            var psCheck = connection.prepareStatement("SELECT 1 FROM catches WHERE player_uuid = ? AND fish_id = ?");
            psCheck.setString(1, uuid);
            psCheck.setString(2, fishId);
            ResultSet rs = psCheck.executeQuery();
            isFirstTime = !rs.next(); // If no result, it's the first time!
            // global total
            int legValue = isLegendary ? 1 : 0;
            var psPlayer = connection.prepareStatement(
                    "INSERT INTO players(uuid, total_catches, legendary_catches) VALUES(?, 1, ?) " +
                            "ON CONFLICT(uuid) DO UPDATE SET " +
                            "total_catches = total_catches + 1, " +
                            "legendary_catches = legendary_catches + ?");
            psPlayer.setString(1, uuid);
            psPlayer.setInt(2, legValue);
            psPlayer.setInt(3, legValue);
            psPlayer.executeUpdate();

            // specific fish count
            var psFish = connection.prepareStatement(
                    "INSERT INTO catches(player_uuid, fish_id, count) VALUES(?, ?, 1) " +
                            "ON CONFLICT(player_uuid, fish_id) DO UPDATE SET count = count + 1");
            psFish.setString(1, uuid);
            psFish.setString(2, fishId);
            psFish.executeUpdate();

            // Performance
            var psRating = connection.prepareStatement(
                    "INSERT INTO performance_stats(player_uuid, rating, count) VALUES(?, ?, 1) " +
                            "ON CONFLICT(player_uuid, rating) DO UPDATE SET count = count + 1");
            psRating.setString(1, uuid);
            psRating.setString(2, rating.name());
            psRating.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
        }

        return isFirstTime;
    }

    public PlayerStatsData getPlayerStats(String uuid) {
        PlayerStatsData data = new PlayerStatsData();
        try {
            // 1. Get totals
            var psTotal = connection.prepareStatement("SELECT total_catches, legendary_catches FROM players WHERE uuid = ?");
            psTotal.setString(1, uuid);
            ResultSet rs1 = psTotal.executeQuery();
            if (rs1.next()) {
                data.totalCatches = rs1.getInt("total_catches");
                data.legendaryCount = rs1.getInt("legendary_catches");
            }

            // 2. Get Top 10 fish
            var psTop = connection.prepareStatement(
                    "SELECT fish_id, count FROM catches WHERE player_uuid = ? ORDER BY count DESC LIMIT 10");
            psTop.setString(1, uuid);
            ResultSet rs2 = psTop.executeQuery();
            while (rs2.next()) {
                data.topFish.add(new FishEntry(rs2.getString("fish_id"), rs2.getInt("count")));
            }

            // 3. Get Performance Ratings
            var psRatings = connection.prepareStatement(
                    "SELECT rating, count FROM performance_stats WHERE player_uuid = ?");
            psRatings.setString(1, uuid);
            ResultSet rs3 = psRatings.executeQuery();
            while (rs3.next()) {
                data.ratingsMap.put(rs3.getString("rating"), rs3.getInt("count"));
            }

            // 4. get all fish
            var psAllFish = connection.prepareStatement(
                    "SELECT fish_id, count FROM catches WHERE player_uuid = ?");
            psAllFish.setString(1, uuid);
            ResultSet rs4 = psAllFish.executeQuery();
            while (rs4.next()) {
                data.catchMap.put(rs4.getString("fish_id"), rs4.getInt("count"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static class PlayerStatsData {
        public int totalCatches = 0;
        public List<FishEntry> topFish = new ArrayList<>();
        public int legendaryCount = 0;
        public java.util.HashMap<String, Integer> ratingsMap = new java.util.HashMap<>();
        public java.util.HashMap<String, Integer> catchMap = new java.util.HashMap<>();
        public int getRatingCount(Minigame.PerformanceRating rating) {
            return ratingsMap.getOrDefault(rating.name(), 0);
        }
        public int getFishCount(String fishId) {
            return catchMap.getOrDefault(fishId, 0);
        }
    }

    public boolean hasPlayerCaught(String playerUUID, String fishId) {
        return true;
//        if (this.connection == null) {
//            init();
//            if (this.connection == null) return false;
//        }
//
//        String sql = "SELECT 1 FROM catches WHERE player_uuid = ? AND fish_id = ? LIMIT 1";
//
//        try (PreparedStatement ps = connection.prepareStatement(sql)) {
//            ps.setString(1, playerUUID);
//            ps.setString(2, fishId);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                return rs.next();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
    }

    public Map<String, Integer> getAllFishCounts(String playerUUID) {
        Map<String, Integer> counts = new HashMap<>();

        String sql = "SELECT fish_id, count FROM catches WHERE player_uuid = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUUID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("fish_id"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public record FishEntry(String name, int count) {}
}