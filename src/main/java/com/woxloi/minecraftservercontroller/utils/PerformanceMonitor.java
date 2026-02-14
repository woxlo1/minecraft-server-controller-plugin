// =============================
// 修正版 PerformanceMonitor.java
// 問題点: 5秒ごとにDB書き込みが発生し、パフォーマンス低下の原因に
// 解決策: 1分ごとに記録し、メモリ上のキャッシュを利用
// =============================

package com.woxloi.minecraftservercontroller.utils;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * サーバーパフォーマンスモニタリング (v1.4.3 - 最適化版)
 * - TPS（Ticks Per Second）
 * - メモリ使用率
 * - エンティティ数
 * - チャンク数
 * - DB書き込みを1分間隔に変更（v1.4.3）
 */
public class PerformanceMonitor extends BukkitRunnable {

    private final MinecraftServerController plugin;
    private final String dbPath;
    private int tickCounter = 0;
    private static final int RECORD_INTERVAL_TICKS = 1200; // 1分間隔（60秒 × 20tick）

    // キャッシュされた最新データ
    private volatile CurrentPerformance cachedPerformance;

    public PerformanceMonitor(MinecraftServerController plugin, String dbPath) {
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
                CREATE TABLE IF NOT EXISTS performance_metrics (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL,
                    tps REAL,
                    memory_used INTEGER,
                    memory_total INTEGER,
                    memory_percent REAL,
                    entities INTEGER,
                    chunks INTEGER,
                    players INTEGER
                )
            """);

            // 古いデータを削除（7日以上前）
            conn.createStatement().execute("""
                DELETE FROM performance_metrics
                WHERE timestamp < datetime('now', '-7 days')
            """);

            plugin.getLogger().info("Performance monitoring database initialized (v1.4.3 - optimized)");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize performance database: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // 毎回データを収集してキャッシュを更新
        updateCachedPerformance();

        tickCounter++;

        // 1分ごとにデータベースに記録
        if (tickCounter >= RECORD_INTERVAL_TICKS) {
            recordMetrics();
            tickCounter = 0;
        }
    }

    /**
     * キャッシュされたパフォーマンスデータを更新
     */
    private void updateCachedPerformance() {
        try {
            // Bukkit TPSを取得（Paper/Spigot）
            double[] tpsArray = Bukkit.getTPS();

            // メモリ情報
            Runtime runtime = Runtime.getRuntime();
            long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long memoryTotal = runtime.maxMemory() / (1024 * 1024);

            // エンティティとチャンク数
            int totalEntities = 0;
            int totalChunks = 0;

            for (World world : Bukkit.getWorlds()) {
                totalEntities += world.getEntities().size();
                totalChunks += world.getLoadedChunks().length;
            }

            int players = Bukkit.getOnlinePlayers().size();

            // キャッシュを更新
            cachedPerformance = new CurrentPerformance(
                    tpsArray[0], tpsArray[1], tpsArray[2],
                    memoryUsed, memoryTotal,
                    totalEntities, totalChunks,
                    players
            );

        } catch (Exception e) {
            plugin.getLogger().warning("Error updating cached performance: " + e.getMessage());
        }
    }

    /**
     * メトリクスをデータベースに記録（v1.4.3: 1分ごと）
     */
    private void recordMetrics() {
        if (cachedPerformance == null) {
            return;
        }

        // JST時刻を使用
        String nowStr = MinecraftServerController.getCurrentTime()
                .format(MinecraftServerController.DATETIME_FORMATTER);

        // TPS低下の警告チェック（メインスレッドで通知）
        if (cachedPerformance.tps1m < 15.0) {
            plugin.getNotificationManager().notifyWarning(
                    String.format("Low TPS detected: %.2f (Entities: %d, Chunks: %d)",
                            cachedPerformance.tps1m, cachedPerformance.entities, cachedPerformance.chunks)
            );
        }

        // DB書き込みとAPI送信は非同期で
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO performance_metrics
                    (timestamp, tps, memory_used, memory_total, memory_percent, entities, chunks, players)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """);

