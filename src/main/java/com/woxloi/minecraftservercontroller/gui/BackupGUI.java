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

public class BackupGUI {
    
    private final MinecraftServerController plugin;
    
    public BackupGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.AQUA + "Backup Management");
        
        // 新規バックアップ作成
        inv.setItem(10, createItem(Material.CHEST,
            ChatColor.GREEN + "Create New Backup",
            ChatColor.GRAY + "Create a manual backup",
            "",
            ChatColor.YELLOW + "Click to create"));
        
        // バックアップ一覧を表示
        inv.setItem(13, createItem(Material.ENDER_CHEST,
            ChatColor.YELLOW + "List Backups",
            ChatColor.GRAY + "View all backups",
            "",
            ChatColor.YELLOW + "Click to view"));
        
        // スケジュール管理
        if (player.hasPermission("msc.admin")) {
            inv.setItem(16, createItem(Material.REPEATER,
                ChatColor.BLUE + "Backup Schedules",
                ChatColor.GRAY + "Manage automatic backups",
                ChatColor.RED + "Admin only",
                "",
                ChatColor.YELLOW + "Click to open"));
        }
        
        // 戻る
        inv.setItem(49, createItem(Material.ARROW,
            ChatColor.YELLOW + "Back",
            ChatColor.GRAY + "Return to main menu"));
        
        player.openInventory(inv);
    }
    
    public void openBackupList(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Loading backups...");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<APIClient.BackupInfo> backups = plugin.getAPIClient().listBackups();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Inventory inv = Bukkit.createInventory(null, 54, ChatColor.AQUA + "Backup List");
                    
                    int slot = 0;
                    for (APIClient.BackupInfo backup : backups) {
                        if (slot >= 45) break;
                        
                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.GRAY + "Filename: " + ChatColor.WHITE + backup.filename);
                        lore.add(ChatColor.GRAY + "Size: " + ChatColor.WHITE + String.format("%.2f MB", backup.sizeMb));
                        lore.add(ChatColor.GRAY + "Modified: " + ChatColor.WHITE + backup.modified);
                        lore.add("");
                        lore.add(ChatColor.YELLOW + "Left-click to restore");
                        lore.add(ChatColor.RED + "Right-click to delete");
                        
                        inv.setItem(slot++, createItem(Material.PAPER,
                            ChatColor.AQUA + backup.filename,
                            lore.toArray(new String[0])));
                    }
                    
                    // 戻る
                    inv.setItem(49, createItem(Material.ARROW,
                        ChatColor.YELLOW + "Back",
                        ChatColor.GRAY + "Return to backup menu"));
                    
                    player.openInventory(inv);
                });
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Failed to load backups: " + e.getMessage());
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
