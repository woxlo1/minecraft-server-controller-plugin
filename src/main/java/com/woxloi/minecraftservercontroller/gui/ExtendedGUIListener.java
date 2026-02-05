package com.woxloi.minecraftservercontroller.gui;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import com.woxloi.minecraftservercontroller.api.APIClient;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ExtendedGUIListener implements Listener {
    
    private final MinecraftServerController plugin;
    
    public ExtendedGUIListener(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        // 新しいGUIのタイトルチェック
        if (!title.contains("MSC") && !title.contains("Backup") && 
            !title.contains("Player") && !title.contains("Plugin") && 
            !title.contains("Server") && !title.contains("Dashboard") &&
            !title.contains("Schedule") && !title.contains("Console") &&
            !title.contains("Audit") && !title.contains("Settings") &&
            !title.contains("Status")) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        
        String itemName = ChatColor.stripColor(meta.getDisplayName());
        
        // 各GUIの処理
        if (title.contains("Dashboard")) {
            handleDashboard(player, itemName);
        }
        else if (title.contains("Backup Schedules")) {
            handleBackupSchedules(player, itemName, event.getClick());
        }
        else if (title.contains("Console Commands")) {
            handleConsole(player, itemName);
        }
        else if (title.contains("Audit Logs")) {
            handleAuditLogs(player, itemName);
        }
        else if (title.contains("MSC Settings")) {
            handleSettings(player, itemName);
        }
        else if (title.contains("Server Status")) {
            handleServerStatus(player, itemName);
        }
    }
    
    private void handleDashboard(Player player, String itemName) {
        switch (itemName) {
            case "Server Status":
                new ServerStatusGUI(plugin).open(player);
                break;
                
            case "Online Players":
                new PlayerManagementGUI(plugin).open(player);
                break;
                
            case "Memory Usage":
                player.closeInventory();
                player.performCommand("msc metrics");
                break;
                
            case "Latest Backup":
            case "No Backups":
                new BackupGUI(plugin).open(player);
                break;
                
            case "Backup Schedules":
                new BackupScheduleGUI(plugin).open(player);
                break;
                
            case "Server Control":
                new ServerControlGUI(plugin).open(player);
                break;
                
            case "Backup Management":
                new BackupGUI(plugin).open(player);
                break;
                
            case "Plugin Management":
                if (!player.hasPermission("msc.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                new PluginGUI(plugin).open(player);
                break;
                
            case "Refresh Dashboard":
                new DashboardGUI(plugin).open(player);
                break;
                
            case "Main Menu":
                new MainMenuGUI(plugin).open(player);
                break;
        }
    }
    
    private void handleBackupSchedules(Player player, String itemName, ClickType clickType) {
        if (itemName.equals("Cron Format Help")) {
            // ヘルプは何もしない
            return;
        }
        
        if (itemName.equals("Back")) {
            new MainMenuGUI(plugin).open(player);
            return;
        }
        
        // スケジュール名からIDを取得（実装が必要）
        // 暫定的な実装：スケジュール名を使う
        
        if (clickType.isLeftClick()) {
            // トグル処理
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Toggling schedule: " + itemName);
            player.sendMessage(ChatColor.GRAY + "Use command: /msc schedule toggle <id>");
        } else if (clickType.isRightClick()) {
            // 削除処理
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "Deleting schedule: " + itemName);
            player.sendMessage(ChatColor.GRAY + "Use command: /msc schedule delete <id>");
        }
    }
    
    private void handleConsole(Player player, String itemName) {
        if (itemName.equals("Back")) {
            new MainMenuGUI(plugin).open(player);
            return;
        }
        
        if (itemName.equals("Custom Command")) {
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Use: /msc exec <command>");
            return;
        }
        
        // コマンドショートカット
        player.closeInventory();
        
        String command = null;
        switch (itemName) {
            case "say Hello":
                command = "say Hello from MSC!";
                break;
            case "tp @a ~ ~ ~":
                command = "tp @a ~ ~ ~";
                break;
            case "time set day":
                command = "time set day";
                break;
            case "weather clear":
                command = "weather clear";
                break;
            case "give @a minecraft:diamond 64":
                command = "give @a minecraft:diamond 64";
                break;
            case "difficulty peaceful":
                command = "difficulty peaceful";
                break;
            case "difficulty hard":
                command = "difficulty hard";
                break;
        }
        
        if (command != null) {
            new ConsoleGUI(plugin).executeCommand(player, command);
        }
    }
    
    private void handleAuditLogs(Player player, String itemName) {
        switch (itemName) {
            case "Refresh":
                new AuditLogGUI(plugin).open(player);
                break;
                
            case "Back":
                new MainMenuGUI(plugin).open(player);
                break;
        }
    }
    
    private void handleSettings(Player player, String itemName) {
        switch (itemName) {
            case "Debug Mode":
                new SettingsGUI(plugin).toggleDebugMode(player);
                break;
                
            case "Reload Configuration":
                new SettingsGUI(plugin).reloadConfig(player);
                break;
                
            case "API URL":
            case "API Key":
            case "Request Timeout":
            case "Configuration File":
                player.sendMessage(ChatColor.YELLOW + "Use commands to change this setting");
                player.sendMessage(ChatColor.GRAY + "See item description for details");
                break;
                
            case "Reset to Defaults":
                player.sendMessage(ChatColor.RED + "Use: /msc config reset");
                break;
                
            case "Backup/Restore Config":
                player.sendMessage(ChatColor.YELLOW + "Use: /msc config backup or /msc config restore");
                break;
                
            case "Back":
                new MainMenuGUI(plugin).open(player);
                break;
        }
    }
    
    private void handleServerStatus(Player player, String itemName) {
        switch (itemName) {
            case "Refresh":
                new ServerStatusGUI(plugin).open(player);
                break;
                
            case "Back":
                new MainMenuGUI(plugin).open(player);
                break;
        }
    }
}
