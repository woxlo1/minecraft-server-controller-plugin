package com.woxloi.minecraftservercontroller.commands;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import com.woxloi.minecraftservercontroller.api.APIClient;
import com.woxloi.minecraftservercontroller.gui.MainMenuGUI;
import com.woxloi.minecraftservercontroller.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class MSCCommand implements CommandExecutor, TabCompleter {

    private final MinecraftServerController plugin;

    public MSCCommand(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            // GUI を開く（プレイヤーの場合）
            if (sender instanceof Player) {
                Player player = (Player) sender;
                new MainMenuGUI(plugin).open(player);
                return true;
            } else {
                sendHelp(sender);
                return true;
            }
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                return true;

            case "gui":
                return handleGUI(sender);

            case "backup":
                return handleBackup(sender, args);

            case "status":
                return handleStatus(sender);

            case "players":
                return handlePlayers(sender);

            case "exec":
                return handleExec(sender, args);

            case "metrics":
                return handleMetrics(sender);

            case "server":
                return handleServer(sender, args);

            case "whitelist":
                return handleWhitelist(sender, args);

            case "op":
                return handleOp(sender, args);

            case "plugins":
                return handlePlugins(sender, args);

            case "logs":
                return handleLogs(sender, args);

            case "audit":
                return handleAudit(sender);

            case "schedule":
                return handleSchedules(sender, args);

            case "reload":
                return handleReload(sender);

            // =============================
            // 新機能のコマンド（v1.3.9）
            // =============================

            case "performance":
            case "perf":
                return handlePerformance(sender, args);

            case "world":
            case "worlds":
                return handleWorld(sender, args);

            case "chat":
                return handleChat(sender, args);

            case "template":
            case "cmd":
                return handleTemplate(sender, args);

            case "stats":
                return handleStats(sender, args);

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + subCommand);
                sender.sendMessage(ChatColor.YELLOW + "Use /msc help for available commands");
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "MSC Commands" + ChatColor.GOLD + " ==========");
        sender.sendMessage(ChatColor.YELLOW + "/msc" + ChatColor.WHITE + " - Open GUI menu");
        sender.sendMessage(ChatColor.YELLOW + "/msc gui" + ChatColor.WHITE + " - Open GUI menu");
        sender.sendMessage(ChatColor.YELLOW + "/msc help" + ChatColor.WHITE + " - Show this help");
        sender.sendMessage(ChatColor.YELLOW + "/msc status" + ChatColor.WHITE + " - Server status");
        sender.sendMessage(ChatColor.YELLOW + "/msc backup [list|delete|restore]" + ChatColor.WHITE + " - Backup management");
        sender.sendMessage(ChatColor.YELLOW + "/msc server [start|stop]" + ChatColor.WHITE + " - Server control");
        sender.sendMessage(ChatColor.YELLOW + "/msc players" + ChatColor.WHITE + " - Online players");
        sender.sendMessage(ChatColor.YELLOW + "/msc whitelist [add|remove|list|on|off]" + ChatColor.WHITE + " - Whitelist management");
        sender.sendMessage(ChatColor.YELLOW + "/msc op [add|remove] <player>" + ChatColor.WHITE + " - OP management");
        sender.sendMessage(ChatColor.YELLOW + "/msc plugins [list|reload]" + ChatColor.WHITE + " - Plugin management");
        sender.sendMessage(ChatColor.YELLOW + "/msc logs [tail]" + ChatColor.WHITE + " - View server logs");
        sender.sendMessage(ChatColor.YELLOW + "/msc audit" + ChatColor.WHITE + " - View audit logs");
        sender.sendMessage(ChatColor.YELLOW + "/msc schedule [create|list|toggle|delete]" + ChatColor.WHITE + " - Backup schedules");
        sender.sendMessage(ChatColor.YELLOW + "/msc metrics" + ChatColor.WHITE + " - Server metrics");
        sender.sendMessage(ChatColor.YELLOW + "/msc exec <command>" + ChatColor.WHITE + " - Execute command");
        sender.sendMessage(ChatColor.YELLOW + "/msc reload" + ChatColor.WHITE + " - Reload config");

        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.AQUA + "New in v1.3.9" + ChatColor.GOLD + " ==========");
        sender.sendMessage(ChatColor.YELLOW + "/msc performance" + ChatColor.WHITE + " - Performance monitoring");
        sender.sendMessage(ChatColor.YELLOW + "/msc world [list|load|unload|backup]" + ChatColor.WHITE + " - World management");
        sender.sendMessage(ChatColor.YELLOW + "/msc chat [search|player]" + ChatColor.WHITE + " - Chat log viewer");
        sender.sendMessage(ChatColor.YELLOW + "/msc template [add|list|use]" + ChatColor.WHITE + " - Command templates");
        sender.sendMessage(ChatColor.YELLOW + "/msc stats [player]" + ChatColor.WHITE + " - Player statistics");
    }

    // =============================
    // 既存のハンドラ（省略 - ドキュメント1,2,3,4と同じ）
    // =============================

    private boolean handleGUI(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        new MainMenuGUI(plugin).open(player);
        return true;
    }

    private boolean handleBackup(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            String action = args[1].toLowerCase();

            switch (action) {
                case "list":
                    return handleBackupList(sender);
                case "delete":
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /msc backup delete <filename>");
                        return true;
                    }
                    return handleBackupDelete(sender, args[2]);
                case "restore":
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /msc backup restore <filename>");
                        return true;
                    }
                    return handleBackupRestore(sender, args[2]);
                default:
                    sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                    return true;
            }
        }

        if (!sender.hasPermission("msc.backup")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Creating backup...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.BackupResult result = plugin.getAPIClient().createBackup();
                sender.sendMessage(ChatColor.GREEN + "✓ Backup created: " + ChatColor.WHITE + result.filename);

                if (plugin.getNotificationManager() != null) {
                    plugin.getNotificationManager().notifyBackupCreated(result.filename);
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());

                if (plugin.getNotificationManager() != null) {
                    plugin.getNotificationManager().notifyError("Backup creation failed: " + e.getMessage());
                }
            }
        });

        return true;
    }

    private boolean handleBackupList(CommandSender sender) {
        if (!sender.hasPermission("msc.backup.list")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Fetching backups...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<APIClient.BackupInfo> backups = plugin.getAPIClient().listBackups();

                if (backups.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No backups found.");
                    return;
                }

                sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Backups" + ChatColor.GOLD + " ==========");
                for (APIClient.BackupInfo backup : backups) {
                    sender.sendMessage(ChatColor.YELLOW + "• " + ChatColor.WHITE + backup.filename);
                    sender.sendMessage(ChatColor.GRAY + "  Size: " + String.format("%.2f MB", backup.sizeMb) +
                            " | Modified: " + backup.modified);
                }
                sender.sendMessage(ChatColor.GOLD + "Total: " + ChatColor.WHITE + backups.size());

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleBackupDelete(CommandSender sender, String filename) {
        if (!sender.hasPermission("msc.backup.restore")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Deleting backup...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getAPIClient().deleteBackup(filename);
                sender.sendMessage(ChatColor.GREEN + "✓ Backup deleted: " + ChatColor.WHITE + filename);

                if (plugin.getNotificationManager() != null) {
                    plugin.getNotificationManager().notifyBackupDeleted(filename);
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleBackupRestore(CommandSender sender, String filename) {
        if (!sender.hasPermission("msc.backup.restore")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "WARNING: This will stop the server and restore from backup!");
        sender.sendMessage(ChatColor.YELLOW + "Restoring backup...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.RestoreResult result = plugin.getAPIClient().restoreBackup(filename);
                sender.sendMessage(ChatColor.GREEN + "✓ Backup restored: " + ChatColor.WHITE + result.backup);
                sender.sendMessage(ChatColor.GRAY + "Pre-restore backup: " + result.preRestoreBackup);

                if (plugin.getNotificationManager() != null) {
                    plugin.getNotificationManager().notifyBackupRestored(result.backup);
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("msc.server")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Fetching status...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.ServerStatus status = plugin.getAPIClient().getServerStatus();

                sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Server Status" + ChatColor.GOLD + " ==========");

                String statusColor = status.status.toLowerCase().contains("up") ?
                        ChatColor.GREEN + "" : ChatColor.RED + "";
                sender.sendMessage(ChatColor.YELLOW + "Status: " + statusColor + status.status.toUpperCase());

                if (status.container != null && status.container.has("State")) {
                    String state = status.container.get("State").getAsString();
                    sender.sendMessage(ChatColor.YELLOW + "Container: " + ChatColor.WHITE + state);
                }

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handlePlayers(CommandSender sender) {
        if (!sender.hasPermission("msc.players")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Fetching players...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.PlayerList playerList = plugin.getAPIClient().getPlayers();

                sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Players" + ChatColor.GOLD + " ==========");
                sender.sendMessage(ChatColor.YELLOW + "Total: " + ChatColor.WHITE + playerList.count);

                if (!playerList.players.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "Players: " + ChatColor.WHITE +
                            String.join(", ", playerList.players));
                }

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleExec(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.exec")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /msc exec <command>");
            return true;
        }

        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        sender.sendMessage(ChatColor.YELLOW + "Executing: " + ChatColor.WHITE + command);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.CommandResult result = plugin.getAPIClient().executeCommand(command);

                sender.sendMessage(ChatColor.GREEN + "✓ Executed");
                if (!result.output.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "Output: " + ChatColor.WHITE + result.output);
                }

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleMetrics(CommandSender sender) {
        if (!sender.hasPermission("msc.server")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Fetching metrics...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.MetricsInfo metrics = plugin.getAPIClient().getMetrics();

                sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Metrics" + ChatColor.GOLD + " ==========");
                sender.sendMessage(ChatColor.YELLOW + "Memory Total: " + ChatColor.WHITE +
                        String.format("%.2f GB", metrics.totalGb));
                sender.sendMessage(ChatColor.YELLOW + "Memory Used: " + ChatColor.WHITE +
                        String.format("%.2f GB", metrics.usedGb));
                sender.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE +
                        String.format("%.1f%%", metrics.percent));

                if (plugin.getNotificationManager() != null) {
                    if (metrics.percent >= 95) {
                        plugin.getNotificationManager().notifyCriticalMemoryUsage(metrics.percent);
                    } else if (metrics.percent >= 90) {
                        plugin.getNotificationManager().notifyHighMemoryUsage(metrics.percent);
                    }
                }

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleServer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.server.control")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /msc server [start|stop]");
            return true;
        }

        String action = args[1].toLowerCase();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String result;
                if (action.equals("start")) {
                    if (plugin.getNotificationManager() != null) {
                        plugin.getNotificationManager().notifyServerStarting();
                    }

                    result = plugin.getAPIClient().startServer();
                    sender.sendMessage(ChatColor.GREEN + "✓ Server starting: " + result);
                } else if (action.equals("stop")) {
                    if (plugin.getNotificationManager() != null) {
                        plugin.getNotificationManager().notifyServerStopping();
                    }

                    result = plugin.getAPIClient().stopServer();
                    sender.sendMessage(ChatColor.GREEN + "✓ Server stopping: " + result);
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                    return;
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleWhitelist(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.whitelist")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /msc whitelist [add|remove|list|on|off]");
            return true;
        }

        String action = args[1].toLowerCase();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                switch (action) {
                    case "add":
                        if (args.length < 3) {
                            sender.sendMessage(ChatColor.RED + "Usage: /msc whitelist add <player>");
                            return;
                        }
                        String addResult = plugin.getAPIClient().whitelistAdd(args[2]);
                        sender.sendMessage(ChatColor.GREEN + "✓ " + addResult);

                        if (plugin.getNotificationManager() != null) {
                            plugin.getNotificationManager().notifyWhitelistAdded(args[2]);
                        }
                        break;

                    case "remove":
                        if (args.length < 3) {
                            sender.sendMessage(ChatColor.RED + "Usage: /msc whitelist remove <player>");
                            return;
                        }
                        String removeResult = plugin.getAPIClient().whitelistRemove(args[2]);
                        sender.sendMessage(ChatColor.GREEN + "✓ " + removeResult);

                        if (plugin.getNotificationManager() != null) {
                            plugin.getNotificationManager().notifyWhitelistRemoved(args[2]);
                        }
                        break;

                    case "list":
                        List<String> whitelist = plugin.getAPIClient().getWhitelist();
                        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Whitelist" + ChatColor.GOLD + " ==========");
                        if (whitelist.isEmpty()) {
                            sender.sendMessage(ChatColor.YELLOW + "No players whitelisted");
                        } else {
                            sender.sendMessage(ChatColor.YELLOW + "Players: " + ChatColor.WHITE + String.join(", ", whitelist));
                        }
                        break;

                    case "on":
                        String onResult = plugin.getAPIClient().whitelistEnable();
                        sender.sendMessage(ChatColor.GREEN + "✓ " + onResult);
                        break;

                    case "off":
                        String offResult = plugin.getAPIClient().whitelistDisable();
                        sender.sendMessage(ChatColor.GREEN + "✓ " + offResult);
                        break;

                    default:
                        sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                        break;
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleOp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /msc op [add|remove] <player>");
            return true;
        }

        String action = args[1].toLowerCase();
        String player = args[2];

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String result;
                if (action.equals("add")) {
                    result = plugin.getAPIClient().opAdd(player);

                    if (plugin.getNotificationManager() != null) {
                        plugin.getNotificationManager().notifyOpGranted(player);
                    }
                } else if (action.equals("remove")) {
                    result = plugin.getAPIClient().opRemove(player);

                    if (plugin.getNotificationManager() != null) {
                        plugin.getNotificationManager().notifyOpRevoked(player);
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                    return;
                }
                sender.sendMessage(ChatColor.GREEN + "✓ " + result);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handlePlugins(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            args = new String[]{"plugins", "list"};
        }

        String action = args[1].toLowerCase();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (action.equals("list")) {
                    List<APIClient.PluginInfo> plugins = plugin.getAPIClient().listPlugins();
                    sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Plugins" + ChatColor.GOLD + " ==========");
                    if (plugins.isEmpty()) {
                        sender.sendMessage(ChatColor.YELLOW + "No plugins found");
                    } else {
                        for (APIClient.PluginInfo p : plugins) {
                            sender.sendMessage(ChatColor.YELLOW + "• " + ChatColor.WHITE + p.name +
                                    ChatColor.GRAY + " (" + String.format("%.2f MB", p.sizeMb) + ")");
                        }
                    }
                } else if (action.equals("reload")) {
                    String result = plugin.getAPIClient().reloadPlugins();
                    sender.sendMessage(ChatColor.GREEN + "✓ " + result);

                    if (plugin.getNotificationManager() != null) {
                        plugin.getNotificationManager().notifyPluginReloaded();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                }
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleLogs(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        boolean tail = args.length >= 2 && args[1].equalsIgnoreCase("tail");

        sender.sendMessage(ChatColor.YELLOW + "Fetching logs...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String logs = plugin.getAPIClient().getLogs();

                if (tail) {
                    String[] lines = logs.split("\n");
                    int start = Math.max(0, lines.length - 20);
                    sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Logs (last 20)" + ChatColor.GOLD + " ==========");
                    for (int i = start; i < lines.length; i++) {
                        sender.sendMessage(ChatColor.GRAY + lines[i]);
                    }
                } else {
                    sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Logs" + ChatColor.GOLD + " ==========");
                    sender.sendMessage(ChatColor.GRAY + "Too long to display. Use /msc logs tail for last 20 lines");
                }

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleAudit(CommandSender sender) {
        if (!sender.hasPermission("msc.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Fetching audit logs...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<APIClient.AuditLog> logs = plugin.getAPIClient().getAuditLogs();

                sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Audit Logs" + ChatColor.GOLD + " ==========");

                int count = Math.min(10, logs.size());
                for (int i = 0; i < count; i++) {
                    APIClient.AuditLog log = logs.get(i);
                    sender.sendMessage(ChatColor.YELLOW + log.time.substring(11, 19) + ChatColor.GRAY + " | " +
                            ChatColor.AQUA + log.role + ChatColor.GRAY + " | " +
                            ChatColor.WHITE + log.action + ChatColor.GRAY + " | " +
                            log.detail);
                }
                sender.sendMessage(ChatColor.GRAY + "Showing " + count + " of " + logs.size() + " logs");

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleSchedules(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length >= 2) {
            String action = args[1].toLowerCase();

            switch (action) {
                case "create":
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /msc schedule create <name> <min> <hour> <day> <month> <weekday> <max>");
                    }
                    return handleScheduleCreate(sender, args);

                case "toggle":
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /msc schedule toggle <id>");
                        return true;
                    }
                    return handleScheduleToggle(sender, args[2]);

                case "delete":
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /msc schedule delete <id>");
                        return true;
                    }
                    return handleScheduleDelete(sender, args[2]);

                case "list":
                    break;

                default:
                    sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /msc schedule [create|list|toggle|delete]");
                    return true;
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "Fetching schedules...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<APIClient.BackupSchedule> schedules = plugin.getAPIClient().listBackupSchedules();

                sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Backup Schedules" + ChatColor.GOLD + " ==========");

                if (schedules.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No schedules found");
                } else {
                    for (APIClient.BackupSchedule schedule : schedules) {
                        String status = schedule.enabled ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
                        sender.sendMessage(status + ChatColor.YELLOW + " [ID:" + schedule.id + "] " + schedule.name);
                        sender.sendMessage(ChatColor.GRAY + "  Cron: " + schedule.cronExpression +
                                " | Max backups: " + schedule.maxBackups);
                        if (schedule.lastRun != null) {
                            sender.sendMessage(ChatColor.GRAY + "  Last run: " + schedule.lastRun);
                        }
                    }
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GRAY + "Use: /msc schedule toggle <id> to enable/disable");
                    sender.sendMessage(ChatColor.GRAY + "Use: /msc schedule delete <id> to delete");
                }

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());

            }
        });

        return true;
    }

    private boolean handleScheduleCreate(CommandSender sender, String[] args) {

        if (args.length < 9) {
            return true;
        }

        String name = args[2];

        int max;
        try {
            max = Integer.parseInt(args[args.length - 1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Max backups must be a number!");
            return true;
        }

        String cron = String.join(" ",
                Arrays.copyOfRange(args, 3, args.length - 1));

        sender.sendMessage(ChatColor.YELLOW + "Creating schedule...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {

                APIClient.BackupSchedule result =
                        plugin.getAPIClient().createBackupSchedule(name, cron, max);

                sender.sendMessage(ChatColor.GREEN + "✓ Schedule created!");
                sender.sendMessage(ChatColor.YELLOW + "ID: " + result.id);
                sender.sendMessage(ChatColor.GRAY + "Name: " + result.name);
                sender.sendMessage(ChatColor.GRAY + "Status: " +
                        (result.enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));

            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleScheduleToggle(CommandSender sender, String idStr) {
        try {
            int id = Integer.parseInt(idStr);

            sender.sendMessage(ChatColor.YELLOW + "Toggling schedule...");

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    APIClient.ScheduleToggleResult result = plugin.getAPIClient().toggleBackupSchedule(id);
                    String status = result.enabled ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED";
                    sender.sendMessage(ChatColor.GREEN + "✓ Schedule [ID:" + result.id + "] is now " + status);
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                }
            });

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid ID: " + idStr);
        }

        return true;
    }

    private boolean handleScheduleDelete(CommandSender sender, String idStr) {
        try {
            int id = Integer.parseInt(idStr);

            sender.sendMessage(ChatColor.YELLOW + "Deleting schedule...");

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    String result = plugin.getAPIClient().deleteBackupSchedule(id);
                    sender.sendMessage(ChatColor.GREEN + "✓ Schedule [ID:" + id + "] deleted");
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                }
            });

        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid ID: " + idStr);
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("msc.reload")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "✓ Configuration reloaded!");

        return true;
    }

    // =============================
    // 新機能のハンドラ（v1.3.9）
    // =============================

    private boolean handlePerformance(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.performance")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (sender instanceof Player) {
            new com.woxloi.minecraftservercontroller.gui.PerformanceMonitorGUI(plugin).open((Player) sender);
            return true;
        }

        // コンソールの場合
        PerformanceMonitor.CurrentPerformance perf = plugin.getPerformanceMonitor().getCurrentPerformance();
        if (perf == null) {
            sender.sendMessage(ChatColor.RED + "Failed to get performance data");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Performance" + ChatColor.GOLD + " ==========");
        sender.sendMessage(ChatColor.YELLOW + "TPS (1m): " + ChatColor.WHITE + String.format("%.2f", perf.tps1m));
        sender.sendMessage(ChatColor.YELLOW + "TPS (5m): " + ChatColor.WHITE + String.format("%.2f", perf.tps5m));
        sender.sendMessage(ChatColor.YELLOW + "TPS (15m): " + ChatColor.WHITE + String.format("%.2f", perf.tps15m));
        sender.sendMessage(ChatColor.YELLOW + "Memory: " + ChatColor.WHITE +
                String.format("%d/%d MB (%.1f%%)", perf.memoryUsed, perf.memoryTotal, perf.getMemoryPercent()));
        sender.sendMessage(ChatColor.YELLOW + "Entities: " + ChatColor.WHITE + perf.entities);
        sender.sendMessage(ChatColor.YELLOW + "Chunks: " + ChatColor.WHITE + perf.chunks);
        sender.sendMessage(ChatColor.YELLOW + "Players: " + ChatColor.WHITE + perf.players);

        return true;
    }

    private boolean handleWorld(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.world")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            // GUI表示（プレイヤーのみ）
            if (sender instanceof Player) {
                new com.woxloi.minecraftservercontroller.gui.WorldManagementGUI(plugin).open((Player) sender);
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /msc world [list|load|unload|backup] <name>");
                return true;
            }
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "list":
                List<WorldManager.WorldInfo> worlds = plugin.getWorldManager().getAvailableWorlds();
                sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Worlds" + ChatColor.GOLD + " ==========");
                for (WorldManager.WorldInfo world : worlds) {
                    String status = world.loaded ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
                    sender.sendMessage(status + ChatColor.YELLOW + " " + world.name +
                            ChatColor.GRAY + " (" + world.sizeMB + " MB, " + world.environment + ")");
                }
                break;

            case "load":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /msc world load <name>");
                    return true;
                }
                String loadWorld = args[2];
                sender.sendMessage(ChatColor.YELLOW + "Loading world: " + loadWorld);
                boolean loaded = plugin.getWorldManager().loadWorld(loadWorld);
                if (loaded) {
                    sender.sendMessage(ChatColor.GREEN + "✓ World loaded: " + loadWorld);
                } else {
                    sender.sendMessage(ChatColor.RED + "✗ Failed to load world");
                }
                break;

            case "unload":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /msc world unload <name>");
                    return true;
                }
                String unloadWorld = args[2];
                sender.sendMessage(ChatColor.YELLOW + "Unloading world: " + unloadWorld);
                boolean unloaded = plugin.getWorldManager().unloadWorld(unloadWorld, true);
                if (unloaded) {
                    sender.sendMessage(ChatColor.GREEN + "✓ World unloaded: " + unloadWorld);
                } else {
                    sender.sendMessage(ChatColor.RED + "✗ Failed to unload world");
                }
                break;

            case "backup":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /msc world backup <name>");
                    return true;
                }
                String backupWorld = args[2];
                sender.sendMessage(ChatColor.YELLOW + "Backing up world: " + backupWorld);

                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        File backup = plugin.getWorldManager().backupWorld(backupWorld);
                        sender.sendMessage(ChatColor.GREEN + "✓ World backed up: " + backup.getName());
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                    }
                });
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                sender.sendMessage(ChatColor.YELLOW + "Usage: /msc world [list|load|unload|backup]");
        }

        return true;
    }

    private boolean handleChat(CommandSender sender, String[] args) {
        if (!sender.hasPermission("msc.chat")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission!");
            return true;
        }

        if (args.length < 2) {
            // GUI表示（プレイヤーのみ）
            if (sender instanceof Player) {
                new com.woxloi.minecraftservercontroller.gui.ChatLogViewerGUI(plugin).open((Player) sender);
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /msc chat [search|player] <term>");
                return true;
            }
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "search":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /msc chat search <keyword>");
                    return true;
                }
                String keyword = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                List<ChatLogManager.ChatMessage> results = plugin.getChatLogManager().searchMessages(keyword, 20);

                sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Chat Search: " + keyword + ChatColor.GOLD + " ==========");
                if (results.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No messages found");
                } else {
                    for (ChatLogManager.ChatMessage msg : results) {
                        sender.sendMessage(ChatColor.GRAY + msg.getFormattedTimestamp() + ChatColor.YELLOW + " [" + msg.playerName + "] " +
                                ChatColor.WHITE + msg.message);
                    }
                    sender.sendMessage(ChatColor.GRAY + "Found " + results.size() + " messages");
                }
                break;

            case "player":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /msc chat player <name>");
                    return true;
                }
                String playerName = args[2];
                Player target = Bukkit.getPlayer(playerName);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
                    return true;
                }

                List<ChatLogManager.ChatMessage> playerMsgs = plugin.getChatLogManager().getPlayerMessages(target.getUniqueId(), 20);

                sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Chat: " + playerName + ChatColor.GOLD + " ==========");
                if (playerMsgs.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No messages found");
                } else {
                    for (ChatLogManager.ChatMessage msg : playerMsgs) {
                        sender.sendMessage(ChatColor.GRAY + msg.getFormattedTimestamp() + " " + ChatColor.WHITE + msg.message);
                    }
                    sender.sendMessage(ChatColor.GRAY + "Showing " + playerMsgs.size() + " messages");
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
                sender.sendMessage(ChatColor.YELLOW + "Usage: /msc chat [search|player]");
        }

        return true;
    }

    private boolean handleTemplate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /msc template [add|list|use|remove] ...");
            return true;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /msc template add <name> <command>");
                    return true;
                }
                String name = args[2];
                String command = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                plugin.getTemplateManager().addTemplate(player.getUniqueId(), name, command, "User template");
                sender.sendMessage(ChatColor.GREEN + "✓ Template added: " + name);
                break;

            case "list":
                List<CommandTemplateManager.CommandTemplate> templates =
                        plugin.getTemplateManager().getTemplates(player.getUniqueId());

                if (templates.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No templates found");
                } else {
                    sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + "Templates" + ChatColor.GOLD + " ==========");
                    for (CommandTemplateManager.CommandTemplate template : templates) {
                        sender.sendMessage(ChatColor.YELLOW + "• " + template.name + ChatColor.GRAY + " - " +
                                ChatColor.WHITE + template.command);
                    }
                }
                break;

            case "use":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /msc template use <name>");
                    return true;
                }
                String templateName = args[2];
                CommandTemplateManager.CommandTemplate template =
                        plugin.getTemplateManager().getTemplate(player.getUniqueId(), templateName);

                if (template == null) {
                    sender.sendMessage(ChatColor.RED + "Template not found: " + templateName);
                    return true;
                }

                if (template.hasPlaceholders()) {
                    sender.sendMessage(ChatColor.YELLOW + "Template: " + template.command);
                    sender.sendMessage(ChatColor.YELLOW + "Placeholders: " + template.getPlaceholders());
                    sender.sendMessage(ChatColor.GRAY + "Use: /msc exec " + template.command);
                } else {
                    player.performCommand("msc exec " + template.command);
                }
                break;

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /msc template remove <name>");
                    return true;
                }
                String removeName = args[2];
                boolean removed = plugin.getTemplateManager().removeTemplate(player.getUniqueId(), removeName);
                if (removed) {
                    sender.sendMessage(ChatColor.GREEN + "✓ Template removed: " + removeName);
                } else {
                    sender.sendMessage(ChatColor.RED + "Template not found: " + removeName);
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown action: " + action);
        }

        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) && args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /msc stats [player]");
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                return true;
            }

            if (!sender.hasPermission("msc.admin") && sender != target) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to view other players' stats!");
                return true;
            }
        } else {
            target = (Player) sender;
        }

        PlayerActivityTracker.PlayerStats stats = plugin.getActivityTracker().getPlayerStats(target.getUniqueId());

        if (stats == null) {
            sender.sendMessage(ChatColor.YELLOW + "No statistics found for " + target.getName());
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "========== " + ChatColor.GREEN + stats.playerName + "'s Stats" + ChatColor.GOLD + " ==========");
        sender.sendMessage(ChatColor.YELLOW + "Total Playtime: " + ChatColor.WHITE + stats.getFormattedPlaytime());
        sender.sendMessage(ChatColor.YELLOW + "Total Sessions: " + ChatColor.WHITE + stats.totalSessions);
        sender.sendMessage(ChatColor.YELLOW + "First Join: " + ChatColor.WHITE + stats.firstJoin);
        sender.sendMessage(ChatColor.YELLOW + "Last Join: " + ChatColor.WHITE + stats.lastJoin);

        String recentActivity = plugin.getActivityTracker().getRecentActivity(target.getUniqueId(), 5);
        if (!recentActivity.isEmpty()) {
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "Recent Activity:");
            sender.sendMessage(ChatColor.GRAY + recentActivity);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(
                    "help", "gui", "backup", "status", "players", "exec", "metrics",
                    "server", "whitelist", "op", "plugins", "logs", "audit", "schedule", "reload",
                    "performance", "world", "chat", "template", "stats"
            );

            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "backup":
                    return Arrays.asList("list", "delete", "restore").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "server":
                    return Arrays.asList("start", "stop").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "whitelist":
                    return Arrays.asList("add", "remove", "list", "on", "off").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "op":
                    return Arrays.asList("add", "remove").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "plugins":
                    return Arrays.asList("list", "reload").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "logs":
                    return Arrays.asList("tail").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "schedule":
                    return Arrays.asList("list", "create", "toggle", "delete").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "world":
                    return Arrays.asList("list", "load", "unload", "backup").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "chat":
                    return Arrays.asList("search", "player").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "template":
                    return Arrays.asList("add", "list", "use", "remove").stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
            }
        }

        return completions;
    }
}