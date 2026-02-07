package com.woxloi.minecraftservercontroller;

import com.woxloi.minecraftservercontroller.gui.EnhancedGUIListener;
import org.bukkit.plugin.java.JavaPlugin;
import com.woxloi.minecraftservercontroller.commands.MSCCommand;
import com.woxloi.minecraftservercontroller.api.APIClient;
import com.woxloi.minecraftservercontroller.gui.GUIListener;
import com.woxloi.minecraftservercontroller.utils.APIHealthChecker;
import com.woxloi.minecraftservercontroller.utils.NotificationManager;

public class MinecraftServerController extends JavaPlugin {

    private static MinecraftServerController instance;
    private APIClient apiClient;
    private NotificationManager notificationManager;
    private long serverStartNano;

    @Override
    public void onEnable() {
        instance = this;

        serverStartNano = System.nanoTime();

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

        // API クライアントの初期化（新しいコンストラクタを使用）
        apiClient = new APIClient(apiUrl, apiKey, getLogger(), debug);

        // ヘルスチェック開始（5分ごと）
        new APIHealthChecker(this).runTaskTimerAsynchronously(this, 20L * 60 * 5, 20L * 60 * 5);

        // 通知マネージャーの初期化
        notificationManager = new NotificationManager(this);

        // コマンドの登録
        getCommand("msc").setExecutor(new MSCCommand(this));

        // GUIリスナーの登録
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new EnhancedGUIListener(this), this);

        getLogger().info("MinecraftServerController has been enabled!");
        getLogger().info("API URL: " + apiUrl);
        getLogger().info("Debug Mode: " + (debug ? "ENABLED" : "DISABLED"));
    }

    @Override
    public void onDisable() {
        getLogger().info("MinecraftServerController has been disabled!");
    }

    public long getUptimeMillis() {
        return (System.nanoTime() - serverStartNano) / 1_000_000;
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
}