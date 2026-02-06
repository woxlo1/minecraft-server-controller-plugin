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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 改善されたバックアップスケジュールGUI
 * - スケジュールの有効/無効切り替え
 * - スケジュール削除
 * - 詳細情報表示
 * - スケジュール作成ウィザード
 */
public class BackupScheduleGUI {

    private final MinecraftServerController plugin;
    // スケジュールIDとインベントリスロットのマッピング
    private final Map<Integer, Integer> slotToScheduleId = new HashMap<>();

    public BackupScheduleGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    /**
     * メインのスケジュール管理GUI
     */
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
                    displaySchedules(player, schedules);
                });

            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Failed to load schedules: " + e.getMessage());
                plugin.getLogger().warning("Schedule GUI error: " + e.getMessage());
            }
        });
    }

    /**
     * スケジュール一覧を表示
     */
    private void displaySchedules(Player player, List<APIClient.BackupSchedule> schedules) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.BLUE + "Backup Schedules");
        slotToScheduleId.clear();

        // スケジュール一覧（最大45個）
        int slot = 0;
        for (APIClient.BackupSchedule schedule : schedules) {
            if (slot >= 45) break;

            // スケジュールIDとスロットをマッピング
            slotToScheduleId.put(slot, schedule.id);

            // 有効/無効でアイコンを変更
            Material material = schedule.enabled ? Material.GREEN_WOOL : Material.GRAY_WOOL;

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
            lore.add(ChatColor.AQUA + "Schedule ID: " + ChatColor.WHITE + schedule.id);
            lore.add("");
            lore.add(ChatColor.YELLOW + "⏰ Cron Expression:");
            lore.add(ChatColor.WHITE + "  " + schedule.cronExpression);
            lore.add(ChatColor.GRAY + "  " + getCronDescription(schedule.cronExpression));
            lore.add("");
            lore.add(ChatColor.YELLOW + "📦 Max Backups: " + ChatColor.WHITE + schedule.maxBackups + " generations");
            lore.add("");
            lore.add(ChatColor.YELLOW + "📅 Status:");
            lore.add(schedule.enabled
                    ? ChatColor.GREEN + "  ✓ ENABLED - Running automatically"
                    : ChatColor.RED + "  ✗ DISABLED - Paused");
            lore.add("");
            lore.add(ChatColor.YELLOW + "📊 Last Run:");
            if (schedule.lastRun != null && !schedule.lastRun.isEmpty()) {
                String formattedTime = formatDateTime(schedule.lastRun);
                lore.add(ChatColor.WHITE + "  " + formattedTime);
            } else {
                lore.add(ChatColor.GRAY + "  Never executed");
            }
            lore.add("");
            lore.add(ChatColor.YELLOW + "📝 Created:");
            lore.add(ChatColor.WHITE + "  " + formatDateTime(schedule.created));
            lore.add("");
            lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
            lore.add("");
            lore.add(ChatColor.GOLD + "⚡ Actions:");
            lore.add(ChatColor.GREEN + "  LEFT-CLICK: " + ChatColor.WHITE + "Toggle Enable/Disable");
            lore.add(ChatColor.RED + "  RIGHT-CLICK: " + ChatColor.WHITE + "Delete Schedule");
            lore.add(ChatColor.YELLOW + "  SHIFT-CLICK: " + ChatColor.WHITE + "View Details");

            inv.setItem(slot++, createItem(material,
                    (schedule.enabled ? ChatColor.GREEN + "✓ " : ChatColor.RED + "✗ ") +
                            ChatColor.AQUA + schedule.name,
                    lore.toArray(new String[0])));
        }

        // 空のスロットに説明を追加
        if (schedules.isEmpty()) {
            inv.setItem(22, createItem(Material.BARRIER,
                    ChatColor.YELLOW + "No Schedules",
                    ChatColor.GRAY + "No backup schedules configured",
                    "",
                    ChatColor.AQUA + "Use commands to create:",
                    ChatColor.WHITE + "/msc schedule create <name> <cron> <max>"));
        }

        // 新規スケジュール作成（コマンド案内）
        inv.setItem(45, createItem(Material.EMERALD,
                ChatColor.GREEN + "➕ Create New Schedule",
                ChatColor.GRAY + "Create a new backup schedule",
                "",
                ChatColor.YELLOW + "Use command:",
                ChatColor.WHITE + "/msc schedule create <name> <cron> <max>",
                "",
                ChatColor.GRAY + "Example:",
                ChatColor.WHITE + "/msc schedule create daily \"0 2 * * *\" 7",
                ChatColor.GRAY + "Creates a daily backup at 2 AM"));

        // プリセット例
        inv.setItem(46, createItem(Material.BOOK,
                ChatColor.YELLOW + "📖 Preset Examples",
                ChatColor.GRAY + "Common backup schedules",
                "",
                ChatColor.AQUA + "Hourly:" + ChatColor.WHITE + " 0 * * * *",
                ChatColor.AQUA + "Every 6h:" + ChatColor.WHITE + " 0 */6 * * *",
                ChatColor.AQUA + "Daily 2AM:" + ChatColor.WHITE + " 0 2 * * *",
                ChatColor.AQUA + "Weekly Sun:" + ChatColor.WHITE + " 0 0 * * 0",
                ChatColor.AQUA + "Monthly 1st:" + ChatColor.WHITE + " 0 0 1 * *",
                "",
                ChatColor.YELLOW + "Copy and use with /msc schedule create"));

        // Cronヘルプ
        inv.setItem(47, createItem(Material.WRITABLE_BOOK,
                ChatColor.GOLD + "❓ Cron Format Help",
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                ChatColor.YELLOW + "Format:",
                ChatColor.WHITE + "  minute hour day month weekday",
                ChatColor.GRAY + "  │      │    │   │     │",
                ChatColor.GRAY + "  │      │    │   │     └─ Day of week (0-6, 0=Sun)",
                ChatColor.GRAY + "  │      │    │   └─────── Month (1-12)",
                ChatColor.GRAY + "  │      │    └─────────── Day (1-31)",
                ChatColor.GRAY + "  │      └──────────────── Hour (0-23)",
                ChatColor.GRAY + "  └─────────────────────── Minute (0-59)",
                "",
                ChatColor.YELLOW + "Special characters:",
                ChatColor.WHITE + "  * " + ChatColor.GRAY + "= Any value",
                ChatColor.WHITE + "  */n " + ChatColor.GRAY + "= Every n units",
                ChatColor.WHITE + "  n-m " + ChatColor.GRAY + "= Range from n to m",
                ChatColor.WHITE + "  n,m " + ChatColor.GRAY + "= Specific values n and m",
                "",
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

        // 統計情報
        long enabledCount = schedules.stream().filter(s -> s.enabled).count();
        inv.setItem(48, createItem(Material.COMPASS,
                ChatColor.AQUA + "📊 Statistics",
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                ChatColor.YELLOW + "Total Schedules: " + ChatColor.WHITE + schedules.size(),
                ChatColor.GREEN + "Active: " + ChatColor.WHITE + enabledCount,
                ChatColor.RED + "Inactive: " + ChatColor.WHITE + (schedules.size() - enabledCount),
                "",
                ChatColor.GRAY + "Next scheduled backups:",
                getNextScheduleInfo(schedules),
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

        // リフレッシュ
        inv.setItem(49, createItem(Material.COMPASS,
                ChatColor.YELLOW + "🔄 Refresh",
                ChatColor.GRAY + "Reload schedule list",
                "",
                ChatColor.YELLOW + "Click to refresh"));

        // 戻る
        inv.setItem(53, createItem(Material.ARROW,
                ChatColor.YELLOW + "⬅ Back",
                ChatColor.GRAY + "Return to main menu"));

        player.openInventory(inv);
    }

    /**
     * スケジュールの有効/無効を切り替え
     */
    public void toggleSchedule(Player player, int scheduleId) {
        player.sendMessage(ChatColor.YELLOW + "Toggling schedule...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.ScheduleToggleResult result = plugin.getAPIClient().toggleBackupSchedule(scheduleId);

                String status = result.enabled ? ChatColor.GREEN + "ENABLED ✓" : ChatColor.RED + "DISABLED ✗";
                player.sendMessage(ChatColor.GREEN + "✓ Schedule [ID:" + result.id + "] is now " + status);

                // 効果音
                if (result.enabled) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                } else {
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1.0f, 1.0f);
                }

                // GUIを再読み込み
                plugin.getServer().getScheduler().runTask(plugin, () -> open(player));

            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        });
    }

    /**
     * スケジュールを削除
     */
    public void deleteSchedule(Player player, int scheduleId) {
        player.sendMessage(ChatColor.RED + "⚠ Deleting schedule...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String result = plugin.getAPIClient().deleteBackupSchedule(scheduleId);

                player.sendMessage(ChatColor.GREEN + "✓ Schedule [ID:" + scheduleId + "] deleted successfully");
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);

                // GUIを再読み込み
                plugin.getServer().getScheduler().runTask(plugin, () -> open(player));

            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        });
    }

    /**
     * スロット番号からスケジュールIDを取得
     */
    public int getScheduleIdFromSlot(int slot) {
        return slotToScheduleId.getOrDefault(slot, -1);
    }

    // =============================
    // ヘルパーメソッド
    // =============================

    /**
     * Cron式の説明を取得
     */
    private String getCronDescription(String cron) {
        // 簡易的なCron式の説明生成
        String[] parts = cron.split(" ");
        if (parts.length != 5) return "Invalid cron format";

        String minute = parts[0];
        String hour = parts[1];
        String day = parts[2];
        String month = parts[3];
        String weekday = parts[4];

        StringBuilder desc = new StringBuilder();

        // 分
        if (minute.equals("0")) {
            desc.append("At the start of ");
        } else if (minute.equals("*")) {
            desc.append("Every minute ");
        } else if (minute.startsWith("*/")) {
            desc.append("Every ").append(minute.substring(2)).append(" minutes ");
        } else {
            desc.append("At minute ").append(minute).append(" ");
        }

        // 時
        if (hour.equals("*")) {
            desc.append("every hour");
        } else if (hour.startsWith("*/")) {
            desc.append("every ").append(hour.substring(2)).append(" hours");
        } else {
            desc.append("at ").append(hour).append(":00");
        }

        // 日
        if (!day.equals("*")) {
            desc.append(" on day ").append(day);
        }

        // 曜日
        if (!weekday.equals("*")) {
            String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            try {
                int dayNum = Integer.parseInt(weekday);
                desc.append(" on ").append(days[dayNum]);
            } catch (Exception ignored) {}
        }

        return desc.toString();
    }

    /**
     * 日時をフォーマット
     */
    private String formatDateTime(String isoDateTime) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoDateTime.replace(" ", "T"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            return dateTime.format(formatter);
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    /**
     * 次のスケジュール実行情報を取得
     */
    private String getNextScheduleInfo(List<APIClient.BackupSchedule> schedules) {
        StringBuilder info = new StringBuilder();
        int count = 0;

        for (APIClient.BackupSchedule schedule : schedules) {
            if (schedule.enabled && count < 3) {
                info.append(ChatColor.WHITE).append("  • ")
                        .append(schedule.name)
                        .append(ChatColor.GRAY).append(" (")
                        .append(schedule.cronExpression)
                        .append(")\n");
                count++;
            }
        }

        if (count == 0) {
            info.append(ChatColor.GRAY).append("  No active schedules");
        }

        return info.toString().trim();
    }

    /**
     * アイテムを作成
     */
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