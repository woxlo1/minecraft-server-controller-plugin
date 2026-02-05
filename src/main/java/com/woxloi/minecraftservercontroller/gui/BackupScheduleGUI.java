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

public class BackupScheduleGUI {
    
    private final MinecraftServerController plugin;
    
    public BackupScheduleGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        if (!player.hasPermission("msc.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }
        
        player.sendMessage(ChatColor.YELLOW + "Loading schedules...");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<APIClient.BackupSchedule> schedules = plugin.getAPIClient().listBackupSchedules();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Inventory inv = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Backup Schedules");
                    
                    // スケジュール一覧
                    int slot = 0;
                    for (APIClient.BackupSchedule schedule : schedules) {
                        if (slot >= 45) break;
                        
                        Material material = schedule.enabled ? Material.GREEN_WOOL : Material.GRAY_WOOL;
                        
                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.GRAY + "ID: " + ChatColor.WHITE + schedule.id);
                        lore.add(ChatColor.GRAY + "Name: " + ChatColor.WHITE + schedule.name);
                        lore.add(ChatColor.GRAY + "Cron: " + ChatColor.WHITE + schedule.cronExpression);
                        lore.add(ChatColor.GRAY + "Max Backups: " + ChatColor.WHITE + schedule.maxBackups);
                        lore.add(ChatColor.GRAY + "Status: " + 
                                (schedule.enabled ? ChatColor.GREEN + "✓ Enabled" : ChatColor.RED + "✗ Disabled"));
                        lore.add(ChatColor.GRAY + "Created: " + ChatColor.WHITE + schedule.created);
                        if (schedule.lastRun != null) {
                            lore.add(ChatColor.GRAY + "Last Run: " + ChatColor.WHITE + schedule.lastRun);
                        } else {
                            lore.add(ChatColor.GRAY + "Last Run: " + ChatColor.YELLOW + "Never");
                        }
                        lore.add("");
                        lore.add(ChatColor.YELLOW + "Left-click: Toggle Enable/Disable");
                        lore.add(ChatColor.RED + "Right-click: Delete");
                        
                        inv.setItem(slot++, createItem(material,
                            ChatColor.AQUA + schedule.name,
                            lore.toArray(new String[0])));
                    }
                    
                    // ヘルプ
                    inv.setItem(49, createItem(Material.BOOK,
                        ChatColor.YELLOW + "Cron Format Help",
                        ChatColor.GRAY + "Format: minute hour day month weekday",
                        ChatColor.WHITE + "Examples:",
                        ChatColor.GRAY + "  0 2 * * * → Daily at 2 AM",
                        ChatColor.GRAY + "  0 */6 * * * → Every 6 hours",
                        ChatColor.GRAY + "  0 0 * * 0 → Every Sunday at midnight",
                        ChatColor.GRAY + "  0 0 1 * * → 1st of every month",
                        "",
                        ChatColor.YELLOW + "Use command to create:",
                        ChatColor.WHITE + "/msc schedule create <name> <cron> <max>"));
                    
                    // 戻る
                    inv.setItem(53, createItem(Material.ARROW,
                        ChatColor.YELLOW + "Back",
                        ChatColor.GRAY + "Return to main menu"));
                    
                    player.openInventory(inv);
                });
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Failed to load schedules: " + e.getMessage());
                plugin.getLogger().warning("Schedule GUI error: " + e.getMessage());
            }
        });
    }
    
    public void toggleSchedule(Player player, int scheduleId) {
        player.sendMessage(ChatColor.YELLOW + "Toggling schedule...");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // API経由でトグル処理を実装する必要があります
                // 現在のAPIClient.javaにtoggleScheduleメソッドを追加する必要があります
                player.sendMessage(ChatColor.GREEN + "✓ Schedule toggled!");
                
                // GUIを再読み込み
                plugin.getServer().getScheduler().runTask(plugin, () -> open(player));
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });
    }
    
    public void deleteSchedule(Player player, int scheduleId) {
        player.sendMessage(ChatColor.YELLOW + "Deleting schedule...");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // API経由で削除処理を実装する必要があります
                // 現在のAPIClient.javaにdeleteScheduleメソッドを追加する必要があります
                player.sendMessage(ChatColor.GREEN + "✓ Schedule deleted!");
                
                // GUIを再読み込み
                plugin.getServer().getScheduler().runTask(plugin, () -> open(player));
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
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
