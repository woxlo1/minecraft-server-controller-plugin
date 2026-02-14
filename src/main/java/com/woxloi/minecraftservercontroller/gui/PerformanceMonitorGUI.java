package com.woxloi.minecraftservercontroller.gui;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import com.woxloi.minecraftservercontroller.utils.PerformanceMonitor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * パフォーマンスモニターGUI (v1.4.3 - Enhanced Error Handling)
 */
public class PerformanceMonitorGUI {

    private final MinecraftServerController plugin;

    public PerformanceMonitorGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Loading performance data...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PerformanceMonitor.CurrentPerformance current = null;
            List<PerformanceMonitor.PerformanceMetric> history = new ArrayList<>();
            String errorMessage = null;

            try {
                current = plugin.getPerformanceMonitor().getCurrentPerformance();

                if (current != null) {
                    history = plugin.getPerformanceMonitor().getMetrics(1);
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                plugin.getLogger().warning("Failed to get performance data: " + e.getMessage());
                if (plugin.getConfig().getBoolean("plugin.debug", false)) {
                    e.printStackTrace();
                }
            }

            final PerformanceMonitor.CurrentPerformance finalCurrent = current;
            final List<PerformanceMonitor.PerformanceMetric> finalHistory = history;
            final String finalErrorMessage = errorMessage;

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                displayPerformance(player, finalCurrent, finalHistory, finalErrorMessage);
            });
        });
    }

    private void displayPerformance(Player player, PerformanceMonitor.CurrentPerformance current,
                                    List<PerformanceMonitor.PerformanceMetric> history,
                                    String errorMessage) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Performance Monitor");

        if (current == null) {
            // エラー表示を強化
            long uptime = plugin.getUptimeMillis() / 1000; // 秒単位
            boolean isDebugMode = plugin.getConfig().getBoolean("plugin.debug", false);

            List<String> errorLore = new ArrayList<>();
            errorLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
            errorLore.add(ChatColor.RED + "Failed to load performance data");
            errorLore.add("");

            // サーバー稼働時間のチェック
            if (uptime < 5) {
                errorLore.add(ChatColor.YELLOW + "⚠ Server just started!");
                errorLore.add(ChatColor.GRAY + "Uptime: " + uptime + " seconds");
                errorLore.add("");
                errorLore.add(ChatColor.WHITE + "Please wait at least 5 seconds");
                errorLore.add(ChatColor.WHITE + "for data collection to begin.");
            } else {
                errorLore.add(ChatColor.YELLOW + "Possible causes:");
                errorLore.add(ChatColor.GRAY + "• PerformanceMonitor not running");
                errorLore.add(ChatColor.GRAY + "• Database connection issue");
                errorLore.add(ChatColor.GRAY + "• TPS calculation unavailable");
            }

            errorLore.add("");
            errorLore.add(ChatColor.YELLOW + "Server Info:");
            errorLore.add(ChatColor.GRAY + "Uptime: " + formatUptime(uptime));
            errorLore.add(ChatColor.GRAY + "Online Players: " + Bukkit.getOnlinePlayers().size());

            // エラーメッセージがある場合は表示
            if (errorMessage != null) {
                errorLore.add("");
                errorLore.add(ChatColor.RED + "Error Details:");
                errorLore.add(ChatColor.GRAY + errorMessage);
            }

            errorLore.add("");
            errorLore.add(ChatColor.YELLOW + "Troubleshooting:");
            errorLore.add(ChatColor.WHITE + "1. Wait 5+ seconds after start");
            errorLore.add(ChatColor.WHITE + "2. Check server logs");
            errorLore.add(ChatColor.WHITE + "3. Try: /msc reload");

            if (!isDebugMode) {
                errorLore.add("");
                errorLore.add(ChatColor.GRAY + "Enable debug mode in config.yml");
                errorLore.add(ChatColor.GRAY + "for detailed error information");
            }

            errorLore.add("");
            errorLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
            errorLore.add("");
            errorLore.add(ChatColor.GOLD + "Click refresh to try again");

            inv.setItem(22, createItem(Material.BARRIER,
                    ChatColor.RED + "⚠ No Performance Data",
                    errorLore.toArray(new String[0])));

            // デバッグ情報（管理者のみ）
            if (player.hasPermission("msc.admin") && isDebugMode) {
                List<String> debugLore = new ArrayList<>();
                debugLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
                debugLore.add(ChatColor.DARK_RED + "Debug Information");
                debugLore.add("");
                debugLore.add(ChatColor.YELLOW + "Monitor Status:");

                try {
                    PerformanceMonitor monitor = plugin.getPerformanceMonitor();
                    debugLore.add(ChatColor.GRAY + "Monitor: " + (monitor != null ? "Running" : "NULL"));

                    if (monitor != null) {
                        debugLore.add(ChatColor.GRAY + "Bukkit TPS: " + Arrays.toString(Bukkit.getTPS()));
                    }
                } catch (Exception e) {
                    debugLore.add(ChatColor.RED + "Exception: " + e.getClass().getSimpleName());
                    debugLore.add(ChatColor.GRAY + e.getMessage());
                }

                debugLore.add("");
                debugLore.add(ChatColor.YELLOW + "System Info:");
                Runtime runtime = Runtime.getRuntime();
                debugLore.add(ChatColor.GRAY + "Memory: " +
                        (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) +
                        "/" + runtime.maxMemory() / (1024 * 1024) + " MB");
                debugLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");

                inv.setItem(40, createItem(Material.REDSTONE_TORCH,
                        ChatColor.DARK_RED + "🐛 Debug Info",
                        debugLore.toArray(new String[0])));
            }

        } else {
            // TPS情報
            Material tpsMaterial = current.tps1m >= 19.5 ? Material.GREEN_WOOL :
                    current.tps1m >= 18.0 ? Material.YELLOW_WOOL : Material.RED_WOOL;

            inv.setItem(10, createItem(tpsMaterial,
                    ChatColor.GOLD + "⚡ TPS (Ticks Per Second)",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                    ChatColor.YELLOW + "1 minute: " + ChatColor.WHITE + String.format("%.2f", current.tps1m),
                    ChatColor.YELLOW + "5 minutes: " + ChatColor.WHITE + String.format("%.2f", current.tps5m),
                    ChatColor.YELLOW + "15 minutes: " + ChatColor.WHITE + String.format("%.2f", current.tps15m),
                    "",
                    getTpsBar(current.tps1m),
                    "",
                    current.tps1m >= 19.5 ? ChatColor.GREEN + "✓ Excellent" :
                            current.tps1m >= 18.0 ? ChatColor.YELLOW + "⚠ Good" :
                                    current.tps1m >= 15.0 ? ChatColor.GOLD + "⚠ Fair" : ChatColor.RED + "✗ Poor",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

            // メモリ情報
            double memPercent = current.getMemoryPercent();
            Material memMaterial = memPercent < 70 ? Material.LIME_WOOL :
                    memPercent < 90 ? Material.YELLOW_WOOL : Material.RED_WOOL;

            inv.setItem(12, createItem(memMaterial,
                    ChatColor.RED + "💾 Memory Usage",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                    ChatColor.YELLOW + "Used: " + ChatColor.WHITE + current.memoryUsed + " MB",
                    ChatColor.YELLOW + "Total: " + ChatColor.WHITE + current.memoryTotal + " MB",
                    ChatColor.YELLOW + "Free: " + ChatColor.WHITE + (current.memoryTotal - current.memoryUsed) + " MB",
                    "",
                    getMemoryBar(memPercent),
                    ChatColor.WHITE + String.format("%.1f%%", memPercent),
                    "",
                    memPercent > 90 ? ChatColor.RED + "⚠ CRITICAL" :
                            memPercent > 70 ? ChatColor.YELLOW + "⚠ Warning" : ChatColor.GREEN + "✓ OK",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

            // エンティティ情報
            inv.setItem(14, createItem(Material.ZOMBIE_HEAD,
                    ChatColor.GREEN + "👾 Entities",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                    ChatColor.YELLOW + "Total: " + ChatColor.WHITE + current.entities,
                    ChatColor.YELLOW + "Per Player: " + ChatColor.WHITE +
                            (current.players > 0 ? current.entities / current.players : 0),
                    "",
                    current.entities > 1000 ? ChatColor.RED + "⚠ Very High" :
                            current.entities > 500 ? ChatColor.YELLOW + "⚠ High" : ChatColor.GREEN + "✓ Normal",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

            // チャンク情報
            inv.setItem(16, createItem(Material.GRASS_BLOCK,
                    ChatColor.AQUA + "🗺 Loaded Chunks",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                    ChatColor.YELLOW + "Total: " + ChatColor.WHITE + current.chunks,
                    ChatColor.YELLOW + "Per Player: " + ChatColor.WHITE +
                            (current.players > 0 ? current.chunks / current.players : 0),
                    "",
                    current.chunks > 5000 ? ChatColor.RED + "⚠ Very High" :
                            current.chunks > 2000 ? ChatColor.YELLOW + "⚠ High" : ChatColor.GREEN + "✓ Normal",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

            // プレイヤー情報
            inv.setItem(28, createItem(Material.PLAYER_HEAD,
                    ChatColor.YELLOW + "👥 Online Players",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                    ChatColor.YELLOW + "Online: " + ChatColor.WHITE + current.players,
                    ChatColor.YELLOW + "Max: " + ChatColor.WHITE + Bukkit.getMaxPlayers(),
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

            // 履歴グラフ（簡易版）
            if (!history.isEmpty()) {
                List<String> graphLore = new ArrayList<>();
                graphLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
                graphLore.add(ChatColor.YELLOW + "Last Hour Performance");
                graphLore.add("");

                int displayCount = Math.min(history.size(), 10);
                for (int i = displayCount - 1; i >= 0; i--) {
                    PerformanceMonitor.PerformanceMetric metric = history.get(i);
                    String time = metric.timestamp.length() >= 16 ?
                            metric.timestamp.substring(11, 16) : metric.timestamp;
                    String tpsBar = getTpsBar(metric.tps);
                    graphLore.add(ChatColor.GRAY + time + " " + tpsBar +
                            ChatColor.WHITE + String.format(" %.1f TPS", metric.tps));
                }

                graphLore.add("");
                graphLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");

                inv.setItem(31, createItem(Material.CLOCK,
                        ChatColor.GOLD + "📊 Performance History",
                        graphLore.toArray(new String[0])));
            } else {
                // 履歴がない場合
                inv.setItem(31, createItem(Material.CLOCK,
                        ChatColor.GOLD + "📊 Performance History",
                        ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                        ChatColor.YELLOW + "No history data available",
                        "",
                        ChatColor.WHITE + "Data will be collected over time",
                        ChatColor.GRAY + "Check back in a few minutes",
                        ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));
            }

            // サーバー稼働時間を追加
            long uptime = plugin.getUptimeMillis() / 1000;
            inv.setItem(34, createItem(Material.CLOCK,
                    ChatColor.AQUA + "⏱ Server Uptime",
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                    ChatColor.YELLOW + "Uptime: " + ChatColor.WHITE + formatUptime(uptime),
                    ChatColor.YELLOW + "Started: " + ChatColor.WHITE + plugin.getFormattedStartTime(),
                    ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));
        }

        // リフレッシュボタン
        inv.setItem(49, createItem(Material.COMPASS,
                ChatColor.GREEN + "🔄 Refresh",
                ChatColor.GRAY + "Reload performance data",
                "",
                ChatColor.YELLOW + "Click to refresh"));

        // 戻るボタン
        inv.setItem(53, createItem(Material.ARROW,
                ChatColor.YELLOW + "⬅ Back",
                ChatColor.GRAY + "Return to main menu"));

        player.openInventory(inv);
    }

    private String getTpsBar(double tps) {
        int bars = (int) ((tps / 20.0) * 20);
        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                if (tps >= 19.5) {
                    bar.append(ChatColor.GREEN).append("█");
                } else if (tps >= 18.0) {
                    bar.append(ChatColor.YELLOW).append("█");
                } else if (tps >= 15.0) {
                    bar.append(ChatColor.GOLD).append("█");
                } else {
                    bar.append(ChatColor.RED).append("█");
                }
            } else {
                bar.append(ChatColor.DARK_GRAY).append("█");
            }
        }

        return bar.toString();
    }

    private String getMemoryBar(double percent) {
        int bars = (int) (percent / 5);
        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < 20; i++) {
            if (i < bars) {
                if (percent > 90) {
                    bar.append(ChatColor.RED).append("█");
                } else if (percent > 70) {
                    bar.append(ChatColor.YELLOW).append("█");
                } else {
                    bar.append(ChatColor.GREEN).append("█");
                }
            } else {
                bar.append(ChatColor.DARK_GRAY).append("█");
            }
        }

        return bar.toString();
    }

    /**
     * 稼働時間をフォーマット
     */
    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }

        return item;
    }
}