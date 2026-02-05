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

public class GUIListener implements Listener {
    
    private final MinecraftServerController plugin;
    
    public GUIListener(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (!title.contains("MSC") && !title.contains("Backup") && 
            !title.contains("Player") && !title.contains("Plugin") && 
            !title.contains("Server")) {
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
        
        // メインメニュー
        if (title.contains("Control Panel")) {
            handleMainMenu(player, itemName);
        }
        // サーバーコントロール
        else if (title.contains("Server Control")) {
            handleServerControl(player, itemName);
        }
        // バックアップメニュー
        else if (title.contains("Backup Management")) {
            handleBackupMenu(player, itemName);
        }
        // バックアップリスト
        else if (title.contains("Backup List")) {
            handleBackupList(player, itemName, event.getClick());
        }
        // プレイヤー管理
        else if (title.contains("Player Management")) {
            handlePlayerManagement(player, itemName);
        }
        // プラグイン管理
        else if (title.contains("Plugin Management")) {
            handlePluginGUI(player, itemName);
        }
    }
    
    private void handleMainMenu(Player player, String itemName) {
        switch (itemName) {
            case "Server Control":
                new ServerControlGUI(plugin).open(player);
                break;
                
            case "Backup Management":
                new BackupGUI(plugin).open(player);
                break;
                
            case "Player Management":
                new PlayerManagementGUI(plugin).open(player);
                break;
                
            case "Plugin Management":
                if (!player.hasPermission("msc.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                new PluginGUI(plugin).open(player);
                break;
                
            case "Server Status":
                player.closeInventory();
                player.performCommand("msc status");
                break;
                
            case "Server Metrics":
                player.closeInventory();
                player.performCommand("msc metrics");
                break;
                
            case "Online Players":
                player.closeInventory();
                player.performCommand("msc players");
                break;
                
            case "Server Logs":
                player.closeInventory();
                player.performCommand("msc logs tail");
                break;
                
            case "Audit Logs":
                if (!player.hasPermission("msc.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                player.closeInventory();
                player.performCommand("msc audit");
                break;
                
            case "Backup Schedules":
                if (!player.hasPermission("msc.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                player.closeInventory();
                player.performCommand("msc schedules");
                break;
                
            case "Reload Config":
                if (!player.hasPermission("msc.reload")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                player.closeInventory();
                plugin.reloadConfig();
                player.sendMessage(ChatColor.GREEN + "✓ Configuration reloaded!");
                break;
                
            case "Close":
                player.closeInventory();
                break;
        }
    }
    
    private void handleServerControl(Player player, String itemName) {
        if (!player.hasPermission("msc.server.control")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }
        
        switch (itemName) {
            case "Start Server":
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Starting server...");
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        String result = plugin.getAPIClient().startServer();
                        player.sendMessage(ChatColor.GREEN + "✓ Server starting: " + result);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                    }
                });
                break;
                
            case "Stop Server":
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Stopping server...");
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        String result = plugin.getAPIClient().stopServer();
                        player.sendMessage(ChatColor.GREEN + "✓ Server stopping: " + result);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                    }
                });
                break;
                
            case "Server Status":
                player.closeInventory();
                player.performCommand("msc status");
                break;
                
            case "Back":
                new MainMenuGUI(plugin).open(player);
                break;
        }
    }
    
    private void handlePlayerManagement(Player player, String itemName) {
        switch (itemName) {
            case "Online Players":
                player.closeInventory();
                player.performCommand("msc players");
                break;
                
            case "Back":
                new MainMenuGUI(plugin).open(player);
                break;
        }
    }
    
    private void handlePluginGUI(Player player, String itemName) {
        if (!player.hasPermission("msc.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }
        
        switch (itemName) {
            case "Reload Plugins":
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Reloading plugins...");
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        String result = plugin.getAPIClient().reloadPlugins();
                        player.sendMessage(ChatColor.GREEN + "✓ " + result);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                    }
                });
                break;
                
            case "Back":
                new MainMenuGUI(plugin).open(player);
                break;
        }
    }
    
    private void handleBackupMenu(Player player, String itemName) {
        switch (itemName) {
            case "Create New Backup":
                if (!player.hasPermission("msc.backup")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Creating backup...");
                
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        APIClient.BackupResult result = plugin.getAPIClient().createBackup();
                        player.sendMessage(ChatColor.GREEN + "✓ Backup created: " + result.filename);
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                    }
                });
                break;
                
            case "List Backups":
                if (!player.hasPermission("msc.backup.list")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                new BackupGUI(plugin).openBackupList(player);
                break;
                
            case "Backup Schedules":
                if (!player.hasPermission("msc.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                player.closeInventory();
                player.performCommand("msc schedules");
                break;
                
            case "Back":
                new MainMenuGUI(plugin).open(player);
                break;
        }
    }
    
    private void handleBackupList(Player player, String itemName, ClickType clickType) {
        if (itemName.equals("Back")) {
            new BackupGUI(plugin).open(player);
            return;
        }
        
        // バックアップファイル名を取得
        String filename = itemName;
        
        if (clickType.isLeftClick()) {
            // リストア
            if (!player.hasPermission("msc.backup.restore")) {
                player.sendMessage(ChatColor.RED + "You don't have permission!");
                return;
            }
            
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "WARNING: This will stop the server!");
            player.sendMessage(ChatColor.YELLOW + "Restoring backup: " + filename);
            
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    APIClient.RestoreResult result = plugin.getAPIClient().restoreBackup(filename);
                    player.sendMessage(ChatColor.GREEN + "✓ Backup restored: " + result.backup);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                }
            });
            
        } else if (clickType.isRightClick()) {
            // 削除
            if (!player.hasPermission("msc.backup.restore")) {
                player.sendMessage(ChatColor.RED + "You don't have permission!");
                return;
            }
            
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Deleting backup: " + filename);
            
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    plugin.getAPIClient().deleteBackup(filename);
                    player.sendMessage(ChatColor.GREEN + "✓ Backup deleted: " + filename);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                }
            });
        }
    }
}
