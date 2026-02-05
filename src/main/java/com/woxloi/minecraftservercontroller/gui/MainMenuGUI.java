package com.woxloi.minecraftservercontroller.gui;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MainMenuGUI {
    
    private final MinecraftServerController plugin;
    
    public MainMenuGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "MSC Control Panel");
        
        // サーバー管理
        inv.setItem(10, createItem(Material.COMMAND_BLOCK, 
            ChatColor.GREEN + "Server Control",
            ChatColor.GRAY + "Start, stop, restart server",
            ChatColor.YELLOW + "Click to open"));
        
        // バックアップ管理
        inv.setItem(12, createItem(Material.CHEST,
            ChatColor.AQUA + "Backup Management",
            ChatColor.GRAY + "Create, list, restore backups",
            ChatColor.YELLOW + "Click to open"));
        
        // プレイヤー管理
        inv.setItem(14, createItem(Material.PLAYER_HEAD,
            ChatColor.LIGHT_PURPLE + "Player Management",
            ChatColor.GRAY + "Whitelist, OP, ban management",
            ChatColor.YELLOW + "Click to open"));
        
        // プラグイン管理
        inv.setItem(16, createItem(Material.PAPER,
            ChatColor.GOLD + "Plugin Management",
            ChatColor.GRAY + "List and reload plugins",
            ChatColor.YELLOW + "Click to open"));
        
        // サーバーステータス
        inv.setItem(28, createItem(Material.COMPASS,
            ChatColor.YELLOW + "Server Status",
            ChatColor.GRAY + "View server information",
            ChatColor.YELLOW + "Click to view"));
        
        // メトリクス
        inv.setItem(30, createItem(Material.CLOCK,
            ChatColor.RED + "Server Metrics",
            ChatColor.GRAY + "Memory and performance",
            ChatColor.YELLOW + "Click to view"));
        
        // オンラインプレイヤー
        inv.setItem(32, createItem(Material.EMERALD,
            ChatColor.GREEN + "Online Players",
            ChatColor.GRAY + "View online players",
            ChatColor.YELLOW + "Click to view"));
        
        // ログ
        inv.setItem(34, createItem(Material.BOOK,
            ChatColor.WHITE + "Server Logs",
            ChatColor.GRAY + "View server logs",
            ChatColor.YELLOW + "Click to view"));
        
        // 監査ログ
        if (player.hasPermission("msc.admin")) {
            inv.setItem(46, createItem(Material.WRITABLE_BOOK,
                ChatColor.DARK_PURPLE + "Audit Logs",
                ChatColor.GRAY + "View API audit logs",
                ChatColor.RED + "Admin only"));
        }
        
        // スケジュール
        if (player.hasPermission("msc.admin")) {
            inv.setItem(48, createItem(Material.REPEATER,
                ChatColor.BLUE + "Backup Schedules",
                ChatColor.GRAY + "View backup schedules",
                ChatColor.RED + "Admin only"));
        }
        
        // 設定リロード
        if (player.hasPermission("msc.reload")) {
            inv.setItem(50, createItem(Material.REDSTONE,
                ChatColor.RED + "Reload Config",
                ChatColor.GRAY + "Reload plugin config",
                ChatColor.RED + "Admin only"));
        }
        
        // 閉じる
        inv.setItem(53, createItem(Material.BARRIER,
            ChatColor.RED + "Close",
            ChatColor.GRAY + "Close this menu"));
        
        player.openInventory(inv);
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
