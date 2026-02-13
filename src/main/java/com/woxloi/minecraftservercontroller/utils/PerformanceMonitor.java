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
 * サーバーパフォーマンスモニタリング (v1.4.2: JST timestamps)
 * - TPS（Ticks Per Second）
 * - メモリ使用率
 * - エンティティ数
 * - チャンク数
 */
public class PerformanceMonitor extends BukkitRunnable {

    private final MinecraftServerController plugin;
    private final String dbPath;

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

            plugin.getLogger().info("Performance monitoring database initialized");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize performance database: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        recordMetrics();
    }

    /**
     * メトリクスを記録（v1.4.2: JST使用）
     */
    private void recordMetrics() {
        try {
            // Bukkit TPSを取得（Paper/Spigot）
            double[] tpsArray = Bukkit.getTPS();
            double currentTps = tpsArray[0]; // 1分平均

            // メモリ情報
            Runtime runtime = Runtime.getRuntime();
            long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long memoryTotal = runtime.maxMemory() / (1024 * 1024);
            double memoryPercent = (double) memoryUsed / memoryTotal * 100;

            // エンティティとチャンク数
            int totalEntities = 0;
            int totalChunks = 0;

            for (World world : Bukkit.getWorlds()) {
                totalEntities += world.getEntities().size();
                totalChunks += world.getLoadedChunks().length;
            }

            int players = Bukkit.getOnlinePlayers().size();

            // TPS低下の警告チェック（メインスレッドで通知）
            if (currentTps < 15.0) {
                plugin.getNotificationManager().notifyWarning(
                        String.format("Low TPS detected: %.2f (Entities: %d, Chunks: %d)",
                                currentTps, totalEntities, totalChunks)
                );
            }

            // v1.4.2: JST時刻を使用
            String nowStr = MinecraftServerController.getCurrentTime()
                    .format(MinecraftServerController.DATETIME_FORMATTER);

            // DB書き込みとAPI送信は非同期で
            int finalTotalEntities = totalEntities;
            int finalTotalChunks = totalChunks;
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection conn = getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO performance_metrics
                        (timestamp, tps, memory_used, memory_total, memory_percent, entities, chunks, players)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """);

                    ps.setString(1, nowStr);
                    ps.setDouble(2, currentTps);
                    ps.setLong(3, memoryUsed);
                    ps.setLong(4, memoryTotal);
                    ps.setDouble(5, memoryPercent);
                    ps.setInt(6, finalTotalEntities);
                    ps.setInt(7, finalTotalChunks);
                    ps.setInt(8, players);

                    ps.executeUpdate();

                    // APIサーバーにも記録
                    try {
                        plugin.getAPIClient().recordPerformance(
                                currentTps,
                                (int) memoryUsed,
                                (int) memoryTotal,
                                memoryPercent,
                                finalTotalEntities,
                                finalTotalChunks,
                                players
                        );
                    } catch (IOException e) {
                        // API記録失敗は警告のみ（ローカルDBには記録済み）
                        plugin.getLogger().warning("Failed to record performance to API: " + e.getMessage());
                    }

                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to record metrics: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            plugin.getLogger().warning("Error collecting metrics: " + e.getMessage());
        }
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
     * 現在のパフォーマンス情報を取得
     */
    public CurrentPerformance getCurrentPerformance() {
        try {
            double[] tps = Bukkit.getTPS();
            Runtime runtime = Runtime.getRuntime();
            long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long memoryTotal = runtime.maxMemory() / (1024 * 1024);

            int totalEntities = 0;
            int totalChunks = 0;

            for (World world : Bukkit.getWorlds()) {
                totalEntities += world.getEntities().size();
                totalChunks += world.getLoadedChunks().length;
            }

            return new CurrentPerformance(
                    tps[0], tps[1], tps[2],
                    memoryUsed, memoryTotal,
                    totalEntities, totalChunks,
                    Bukkit.getOnlinePlayers().size()
            );

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get current performance: " + e.getMessage());
            return null;
        }
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