                ps.setString(1, nowStr);
                ps.setDouble(2, cachedPerformance.tps1m);
                ps.setLong(3, cachedPerformance.memoryUsed);
                ps.setLong(4, cachedPerformance.memoryTotal);
                ps.setDouble(5, cachedPerformance.getMemoryPercent());
                ps.setInt(6, cachedPerformance.entities);
                ps.setInt(7, cachedPerformance.chunks);
                ps.setInt(8, cachedPerformance.players);

                ps.executeUpdate();

                // APIサーバーにも記録（オプション）
                try {
                    plugin.getAPIClient().recordPerformance(
                            cachedPerformance.tps1m,
                            (int) cachedPerformance.memoryUsed,
                            (int) cachedPerformance.memoryTotal,
                            cachedPerformance.getMemoryPercent(),
                            cachedPerformance.entities,
                            cachedPerformance.chunks,
                            cachedPerformance.players
                    );
                } catch (IOException e) {
                    // API記録失敗は警告のみ（ローカルDBには記録済み）
                    if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                        plugin.getLogger().warning("Failed to record performance to API: " + e.getMessage());
                    }
                }

            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to record metrics: " + e.getMessage());
            }
        });
    }

    /**
     * 過去のメトリクスを取得
     */
    public List<PerformanceMetric> getMetrics(int hours) {
        List<PerformanceMetric> metrics = new ArrayList<>();

        try (Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT * FROM performance_metrics
                WHERE timestamp > datetime('now', '-' || ? || ' hours')
                ORDER BY timestamp DESC
            """);
            ps.setInt(1, hours);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                metrics.add(new PerformanceMetric(
                        rs.getString("timestamp"),
                        rs.getDouble("tps"),
                        rs.getLong("memory_used"),
                        rs.getLong("memory_total"),
                        rs.getDouble("memory_percent"),
                        rs.getInt("entities"),
                        rs.getInt("chunks"),
                        rs.getInt("players")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to get metrics: " + e.getMessage());
        }

        return metrics;
    }

    /**
     * 現在のパフォーマンス情報を取得（キャッシュから）
     */
    public CurrentPerformance getCurrentPerformance() {
        // キャッシュがnullの場合は即座に作成
        if (cachedPerformance == null) {
            updateCachedPerformance();
        }
        return cachedPerformance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    /**
     * パフォーマンスメトリクスデータクラス
     */
    public static class PerformanceMetric {
        public final String timestamp;
        public final double tps;
        public final long memoryUsed;
        public final long memoryTotal;
        public final double memoryPercent;
        public final int entities;
        public final int chunks;
        public final int players;

        public PerformanceMetric(String timestamp, double tps, long memoryUsed, long memoryTotal,
                                 double memoryPercent, int entities, int chunks, int players) {
            this.timestamp = timestamp;
            this.tps = tps;
            this.memoryUsed = memoryUsed;
            this.memoryTotal = memoryTotal;
            this.memoryPercent = memoryPercent;
            this.entities = entities;
            this.chunks = chunks;
            this.players = players;
        }
    }

    /**
     * 現在のパフォーマンス情報
     */
    public static class CurrentPerformance {
        public final double tps1m;
        public final double tps5m;
        public final double tps15m;
        public final long memoryUsed;
        public final long memoryTotal;
        public final int entities;
        public final int chunks;
        public final int players;

        public CurrentPerformance(double tps1m, double tps5m, double tps15m,
                                  long memoryUsed, long memoryTotal,
                                  int entities, int chunks, int players) {
            this.tps1m = tps1m;
            this.tps5m = tps5m;
            this.tps15m = tps15m;
            this.memoryUsed = memoryUsed;
            this.memoryTotal = memoryTotal;
            this.entities = entities;
            this.chunks = chunks;
            this.players = players;
        }

        public double getMemoryPercent() {
            return (double) memoryUsed / memoryTotal * 100;
        }
    }
}