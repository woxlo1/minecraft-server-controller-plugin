package com.woxloi.minecraftservercontroller;

import com.woxloi.minecraftservercontroller.gui.EnhancedGUIListener;
import com.woxloi.minecraftservercontroller.utils.*;
import org.bukkit.plugin.java.JavaPlugin;
import com.woxloi.minecraftservercontroller.commands.MSCCommand;
import com.woxloi.minecraftservercontroller.api.APIClient;
import com.woxloi.minecraftservercontroller.gui.GUIListener;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

public class MinecraftServerController extends JavaPlugin {

    private static MinecraftServerController instance;
    private APIClient apiClient;
    private NotificationManager notificationManager;
    private PlayerActivityTracker activityTracker;
    private PerformanceMonitor performanceMonitor;
    private WorldManager worldManager;
    private CommandTemplateManager templateManager;
    private ChatLogManager chatLogManager;

    private ZonedDateTime serverStartTime;
    private File dataDir;

    // 日本時間のタイムゾーン（全プラグインで使用）
    public static final ZoneId JAPAN_ZONE = ZoneId.of("Asia/Tokyo");

    // 日時フォーマッター
    public static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    @Override
    public void onEnable() {
        instance = this;
        serverStartTime = ZonedDateTime.now(JAPAN_ZONE);

        // データディレクトリ作成
        dataDir = new File(getDataFolder(), "data");
        dataDir.mkdirs();

        // コンフィグの保存
        saveDefaultConfig();

        // 設定値の取得
        String apiUrl = getConfig().getString("api.url", "http://localhost:8000");
        String apiKey = getConfig().getString("api.key", "");
        boolean debug = getConfig().getBoolean("plugin.debug", false);

        // API Key の警告
        if (apiKey.isEmpty()) {
            getLogger().warning("========================================");
            getLogger().warning("API Key is not set in config.yml!");
            getLogger().warning("Please set api.key in config.yml and reload the plugin.");
            getLogger().warning("========================================");
        }

        // API クライアントの初期化
        apiClient = new APIClient(apiUrl, apiKey, getLogger(), debug);

        // ヘルスチェック開始（5分ごと）
        new APIHealthChecker(this).runTaskTimerAsynchronously(this, 20L * 60 * 5, 20L * 60 * 5);

        // 通知マネージャーの初期化
        notificationManager = new NotificationManager(this);

        // プレイヤーアクティビティトラッカー初期化
        String activityDbPath = new File(dataDir, "activity.db").getAbsolutePath();
        activityTracker = new PlayerActivityTracker(this, activityDbPath);
        getServer().getPluginManager().registerEvents(activityTracker, this);

        // パフォーマンスモニター初期化
        String performanceDbPath = new File(dataDir, "performance.db").getAbsolutePath();
        performanceMonitor = new PerformanceMonitor(this, performanceDbPath);
        performanceMonitor.runTaskTimer(this, 20L, 100L);

        // ワールドマネージャー初期化
        File backupDir = new File(getDataFolder().getParentFile().getParentFile(), "backups");
        worldManager = new WorldManager(this, backupDir);

        // コマンドテンプレートマネージャー初期化
        templateManager = new CommandTemplateManager(dataDir);

        // チャットログマネージャー初期化
        String chatLogDbPath = new File(dataDir, "chatlogs.db").getAbsolutePath();
        chatLogManager = new ChatLogManager(this, chatLogDbPath);
        getServer().getPluginManager().registerEvents(chatLogManager, this);

        // コマンドの登録
        getCommand("msc").setExecutor(new MSCCommand(this));

        // GUIリスナーの登録
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new EnhancedGUIListener(this), this);

        getLogger().info("MinecraftServerController v1.4.1 has been enabled!");
        getLogger().info("Server started at: " + getFormattedStartTime());
        getLogger().info("API URL: " + apiUrl);
        getLogger().info("Debug Mode: " + (debug ? "ENABLED" : "DISABLED"));
        getLogger().info("Features:");
        getLogger().info("  - Player Activity Tracking (JST)");
        getLogger().info("  - Performance Monitoring (JST)");
        getLogger().info("  - World Management");
        getLogger().info("  - Command Templates");
        getLogger().info("  - Chat Log Viewer (JST)");
    }

    @Override
    public void onDisable() {
        // パフォーマンスモニター停止
        if (performanceMonitor != null) {
            performanceMonitor.cancel();
        }

        getLogger().info("MinecraftServerController v1.4.1 has been disabled!");
        getLogger().info("Total uptime: " + getFormattedUptime());
    }

    /**
     * サーバー稼働時間をミリ秒で取得
     */
    public long getUptimeMillis() {
        return Duration.between(serverStartTime, ZonedDateTime.now(JAPAN_ZONE)).toMillis();
    }

    /**
     * サーバー起動時刻を取得
     */
    public ZonedDateTime getServerStartTime() {
        return serverStartTime;
    }

    /**
     * 現在時刻を日本時間で取得
     */
    public static ZonedDateTime getCurrentTime() {
        return ZonedDateTime.now(JAPAN_ZONE);
    }

    /**
     * 現在時刻をフォーマット済み文字列で取得
     */
    public static String getFormattedCurrentTime() {
        return getCurrentTime().format(DATETIME_FORMATTER);
    }

    /**
     * サーバー起動時刻をフォーマット済み文字列で取得
     */
    public String getFormattedStartTime() {
        return serverStartTime.format(DATETIME_FORMATTER);
    }

    /**
     * サーバー稼働時間をフォーマット済み文字列で取得
     */
    public String getFormattedUptime() {
        long millis = getUptimeMillis();
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        return String.format("%d時間 %d分", hours, minutes);
    }

    public static MinecraftServerController getInstance() {
        return instance;
    }

    public APIClient getAPIClient() {
        return apiClient;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public PlayerActivityTracker getActivityTracker() {
        return activityTracker;
    }

    public PerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public CommandTemplateManager getTemplateManager() {
        return templateManager;
    }

    public ChatLogManager getChatLogManager() {
        return chatLogManager;
    }

    public File getDataDir() {
        return dataDir;
    }
}