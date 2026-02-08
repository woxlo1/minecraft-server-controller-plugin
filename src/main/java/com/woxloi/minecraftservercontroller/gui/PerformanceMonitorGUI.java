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
 * パフォーマンスモニターGUI
 */
public class PerformanceMonitorGUI {

    private final MinecraftServerController plugin;

    public PerformanceMonitorGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Loading performance data...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PerformanceMonitor.CurrentPerformance current = plugin.getPerformanceMonitor().getCurrentPerformance();
            List<PerformanceMonitor.PerformanceMetric> history = plugin.getPerformanceMonitor().getMetrics(1);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                displayPerformance(player, current, history);
            });
        });
    }

    private void displayPerformance(Player player, PerformanceMonitor.CurrentPerformance current,
                                    List<PerformanceMonitor.PerformanceMetric> history) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Performance Monitor");

        if (current == null) {
            inv.setItem(22, createItem(Material.BARRIER,
                    ChatColor.RED + "Error",
                    ChatColor.GRAY + "Failed to load performance data"));
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

                for (int i = Math.min(history.size(), 10) - 1; i >= 0; i--) {
                    PerformanceMonitor.PerformanceMetric metric = history.get(i);
                    String time = metric.timestamp.substring(11, 16);
                    String tpsBar = getTpsBar(metric.tps);
                    graphLore.add(ChatColor.GRAY + time + " " + tpsBar +
                            ChatColor.WHITE + String.format(" %.1f TPS", metric.tps));
                }

                graphLore.add("");
                graphLore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");

                inv.setItem(31, createItem(Material.CLOCK,
                        ChatColor.GOLD + "📊 Performance History",
                        graphLore.toArray(new String[0])));
            }
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