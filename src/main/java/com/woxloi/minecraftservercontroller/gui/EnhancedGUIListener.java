package com.woxloi.minecraftservercontroller.gui;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * 拡張GUIリスナー
 * バックアップスケジュールGUIとオンラインプレイヤーGUIのイベント処理
 */
public class EnhancedGUIListener implements Listener {

    private final MinecraftServerController plugin;

    public EnhancedGUIListener(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // 対象のGUIかチェック
        if (!title.contains("Backup Schedules") &&
                !title.contains("Online Players") &&
                !title.contains("Manage:")) {
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
        ClickType clickType = event.getClick();
        int slot = event.getSlot();

        // バックアップスケジュールGUI
        if (title.contains("Backup Schedules")) {
            handleBackupSchedulesClick(player, itemName, clickType, slot, clicked);
        }
        // オンラインプレイヤーGUI
        else if (title.contains("Online Players")) {
            handleOnlinePlayersClick(player, itemName, clickType, slot, clicked);
        }
        // プレイヤー管理GUI
        else if (title.contains("Manage:")) {
            handlePlayerManagementClick(player, title, itemName, clickType);
        }
    }

    /**
     * バックアップスケジュールGUIのクリック処理
     */
    private void handleBackupSchedulesClick(Player player, String itemName, ClickType clickType, int slot, ItemStack clicked) {
    BackupScheduleGUI gui = new BackupScheduleGUI(plugin);

        // 制御ボタン
        if (itemName.contains("Back") || itemName.contains("⬅")) {
            new MainMenuGUI(plugin).open(player);
            return;
        }

        if (itemName.contains("Refresh") || itemName.contains("🔄")) {
            gui.open(player);
            return;
        }

        if (itemName.contains("Create New Schedule") || itemName.contains("➕")) {
            player.closeInventory();
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage(ChatColor.GREEN + "Create New Backup Schedule");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Command Format:");
            player.sendMessage(ChatColor.WHITE + "/msc schedule create <name> \"<cron>\" <max_backups>");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Example:");
            player.sendMessage(ChatColor.WHITE + "/msc schedule create daily \"0 2 * * *\" 7");
            player.sendMessage(ChatColor.GRAY + "  → Creates a daily backup at 2 AM, keeping 7 generations");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "Common Patterns:");
            player.sendMessage(ChatColor.AQUA + "  Hourly: " + ChatColor.WHITE + "\"0 * * * *\"");
            player.sendMessage(ChatColor.AQUA + "  Every 6h: " + ChatColor.WHITE + "\"0 */6 * * *\"");
            player.sendMessage(ChatColor.AQUA + "  Daily 2AM: " + ChatColor.WHITE + "\"0 2 * * *\"");
            player.sendMessage(ChatColor.AQUA + "  Weekly Sunday: " + ChatColor.WHITE + "\"0 0 * * 0\"");
            player.sendMessage(ChatColor.AQUA + "  Monthly 1st: " + ChatColor.WHITE + "\"0 0 1 * *\"");
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            return;
        }

        if (itemName.contains("Preset Examples") || itemName.contains("Cron Format Help") ||
                itemName.contains("Statistics")) {
            // 情報表示のみ
            return;
        }

        // スケジュールアイテム（0-44スロット）
        if (slot >= 0 && slot < 45) {
            int scheduleId = gui.getScheduleIdFromSlot(slot);

            if (scheduleId == -1) {
                player.sendMessage(ChatColor.RED + "Invalid schedule");
                return;
            }

            if (clickType.isLeftClick() && !clickType.isShiftClick()) {
                // 左クリック: トグル
                player.closeInventory();
                gui.toggleSchedule(player, scheduleId);

            } else if (clickType.isRightClick()) {
                // 右クリック: 削除確認
                player.closeInventory();
                player.sendMessage("");
                player.sendMessage(ChatColor.RED + "⚠⚠⚠ DELETE SCHEDULE ⚠⚠⚠");
                player.sendMessage(ChatColor.YELLOW + "Are you sure you want to delete this schedule?");
                player.sendMessage(ChatColor.GRAY + "Schedule: " + ChatColor.WHITE + itemName);
                player.sendMessage("");
                player.sendMessage(ChatColor.RED + "To confirm, use:");
                player.sendMessage(ChatColor.WHITE + "/msc schedule delete " + scheduleId);
                player.sendMessage("");

            } else if (clickType.isShiftClick()) {

                ItemMeta meta = clicked.getItemMeta();

                // Shiftクリック: 詳細表示
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                player.sendMessage(ChatColor.AQUA + "Schedule Details: " + ChatColor.WHITE + itemName);
                player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

                if (meta.hasLore()) {
                    for (String line : meta.getLore()) {
                        player.sendMessage(line);
                    }
                }

                player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
        }
    }

    /**
     * オンラインプレイヤーGUIのクリック処理
     */
    private void handleOnlinePlayersClick(Player player, String itemName, ClickType clickType,
                                          int slot, ItemStack clicked) {
        OnlinePlayersGUI gui = new OnlinePlayersGUI(plugin);

        // 制御ボタン
        if (itemName.contains("Back") || itemName.contains("⬅")) {
            new MainMenuGUI(plugin).open(player);
            return;
        }

        if (itemName.contains("Refresh") || itemName.contains("🔄")) {
            gui.open(player);
            return;
        }

        if (itemName.contains("Server Information") || itemName.contains("Server Statistics")) {
            // 情報表示のみ
            return;
        }

        // プレイヤースカル（9-44スロット）
        if (slot >= 9 && slot < 45 && clicked.getType() == Material.PLAYER_HEAD) {
            SkullMeta skullMeta = (SkullMeta) clicked.getItemMeta();
            if (skullMeta == null || skullMeta.getOwningPlayer() == null) {
                return;
            }

            Player target = plugin.getServer().getPlayer(skullMeta.getOwningPlayer().getUniqueId());
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }

            if (clickType.isLeftClick() && !clickType.isShiftClick()) {
                // 左クリック: インベントリを表示
                player.closeInventory();
                if (player.hasPermission("msc.admin")) {
                    player.openInventory(target.getInventory());
                    player.sendMessage(ChatColor.GREEN + "Viewing " + target.getName() + "'s inventory");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to view inventories!");
                }

            } else if (clickType.isRightClick() && !clickType.isShiftClick()) {
                // 右クリック: テレポート
                player.closeInventory();
                if (player.hasPermission("msc.admin")) {
                    player.teleport(target);
                    player.sendMessage(ChatColor.GREEN + "✓ Teleported to " + target.getName());
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to teleport!");
                }

            } else if (clickType.isShiftClick()) {
                // Shiftクリック: 管理オプション
                player.closeInventory();
                gui.openManagementOptions(player, target);
            }
        }
    }

