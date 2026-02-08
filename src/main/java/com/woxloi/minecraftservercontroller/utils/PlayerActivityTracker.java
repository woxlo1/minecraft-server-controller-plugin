package com.woxloi.minecraftservercontroller.utils;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * プレイヤーアクティビティ統計管理
 * - ログイン/ログアウト履歴
 * - 総プレイ時間
 * - 最終ログイン時刻
 */
public class PlayerActivityTracker implements Listener {

    private final MinecraftServerController plugin;
    private final String dbPath;
    private final Map<UUID, Long> sessionStartTimes;

    public PlayerActivityTracker(MinecraftServerController plugin, String dbPath) {
        this.plugin = plugin;
        this.dbPath = dbPath;
        this.sessionStartTimes = new HashMap<>();
        initDatabase();
    }

    /**
     * データベース初期化
     */
    private void initDatabase() {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS player_activity (
                    uuid TEXT NOT NULL,
                    player_name TEXT NOT NULL,
                    login_time TEXT NOT NULL,
                    logout_time TEXT,
                    session_duration INTEGER,
                    PRIMARY KEY (uuid, login_time)
                )
            """);

            conn.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS player_stats (
                    uuid TEXT PRIMARY KEY,
                    player_name TEXT NOT NULL,
                    total_playtime INTEGER DEFAULT 0,
                    total_sessions INTEGER DEFAULT 0,
                    first_join TEXT,
                    last_join TEXT
                )
            """);

            plugin.getLogger().info("Player activity database initialized");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize activity database: " + e.getMessage());
        }
    }

    /**
     * プレイヤーログイン処理
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        LocalDateTime now = LocalDateTime.now();

        sessionStartTimes.put(uuid, System.currentTimeMillis());

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                // アクティビティログに記録
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO player_activity (uuid, player_name, login_time) VALUES (?, ?, ?)"
                );
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.setString(3, now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                ps.executeUpdate();

                // 統計を更新
                ps = conn.prepareStatement("""
                    INSERT INTO player_stats (uuid, player_name, total_sessions, first_join, last_join)
                    VALUES (?, ?, 1, ?, ?)
                    ON CONFLICT(uuid) DO UPDATE SET
                        player_name = ?,
                        total_sessions = total_sessions + 1,
                        last_join = ?
                """);
                String nowStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.setString(3, nowStr);
                ps.setString(4, nowStr);
                ps.setString(5, name);
                ps.setString(6, nowStr);
                ps.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to log player join: " + e.getMessage());
            }
        });
    }

    /**
     * プレイヤーログアウト処理
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        LocalDateTime now = LocalDateTime.now();

        Long sessionStart = sessionStartTimes.remove(uuid);
        if (sessionStart == null) return;

        long duration = (System.currentTimeMillis() - sessionStart) / 1000; // 秒単位

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                // ログアウト時刻とセッション時間を記録
                PreparedStatement ps = conn.prepareStatement("""
                    UPDATE player_activity
                    SET logout_time = ?, session_duration = ?
                    WHERE uuid = ? AND logout_time IS NULL
                    ORDER BY login_time DESC
                    LIMIT 1
                """);
                ps.setString(1, now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                ps.setLong(2, duration);
                ps.setString(3, uuid.toString());
                ps.executeUpdate();

                // 総プレイ時間を更新
                ps = conn.prepareStatement("""
                    UPDATE player_stats
                    SET total_playtime = total_playtime + ?
                    WHERE uuid = ?
                """);
                ps.setLong(1, duration);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();

            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to log player quit: " + e.getMessage());
            }
        });
    }

    /**
     * プレイヤー統計を取得
     */
    public PlayerStats getPlayerStats(UUID uuid) {
        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM player_stats WHERE uuid = ?"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new PlayerStats(
                        rs.getString("player_name"),
                        rs.getLong("total_playtime"),
                        rs.getInt("total_sessions"),
                        rs.getString("first_join"),
                        rs.getString("last_join")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get player stats: " + e.getMessage());
        }
        return null;
    }

    /**
     * 最近のアクティビティを取得
     */
    public String getRecentActivity(UUID uuid, int limit) {
        StringBuilder result = new StringBuilder();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT login_time, logout_time, session_duration
                FROM player_activity
                WHERE uuid = ?
                ORDER BY login_time DESC
                LIMIT ?
            """);
            ps.setString(1, uuid.toString());
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String login = rs.getString("login_time");
                String logout = rs.getString("logout_time");
                Long duration = rs.getLong("session_duration");

                result.append("Login: ").append(login);
                if (logout != null) {
                    result.append(" → Logout: ").append(logout);
                    result.append(" (").append(formatDuration(duration)).append(")");
                } else {
                    result.append(" → Still online");
                }
                result.append("\n");
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get recent activity: " + e.getMessage());
        }

        return result.toString();
    }

    /**
     * 時間をフォーマット
     */
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    /**
     * データベース接続を取得
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    /**
     * プレイヤー統計データクラス
     */
    public static class PlayerStats {
        public final String playerName;
        public final long totalPlaytime;
        public final int totalSessions;
        public final String firstJoin;
        public final String lastJoin;

        public PlayerStats(String playerName, long totalPlaytime, int totalSessions,
                           String firstJoin, String lastJoin) {
            this.playerName = playerName;
            this.totalPlaytime = totalPlaytime;
            this.totalSessions = totalSessions;
            this.firstJoin = firstJoin;
            this.lastJoin = lastJoin;
        }

        public String getFormattedPlaytime() {
            long hours = totalPlaytime / 3600;
            long minutes = (totalPlaytime % 3600) / 60;
            return String.format("%dh %dm", hours, minutes);
        }
    }
}