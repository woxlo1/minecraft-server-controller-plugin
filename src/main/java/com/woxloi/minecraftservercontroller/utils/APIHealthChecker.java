package com.woxloi.minecraftservercontroller.utils;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import org.bukkit.scheduler.BukkitRunnable;

public class APIHealthChecker extends BukkitRunnable {

    private final MinecraftServerController plugin;
    private boolean lastStatus = true;

    public APIHealthChecker(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            // 簡易ヘルスチェック: メトリクス取得
            plugin.getAPIClient().getMetrics();

            if (!lastStatus) {
                // 復旧した
                plugin.getLogger().info("API connection restored!");
                lastStatus = true;
            }
        } catch (Exception e) {
            if (lastStatus) {
                // 接続失敗した
                plugin.getLogger().warning("API connection lost: " + e.getMessage());
                lastStatus = false;
            }
        }
    }
}