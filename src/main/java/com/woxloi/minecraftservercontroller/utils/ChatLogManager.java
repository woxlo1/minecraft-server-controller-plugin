package com.woxloi.minecraftservercontroller.utils;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * チャットログ管理 (v1.4.2: JST timestamps)
 * - チャットメッセージの記録
 * - プレイヤー別フィルタ
 * - キーワード検索
 */
public class ChatLogManager implements Listener {

    private final MinecraftServerController plugin;
    private final String dbPath;

    public ChatLogManager(MinecraftServerController plugin, String dbPath) {
        this.plugin = plugin;
        this.dbPath = dbPath;
        initDatabase();
    }

    /**
     * データベース初期化
     */
    private void initDatabase() {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS chat_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL,
                    player_uuid TEXT NOT NULL,
                    player_name TEXT NOT NULL,
                    message TEXT NOT NULL,
                    world TEXT
                )
            """);

            // 古いログを削除（30日以上前）
            conn.createStatement().execute("""
                DELETE FROM chat_logs
                WHERE timestamp < datetime('now', '-30 days')
            """);

            // インデックス作成
            conn.createStatement().execute("""
                CREATE INDEX IF NOT EXISTS idx_chat_timestamp
                ON chat_logs(timestamp)
            """);

            conn.createStatement().execute("""
                CREATE INDEX IF NOT EXISTS idx_chat_player
                ON chat_logs(player_uuid)
            """);

            plugin.getLogger().info("Chat log database initialized");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize chat log database: " + e.getMessage());
        }
    }

    /**
     * チャットメッセージを記録（v1.4.2: JST使用）
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // v1.4.2: JST時刻を使用
        String nowStr = MinecraftServerController.getCurrentTime()
                .format(MinecraftServerController.DATETIME_FORMATTER);

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO chat_logs (timestamp, player_uuid, player_name, message, world)
                VALUES (?, ?, ?, ?, ?)
            """);

            ps.setString(1, nowStr);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, player.getName());
            ps.setString(4, message);
            ps.setString(5, player.getWorld().getName());

            ps.executeUpdate();

            // v1.4.2: APIサーバーにも記録
            try {
                plugin.getAPIClient().logChatMessage(
                        player.getUniqueId().toString(),
                        player.getName(),
                        message,
                        player.getWorld().getName()
                );
            } catch (IOException e) {
                // API記録失敗は警告のみ（ローカルDBには記録済み）
                plugin.getLogger().warning("Failed to log chat to API: " + e.getMessage());
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to log chat message: " + e.getMessage());
        }
    }

    /**
     * 最新のチャットログを取得
     */
    public List<ChatMessage> getRecentMessages(int limit) {
        List<ChatMessage> messages = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT * FROM chat_logs
                ORDER BY timestamp DESC
                LIMIT ?
            """);
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                messages.add(new ChatMessage(
                        rs.getString("timestamp"),
                        rs.getString("player_uuid"),
                        rs.getString("player_name"),
                        rs.getString("message"),
                        rs.getString("world")
                ));
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get recent messages: " + e.getMessage());
        }

        return messages;
    }

    /**
     * プレイヤー別チャットログを取得
     */
    public List<ChatMessage> getPlayerMessages(UUID playerUuid, int limit) {
        List<ChatMessage> messages = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT * FROM chat_logs
                WHERE player_uuid = ?
                ORDER BY timestamp DESC
                LIMIT ?
            """);
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                messages.add(new ChatMessage(
                        rs.getString("timestamp"),
                        rs.getString("player_uuid"),
                        rs.getString("player_name"),
                        rs.getString("message"),
                        rs.getString("world")
                ));
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get player messages: " + e.getMessage());
        }

        return messages;
    }

    /**
     * キーワード検索
     */
    public List<ChatMessage> searchMessages(String keyword, int limit) {
        List<ChatMessage> messages = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT * FROM chat_logs
                WHERE message LIKE ?
                ORDER BY timestamp DESC
                LIMIT ?
            """);
            ps.setString(1, "%" + keyword + "%");
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                messages.add(new ChatMessage(
                        rs.getString("timestamp"),
                        rs.getString("player_uuid"),
                        rs.getString("player_name"),
                        rs.getString("message"),
                        rs.getString("world")
                ));
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to search messages: " + e.getMessage());
        }

        return messages;
    }

    /**
     * 期間指定でログを取得
     */
    public List<ChatMessage> getMessagesByDateRange(String startDate, String endDate, int limit) {
        List<ChatMessage> messages = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT * FROM chat_logs
                WHERE timestamp BETWEEN ? AND ?
                ORDER BY timestamp DESC
                LIMIT ?
            """);
            ps.setString(1, startDate);
            ps.setString(2, endDate);
            ps.setInt(3, limit);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                messages.add(new ChatMessage(
                        rs.getString("timestamp"),
                        rs.getString("player_uuid"),
                        rs.getString("player_name"),
                        rs.getString("message"),
                        rs.getString("world")
                ));
            }

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get messages by date range: " + e.getMessage());
        }

        return messages;
    }

    /**
     * チャット統計を取得
     */
    public ChatStats getChatStats() {
        try (Connection conn = getConnection()) {
            // 総メッセージ数
            PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM chat_logs");
            ResultSet rs = ps.executeQuery();
            long totalMessages = rs.next() ? rs.getLong(1) : 0;

            // 今日のメッセージ数
            ps = conn.prepareStatement("""
                SELECT COUNT(*) FROM chat_logs
                WHERE DATE(timestamp) = DATE('now')
            """);
            rs = ps.executeQuery();
            long todayMessages = rs.next() ? rs.getLong(1) : 0;

            // 最もアクティブなプレイヤー
            ps = conn.prepareStatement("""
                SELECT player_name, COUNT(*) as count
                FROM chat_logs
                WHERE timestamp > datetime('now', '-7 days')
                GROUP BY player_uuid
                ORDER BY count DESC
                LIMIT 1
            """);
            rs = ps.executeQuery();
            String topPlayer = rs.next() ? rs.getString("player_name") : "N/A";

            return new ChatStats(totalMessages, todayMessages, topPlayer);

        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get chat stats: " + e.getMessage());
            return new ChatStats(0, 0, "N/A");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    /**
     * チャットメッセージクラス
     */
    public static class ChatMessage {
        public final String timestamp;
        public final String playerUuid;
        public final String playerName;
        public final String message;
        public final String world;

        public ChatMessage(String timestamp, String playerUuid, String playerName,
                           String message, String world) {
            this.timestamp = timestamp;
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.message = message;
            this.world = world;
        }

        public String getFormattedTimestamp() {
            // Already in JST format from DB
            return timestamp;
        }
    }

    /**
     * チャット統計クラス
     */
    public static class ChatStats {
        public final long totalMessages;
        public final long todayMessages;
        public final String topPlayer;

        public ChatStats(long totalMessages, long todayMessages, String topPlayer) {
            this.totalMessages = totalMessages;
            this.todayMessages = todayMessages;
            this.topPlayer = topPlayer;
        }
    }
}