    /**
     * プレイヤー管理GUIのクリック処理
     */
    private void handlePlayerManagementClick(Player player, String title, String itemName, ClickType clickType) {
        // タイトルからプレイヤー名を取得
        String targetName = title.replace("Manage:", "").trim();
        Player target = plugin.getServer().getPlayer(targetName);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            player.closeInventory();
            return;
        }

        if (!player.hasPermission("msc.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            player.closeInventory();
            return;
        }

        if (itemName.contains("Back") || itemName.contains("⬅")) {
            new OnlinePlayersGUI(plugin).open(player);
            return;
        }

        player.closeInventory();

        if (itemName.contains("Whitelist Management")) {
            player.sendMessage(ChatColor.YELLOW + "Use commands:");
            player.sendMessage(ChatColor.WHITE + "/msc whitelist add " + target.getName());
            player.sendMessage(ChatColor.WHITE + "/msc whitelist remove " + target.getName());

        } else if (itemName.contains("OP")) {
            if (target.isOp()) {
                player.performCommand("msc op remove " + target.getName());
            } else {
                player.performCommand("msc op add " + target.getName());
            }

        } else if (itemName.contains("Kick")) {
            player.sendMessage(ChatColor.YELLOW + "Use command:");
            player.sendMessage(ChatColor.WHITE + "/kick " + target.getName() + " <reason>");

        } else if (itemName.contains("Ban")) {
            player.sendMessage(ChatColor.RED + "⚠ BAN PLAYER");
            player.sendMessage(ChatColor.YELLOW + "Use command:");
            player.sendMessage(ChatColor.WHITE + "/ban " + target.getName() + " <reason>");
        }
    }
}