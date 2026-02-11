package com.woxloi.minecraftservercontroller.gui;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import com.woxloi.minecraftservercontroller.api.APIClient;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

public class GUIListener implements Listener {

    private final MinecraftServerController plugin;

    // v1.4.1 fix: contains("Server") のような部分一致では他プラグインのGUIも
    //             誤ってキャンセルされてしまうため、固定セットで完全一致チェックに変更
    private static final Set<String> MSC_GUI_TITLES = Set.of(
            "MSC Control Panel",
            "Server Dashboard",
            "Server Control",
            "Server Status",
            "Backup Management",
            "Backup List",
            "Backup Schedules",
            "Player Management",
            "Plugin Management",
            "Console Commands",
            "Audit Logs",
            "MSC Settings",
            "Performance Monitor",
            "World Management",
            "Chat Log Viewer",
            "Online Players"
    );

    public GUIListener(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // v1.4.1 fix: カラーコードを除いたタイトルで完全一致チェック
        String rawTitle = event.getView().getTitle();
        String title = ChatColor.stripColor(rawTitle);

        if (!MSC_GUI_TITLES.contains(title)) {
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

        switch (title) {
            case "MSC Control Panel":   handleMainMenu(player, itemName);               break;
            case "Server Dashboard":    handleDashboard(player, itemName);              break;
            case "Server Control":      handleServerControl(player, itemName);          break;
            case "Server Status":       handleServerStatus(player, itemName);           break;
            case "Backup Management":   handleBackupMenu(player, itemName);             break;
            case "Backup List":         handleBackupList(player, itemName, event.getClick()); break;
            case "Backup Schedules":    handleBackupSchedules(player, itemName, event.getClick()); break;
            case "Player Management":   handlePlayerManagement(player, itemName);       break;
            case "Plugin Management":   handlePluginGUI(player, itemName);              break;
            case "Console Commands":    handleConsole(player, itemName);                break;
            case "Audit Logs":          handleAuditLogs(player, itemName);              break;
            case "MSC Settings":        handleSettings(player, itemName);               break;
            case "Performance Monitor": handlePerformanceMonitor(player, itemName);     break;
            case "World Management":
                // v1.4.1 fix: clickType と slot を渡してワールド操作を実装
                handleWorldManagement(player, itemName, event.getClick(), event.getSlot());
                break;
            case "Chat Log Viewer":     handleChatLog(player, itemName);                break;
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

    // v1.4.1 fix: clickType と slot を引数に追加して実際のワールド操作を実装
    private void handleWorldManagement(Player player, String itemName, ClickType clickType, int slot) {
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

        // ワールドアイテム（スロット 0〜44）
        if (slot >= 45) return;

        if (!player.hasPermission("msc.world")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }

        // v1.4.1 fix: アイテム名から "✓ " / "○ " プレフィックスとカラーコードを除いてワールド名を取得
        //             WorldManagementGUI のアイテム名フォーマット:
        //             (ChatColor.GREEN + "✓ " | ChatColor.GRAY + "○ ") + ChatColor.AQUA + worldName
        String worldName = ChatColor.stripColor(itemName).replaceAll("^[✓○] ", "").trim();
        if (worldName.isEmpty()) return;

        if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            // Shift+クリック: ワールドバックアップ
            player.closeInventory();
            player.sendMessage(ChatColor.YELLOW + "Backing up world: " + worldName + "...");
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    java.io.File backup = plugin.getWorldManager().backupWorld(worldName);
                    player.sendMessage(ChatColor.GREEN + "✓ World backed up: " + backup.getName());
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "✗ Failed to backup: " + e.getMessage());
                }
            });

        } else if (clickType.isRightClick()) {
            // 右クリック: スポーンへテレポート（ロード済みのみ）
            World w = Bukkit.getWorld(worldName);
            if (w != null) {
                player.closeInventory();
                player.teleport(w.getSpawnLocation());
                player.sendMessage(ChatColor.GREEN + "✓ Teleported to spawn of " + worldName);
            } else {
                player.sendMessage(ChatColor.RED + "World is not loaded: " + worldName);
            }

        } else if (clickType.isLeftClick()) {
            // 左クリック: ロード/アンロードをトグル
            player.closeInventory();
            World w = Bukkit.getWorld(worldName);
            if (w != null) {
                boolean result = plugin.getWorldManager().unloadWorld(worldName, true);
                player.sendMessage(result
                        ? ChatColor.GREEN + "✓ World unloaded: " + worldName
                        : ChatColor.RED   + "✗ Failed to unload: " + worldName);
            } else {
                boolean result = plugin.getWorldManager().loadWorld(worldName);
                player.sendMessage(result
                        ? ChatColor.GREEN + "✓ World loaded: " + worldName
                        : ChatColor.RED   + "✗ Failed to load: " + worldName);
            }
            // GUIを再描画
            plugin.getServer().getScheduler().runTaskLater(plugin,
                    () -> new WorldManagementGUI(plugin).open(player), 10L);
        }
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