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

public class SettingsGUI {
    
    private final MinecraftServerController plugin;
    
    public SettingsGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        if (!player.hasPermission("msc.reload")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "MSC Settings");
        
        // 現在の設定を取得
        String apiUrl = plugin.getConfig().getString("api.url", "http://localhost:8000");
        String apiKey = plugin.getConfig().getString("api.key", "");
        boolean debug = plugin.getConfig().getBoolean("plugin.debug", false);
        int timeout = plugin.getConfig().getInt("plugin.timeout", 30);
        
        // API URL
        inv.setItem(10, createItem(Material.PAPER,
            ChatColor.YELLOW + "API URL",
            ChatColor.GRAY + "Current: " + ChatColor.WHITE + apiUrl,
            "",
            ChatColor.YELLOW + "Use /msc config set api.url <url>",
            ChatColor.GRAY + "to change"));
        
        // API Key
        String maskedKey = apiKey.isEmpty() ? ChatColor.RED + "NOT SET" : 
                          apiKey.substring(0, Math.min(8, apiKey.length())) + "...";
        inv.setItem(11, createItem(Material.TRIPWIRE_HOOK,
            ChatColor.YELLOW + "API Key",
            ChatColor.GRAY + "Current: " + ChatColor.WHITE + maskedKey,
            "",
            ChatColor.YELLOW + "Use /msc config set api.key <key>",
            ChatColor.GRAY + "to change"));
        
        // Debug Mode
        Material debugMaterial = debug ? Material.GREEN_WOOL : Material.RED_WOOL;
        inv.setItem(12, createItem(debugMaterial,
            ChatColor.YELLOW + "Debug Mode",
            ChatColor.GRAY + "Status: " + (debug ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"),
            "",
            ChatColor.YELLOW + "Click to toggle"));
        
        // Timeout
        inv.setItem(13, createItem(Material.CLOCK,
            ChatColor.YELLOW + "Request Timeout",
            ChatColor.GRAY + "Current: " + ChatColor.WHITE + timeout + " seconds",
            "",
            ChatColor.YELLOW + "Use /msc config set timeout <seconds>",
            ChatColor.GRAY + "to change"));
        
        // 設定リロード
        inv.setItem(16, createItem(Material.REDSTONE,
            ChatColor.GREEN + "Reload Configuration",
            ChatColor.GRAY + "Reload config.yml",
            "",
            ChatColor.YELLOW + "Click to reload"));
        
        // 設定ファイルを開く（説明のみ）
        inv.setItem(19, createItem(Material.WRITABLE_BOOK,
            ChatColor.AQUA + "Configuration File",
            ChatColor.GRAY + "Location: plugins/MinecraftServerController/config.yml",
            "",
            ChatColor.YELLOW + "Edit manually for advanced settings"));
        
        // デフォルト設定に戻す
        inv.setItem(20, createItem(Material.BARRIER,
            ChatColor.RED + "Reset to Defaults",
            ChatColor.GRAY + "Reset all settings to default values",
            ChatColor.RED + "⚠ This cannot be undone!",
            "",
            ChatColor.YELLOW + "Use /msc config reset"));
        
        // 設定のエクスポート/インポート
        inv.setItem(21, createItem(Material.BOOK,
            ChatColor.GOLD + "Backup/Restore Config",
            ChatColor.GRAY + "Save current config as backup",
            "",
            ChatColor.YELLOW + "Use /msc config backup",
            ChatColor.YELLOW + "Use /msc config restore"));
        
        // 戻る
        inv.setItem(26, createItem(Material.ARROW,
            ChatColor.YELLOW + "Back",
            ChatColor.GRAY + "Return to main menu"));
        
        player.openInventory(inv);
    }
    
    public void toggleDebugMode(Player player) {
        boolean currentDebug = plugin.getConfig().getBoolean("plugin.debug", false);
        plugin.getConfig().set("plugin.debug", !currentDebug);
        plugin.saveConfig();
        
        player.sendMessage(ChatColor.GREEN + "✓ Debug mode " + 
            (!currentDebug ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
        
        // GUIを再読み込み
        open(player);
    }
    
    public void reloadConfig(Player player) {
        plugin.reloadConfig();
        player.sendMessage(ChatColor.GREEN + "✓ Configuration reloaded!");
        
        // GUIを再読み込み
        open(player);
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
