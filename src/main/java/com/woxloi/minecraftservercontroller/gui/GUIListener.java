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
                !title.contains("Server") && !title.contains("Dashboard") &&
                !title.contains("Schedule") && !title.contains("Console") &&
                !title.contains("Audit") && !title.contains("Settings") &&
                !title.contains("Status") && !title.contains("Control Panel") &&
                !title.contains("Performance") && !title.contains("World") &&
                !title.contains("Chat Log")) {
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
        // ダッシュボード
        else if (title.contains("Dashboard")) {
            handleDashboard(player, itemName);
        }
        // サーバーコントロール
        else if (title.contains("Server Control")) {
            handleServerControl(player, itemName);
        }
        // サーバーステータス
        else if (title.contains("Server Status")) {
            handleServerStatus(player, itemName);
        }
        // バックアップメニュー
        else if (title.contains("Backup Management")) {
            handleBackupMenu(player, itemName);
        }
        // バックアップリスト
        else if (title.contains("Backup List")) {
            handleBackupList(player, itemName, event.getClick());
        }
        // バックアップスケジュール
        else if (title.contains("Backup Schedules")) {
            handleBackupSchedules(player, itemName, event.getClick());
        }
        // プレイヤー管理
        else if (title.contains("Player Management")) {
            handlePlayerManagement(player, itemName);
        }
        // プラグイン管理
        else if (title.contains("Plugin Management")) {
            handlePluginGUI(player, itemName);
        }
        // コンソール
        else if (title.contains("Console Commands")) {
            handleConsole(player, itemName);
        }
        // 監査ログ
        else if (title.contains("Audit Logs")) {
            handleAuditLogs(player, itemName);
        }
        // 設定
        else if (title.contains("MSC Settings")) {
            handleSettings(player, itemName);
        }
        // パフォーマンスモニター（新機能）
        else if (title.contains("Performance Monitor")) {
            handlePerformanceMonitor(player, itemName);
        }
        // ワールド管理（新機能）
        else if (title.contains("World Management")) {
            handleWorldManagement(player, itemName);
        }
        // チャットログ（新機能）
        else if (title.contains("Chat Log")) {
            handleChatLog(player, itemName);
        }
    }

    private void handleMainMenu(Player player, String itemName) {
        switch (itemName) {
            case "★ Dashboard":
                new DashboardGUI(plugin).open(player);
                break;

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

            case "★ Console Commands":
                new ConsoleGUI(plugin).open(player);
                break;

            case "★ Performance Monitor":
                if (!player.hasPermission("msc.performance")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                new PerformanceMonitorGUI(plugin).open(player);
                break;

            case "★ World Management":
                if (!player.hasPermission("msc.world")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                new WorldManagementGUI(plugin).open(player);
                break;

            case "★ Chat Log Viewer":
                if (!player.hasPermission("msc.chat")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                new ChatLogViewerGUI(plugin).open(player);
                break;

            case "Server Status":
                new ServerStatusGUI(plugin).open(player);
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

            case "★ Audit Logs":
                if (!player.hasPermission("msc.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                new AuditLogGUI(plugin).open(player);
                break;

            case "★ Backup Schedules":
                if (!player.hasPermission("msc.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                new BackupScheduleGUI(plugin).open(player);
                break;

            case "★ Settings":
                if (!player.hasPermission("msc.reload")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission!");
                    return;
                }
                new SettingsGUI(plugin).open(player);
                break;

            case "Close":
                player.closeInventory();
                break;
        }
    }

    private void handleDashboard(Player player, String itemName) {
        switch (itemName) {
            case "Server Status":
                new ServerStatusGUI(plugin).open(player);
                break;

            case "Online Players":
                new OnlinePlayersGUI(plugin).open(player);
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
                new ServerStatusGUI(plugin).open(player);
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
                new BackupScheduleGUI(plugin).open(player);
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

    private void handleBackupSchedules(Player player, String itemName, ClickType clickType) {
        if (itemName.equals("Cron Format Help")) {
            return;
        }

        if (itemName.equals("Back")) {
            new MainMenuGUI(plugin).open(player);
            return;
        }

        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Schedule management: " + itemName);
        player.sendMessage(ChatColor.GRAY + "Use commands:");
        player.sendMessage(ChatColor.WHITE + "/msc schedule toggle <id>");
        player.sendMessage(ChatColor.WHITE + "/msc schedule delete <id>");
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

    // =============================
    // 新機能のハンドラ（v1.3.9）
    // =============================

    private void handlePerformanceMonitor(Player player, String itemName) {
        switch (itemName) {
            case "🔄 Refresh":
                new PerformanceMonitorGUI(plugin).open(player);
                break;

            case "⬅ Back":
                new MainMenuGUI(plugin).open(player);
                break;
        }
    }

    private void handleWorldManagement(Player player, String itemName) {
        if (itemName.equals("🔄 Refresh")) {
            new WorldManagementGUI(plugin).open(player);
            return;
        }

        if (itemName.equals("⬅ Back")) {
            new MainMenuGUI(plugin).open(player);
            return;
        }

        if (itemName.equals("📊 World Statistics")) {
            return; // 情報表示のみ
        }

        // ワールド名の処理
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "Use commands to manage worlds:");
        player.sendMessage(ChatColor.WHITE + "/msc world load <name>");
        player.sendMessage(ChatColor.WHITE + "/msc world unload <name>");
        player.sendMessage(ChatColor.WHITE + "/msc world backup <name>");
    }

    private void handleChatLog(Player player, String itemName) {
        switch (itemName) {
            case "🔄 Refresh":
                new ChatLogViewerGUI(plugin).open(player);
                break;

            case "⬅ Back":
                new MainMenuGUI(plugin).open(player);
                break;

            case "🔍 Search":
            case "👤 Player Filter":
            case "📊 Chat Statistics":
                // 情報表示のみ
                break;
        }
    }
}