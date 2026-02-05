package com.woxloi.minecraftservercontroller.gui;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import com.woxloi.minecraftservercontroller.api.APIClient;
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

public class DashboardGUI {
    
    private final MinecraftServerController plugin;
    
    public DashboardGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Loading dashboard...");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // データ取得
                APIClient.ServerStatus status = plugin.getAPIClient().getServerStatus();
                APIClient.PlayerList players = plugin.getAPIClient().getPlayers();
                APIClient.MetricsInfo metrics = plugin.getAPIClient().getMetrics();
                List<APIClient.BackupInfo> backups = plugin.getAPIClient().listBackups();
                List<APIClient.BackupSchedule> schedules = plugin.getAPIClient().listBackupSchedules();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Server Dashboard");
                    
                    // サーバーステータス
                    Material statusMaterial = status.status.toLowerCase().contains("up") 
                        ? Material.GREEN_WOOL : Material.RED_WOOL;
                    List<String> statusLore = new ArrayList<>();
                    statusLore.add(ChatColor.GRAY + "Status: " + (status.status.toLowerCase().contains("up")
                        ? ChatColor.GREEN + "RUNNING" : ChatColor.RED + "STOPPED"));
                    if (status.container != null && status.container.has("State")) {
                        statusLore.add(ChatColor.GRAY + "Container: " + ChatColor.WHITE + 
                            status.container.get("State").getAsString());
                    }
                    statusLore.add("");
                    statusLore.add(ChatColor.YELLOW + "Click to view details");
                    
                    inv.setItem(10, createItem(statusMaterial,
                        ChatColor.YELLOW + "Server Status",
                        statusLore.toArray(new String[0])));
                    
                    // オンラインプレイヤー
                    List<String> playerLore = new ArrayList<>();
                    playerLore.add(ChatColor.GRAY + "Count: " + ChatColor.WHITE + players.count);
                    if (!players.players.isEmpty()) {
                        playerLore.add(ChatColor.GRAY + "Players:");
                        for (String p : players.players) {
                            playerLore.add(ChatColor.WHITE + "  • " + p);
                        }
                    }
                    playerLore.add("");
                    playerLore.add(ChatColor.YELLOW + "Click for player management");
                    
                    inv.setItem(12, createItem(Material.PLAYER_HEAD,
                        ChatColor.GREEN + "Online Players",
                        playerLore.toArray(new String[0])));
                    
                    // メモリ使用率
                    Material memMaterial = metrics.percent < 70 ? Material.LIME_WOOL :
                                          metrics.percent < 90 ? Material.YELLOW_WOOL : Material.RED_WOOL;
                    inv.setItem(14, createItem(memMaterial,
                        ChatColor.RED + "Memory Usage",
                        ChatColor.GRAY + "Used: " + ChatColor.WHITE + 
                            String.format("%.2f GB / %.2f GB", metrics.usedGb, metrics.totalGb),
                        ChatColor.GRAY + "Usage: " + ChatColor.WHITE + 
                            String.format("%.1f%%", metrics.percent),
                        "",
                        metrics.percent > 90 ? ChatColor.RED + "⚠ High memory usage!" : 
                            ChatColor.GREEN + "✓ Memory OK"));
                    
                    // 最新バックアップ
                    if (!backups.isEmpty()) {
                        APIClient.BackupInfo latest = backups.get(0);
                        inv.setItem(16, createItem(Material.CHEST,
                            ChatColor.AQUA + "Latest Backup",
                            ChatColor.GRAY + "File: " + ChatColor.WHITE + latest.filename,
                            ChatColor.GRAY + "Size: " + ChatColor.WHITE + 
                                String.format("%.2f MB", latest.sizeMb),
                            ChatColor.GRAY + "Date: " + ChatColor.WHITE + latest.modified,
                            "",
                            ChatColor.YELLOW + "Click for backup management"));
                    } else {
                        inv.setItem(16, createItem(Material.BARRIER,
                            ChatColor.RED + "No Backups",
                            ChatColor.GRAY + "No backups found",
                            "",
                            ChatColor.YELLOW + "Click to create backup"));
                    }
                    
                    // アクティブなスケジュール
                    long activeSchedules = schedules.stream().filter(s -> s.enabled).count();
                    List<String> scheduleLore = new ArrayList<>();
                    scheduleLore.add(ChatColor.GRAY + "Total: " + ChatColor.WHITE + schedules.size());
                    scheduleLore.add(ChatColor.GRAY + "Active: " + ChatColor.GREEN + activeSchedules);
                    
                    if (!schedules.isEmpty()) {
                        scheduleLore.add("");
                        scheduleLore.add(ChatColor.YELLOW + "Next schedules:");
                        int count = 0;
                        for (APIClient.BackupSchedule schedule : schedules) {
                            if (schedule.enabled && count < 3) {
                                scheduleLore.add(ChatColor.WHITE + "  • " + schedule.name + 
                                    ChatColor.GRAY + " (" + schedule.cronExpression + ")");
                                count++;
                            }
                        }
                    }
                    scheduleLore.add("");
                    scheduleLore.add(ChatColor.YELLOW + "Click for schedule management");
                    
                    inv.setItem(28, createItem(Material.CLOCK,
                        ChatColor.BLUE + "Backup Schedules",
                        scheduleLore.toArray(new String[0])));
                    
                    // サーバーコントロール
                    inv.setItem(30, createItem(Material.COMMAND_BLOCK,
                        ChatColor.GREEN + "Server Control",
                        ChatColor.GRAY + "Start, stop, restart server",
                        "",
                        ChatColor.YELLOW + "Click to open"));
                    
                    // バックアップ管理
                    inv.setItem(32, createItem(Material.ENDER_CHEST,
                        ChatColor.AQUA + "Backup Management",
                        ChatColor.GRAY + "Total backups: " + ChatColor.WHITE + backups.size(),
                        "",
                        ChatColor.YELLOW + "Click to manage"));
                    
                    // プラグイン管理（管理者のみ）
                    if (player.hasPermission("msc.admin")) {
                        inv.setItem(34, createItem(Material.PAPER,
                            ChatColor.GOLD + "Plugin Management",
                            ChatColor.GRAY + "Manage server plugins",
                            ChatColor.RED + "Admin only",
                            "",
                            ChatColor.YELLOW + "Click to open"));
                    }
                    
                    // リフレッシュボタン
                    inv.setItem(49, createItem(Material.COMPASS,
                        ChatColor.YELLOW + "Refresh Dashboard",
                        ChatColor.GRAY + "Reload dashboard data",
                        "",
                        ChatColor.YELLOW + "Click to refresh"));
                    
                    // メインメニューへ戻る
                    inv.setItem(53, createItem(Material.ARROW,
                        ChatColor.YELLOW + "Main Menu",
                        ChatColor.GRAY + "Return to main menu"));
                    
                    player.openInventory(inv);
                });
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Failed to load dashboard: " + e.getMessage());
                plugin.getLogger().warning("Dashboard error: " + e.getMessage());
            }
        });
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
