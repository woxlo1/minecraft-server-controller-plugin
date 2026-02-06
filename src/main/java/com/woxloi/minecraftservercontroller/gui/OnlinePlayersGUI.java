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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * オンラインプレイヤーの詳細情報を表示するGUI
 */
public class OnlinePlayersGUI {

    private final MinecraftServerController plugin;

    public OnlinePlayersGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    /**
     * オンラインプレイヤー一覧を表示
     */
    public void open(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Loading player information...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.PlayerList apiPlayers = plugin.getAPIClient().getPlayers();

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    displayPlayers(player, apiPlayers);
                });

            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Failed to load players: " + e.getMessage());
                plugin.getLogger().warning("Online players GUI error: " + e.getMessage());
            }
        });
    }

    /**
     * プレイヤー一覧を表示
     */
    private void displayPlayers(Player viewer, APIClient.PlayerList apiPlayers) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GREEN + "Online Players");

        // サーバー情報（上部）
        inv.setItem(4, createItem(Material.EMERALD,
                ChatColor.GOLD + "Server Information",
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                ChatColor.YELLOW + "Online Players: " + ChatColor.WHITE + apiPlayers.count +
                        ChatColor.GRAY + " / " + ChatColor.WHITE + Bukkit.getMaxPlayers(),
                ChatColor.YELLOW + "Max Players: " + ChatColor.WHITE + Bukkit.getMaxPlayers(),
                ChatColor.YELLOW + "Server TPS: " + ChatColor.WHITE + getServerTPS(),
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

        // オンラインプレイヤー一覧（最大45人）
        int slot = 9; // 2行目から開始
        for (String playerName : apiPlayers.players) {
            if (slot >= 45) break;

            Player onlinePlayer = Bukkit.getPlayer(playerName);

            if (onlinePlayer != null) {
                // 詳細情報を取得
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
                lore.add(ChatColor.AQUA + "Player: " + ChatColor.WHITE + onlinePlayer.getName());
                lore.add("");

                // 基本情報
                lore.add(ChatColor.YELLOW + "👤 Basic Info:");
                lore.add(ChatColor.GRAY + "  Display Name: " + ChatColor.WHITE + onlinePlayer.getDisplayName());
                lore.add(ChatColor.GRAY + "  UUID: " + ChatColor.DARK_GRAY + onlinePlayer.getUniqueId().toString().substring(0, 8) + "...");
                lore.add("");

                // ゲーム情報
                lore.add(ChatColor.YELLOW + "🎮 Game Info:");
                lore.add(ChatColor.GRAY + "  Gamemode: " + ChatColor.WHITE + onlinePlayer.getGameMode().name());
                lore.add(ChatColor.GRAY + "  Level: " + ChatColor.WHITE + onlinePlayer.getLevel());
                lore.add(ChatColor.GRAY + "  XP: " + ChatColor.WHITE + String.format("%.1f%%", onlinePlayer.getExp() * 100));
                lore.add("");

                // 健康状態
                double health = onlinePlayer.getHealth();
                double maxHealth = onlinePlayer.getMaxHealth();
                double healthPercent = (health / maxHealth) * 100;
                String healthColor = healthPercent > 75 ? ChatColor.GREEN.toString() :
                        healthPercent > 50 ? ChatColor.YELLOW.toString() :
                                healthPercent > 25 ? ChatColor.GOLD.toString() : ChatColor.RED.toString();

                lore.add(ChatColor.YELLOW + "❤ Health:");
                lore.add(ChatColor.GRAY + "  HP: " + healthColor + String.format("%.1f", health) +
                        ChatColor.GRAY + " / " + ChatColor.WHITE + String.format("%.1f", maxHealth));
                lore.add(ChatColor.GRAY + "  " + getHealthBar(healthPercent));
                lore.add(ChatColor.GRAY + "  Food: " + ChatColor.WHITE + onlinePlayer.getFoodLevel() + "/20");
                lore.add("");

                // 位置情報
                lore.add(ChatColor.YELLOW + "📍 Location:");
                lore.add(ChatColor.GRAY + "  World: " + ChatColor.WHITE + onlinePlayer.getWorld().getName());
                lore.add(ChatColor.GRAY + "  X: " + ChatColor.WHITE + String.format("%.1f", onlinePlayer.getLocation().getX()));
                lore.add(ChatColor.GRAY + "  Y: " + ChatColor.WHITE + String.format("%.1f", onlinePlayer.getLocation().getY()));
                lore.add(ChatColor.GRAY + "  Z: " + ChatColor.WHITE + String.format("%.1f", onlinePlayer.getLocation().getZ()));
                lore.add("");

                // 権限情報
                lore.add(ChatColor.YELLOW + "🔐 Permissions:");
                lore.add(ChatColor.GRAY + "  OP: " + (onlinePlayer.isOp() ? ChatColor.GREEN + "✓ Yes" : ChatColor.RED + "✗ No"));
                lore.add(ChatColor.GRAY + "  Flying: " + (onlinePlayer.isFlying() ? ChatColor.GREEN + "✓ Yes" : ChatColor.RED + "✗ No"));
                lore.add("");

                // 接続情報
                lore.add(ChatColor.YELLOW + "🌐 Connection:");
                lore.add(ChatColor.GRAY + "  Ping: " + ChatColor.WHITE + getPingColor(onlinePlayer) + onlinePlayer.getPing() + "ms");
                lore.add(ChatColor.GRAY + "  Client: " + ChatColor.WHITE + getClientVersion(onlinePlayer));
                lore.add("");

                lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
                lore.add("");
                lore.add(ChatColor.GOLD + "⚡ Actions:");
                lore.add(ChatColor.YELLOW + "  LEFT-CLICK: " + ChatColor.WHITE + "View Inventory");
                lore.add(ChatColor.AQUA + "  RIGHT-CLICK: " + ChatColor.WHITE + "Teleport to Player");
                lore.add(ChatColor.RED + "  SHIFT-CLICK: " + ChatColor.WHITE + "Management Options");

                // プレイヤーの頭を使用
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                if (skullMeta != null) {
                    skullMeta.setOwningPlayer(onlinePlayer);
                    skullMeta.setDisplayName(ChatColor.GREEN + onlinePlayer.getName());
                    skullMeta.setLore(lore);
                    skull.setItemMeta(skullMeta);
                }

                inv.setItem(slot++, skull);
            } else {
                // オフラインまたは情報取得失敗
                inv.setItem(slot++, createItem(Material.SKELETON_SKULL,
                        ChatColor.GRAY + playerName,
                        ChatColor.RED + "Player not found in server",
                        ChatColor.GRAY + "May be in different dimension"));
            }
        }

        // プレイヤーがいない場合
        if (apiPlayers.count == 0) {
            inv.setItem(22, createItem(Material.BARRIER,
                    ChatColor.YELLOW + "No Players Online",
                    ChatColor.GRAY + "The server is currently empty"));
        }

        // 統計情報
        inv.setItem(49, createItem(Material.BOOK,
                ChatColor.AQUA + "📊 Server Statistics",
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                ChatColor.YELLOW + "Total Players: " + ChatColor.WHITE + apiPlayers.count,
                ChatColor.YELLOW + "Max Players: " + ChatColor.WHITE + Bukkit.getMaxPlayers(),
                ChatColor.YELLOW + "Average Ping: " + ChatColor.WHITE + getAveragePing() + "ms",
                ChatColor.YELLOW + "Server Uptime: " + ChatColor.WHITE + getUptime(),
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

        // リフレッシュ
        inv.setItem(51, createItem(Material.COMPASS,
                ChatColor.YELLOW + "🔄 Refresh",
                ChatColor.GRAY + "Reload player list",
                "",
                ChatColor.YELLOW + "Click to refresh"));

        // 戻る
        inv.setItem(53, createItem(Material.ARROW,
                ChatColor.YELLOW + "⬅ Back",
                ChatColor.GRAY + "Return to main menu"));

        viewer.openInventory(inv);
    }

    /**
     * プレイヤー管理オプションGUIを開く
     */
    public void openManagementOptions(Player viewer, Player target) {
        if (!viewer.hasPermission("msc.admin")) {
            viewer.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "Manage: " + target.getName());

        // ホワイトリスト
        inv.setItem(10, createItem(Material.PAPER,
                ChatColor.WHITE + "Whitelist Management",
                ChatColor.GRAY + "Add or remove from whitelist",
                "",
                ChatColor.YELLOW + "Click to manage"));

        // OP権限
        inv.setItem(12, createItem(Material.NETHER_STAR,
                ChatColor.GOLD + (target.isOp() ? "Remove OP" : "Grant OP"),
                ChatColor.GRAY + "Operator permissions",
                ChatColor.GRAY + "Current: " + (target.isOp() ? ChatColor.GREEN + "OP" : ChatColor.RED + "Not OP"),
                "",
                ChatColor.YELLOW + "Click to toggle"));

        // キック
        inv.setItem(14, createItem(Material.IRON_DOOR,
                ChatColor.RED + "Kick Player",
                ChatColor.GRAY + "Remove from server",
                "",
                ChatColor.YELLOW + "Click to kick"));

        // BAN
        inv.setItem(16, createItem(Material.BARRIER,
                ChatColor.DARK_RED + "Ban Player",
                ChatColor.GRAY + "Permanently ban from server",
                ChatColor.RED + "⚠ Use with caution!",
                "",
                ChatColor.YELLOW + "Click to ban"));

        // 戻る
        inv.setItem(22, createItem(Material.ARROW,
                ChatColor.YELLOW + "⬅ Back",
                ChatColor.GRAY + "Return to player list"));

        viewer.openInventory(inv);
    }

    // =============================
    // ヘルパーメソッド
    // =============================

    /**
     * サーバーTPSを取得（近似値）
     */
    private String getServerTPS() {
        try {
            // Paper/Spigot の場合
            double tps = Bukkit.getTPS()[0]; // 1分平均
            String color;
            if (tps >= 19.5) {
                color = ChatColor.GREEN.toString();
            } else if (tps >= 18) {
                color = ChatColor.YELLOW.toString();
            } else {
                color = ChatColor.RED.toString();
            }
            return color + String.format("%.2f", tps) + ChatColor.GRAY + " / 20.0";
        } catch (Exception e) {
            return ChatColor.GRAY + "N/A";
        }
    }

    /**
     * 平均Pingを取得
     */
    private String getAveragePing() {
        int total = 0;
        int count = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            total += p.getPing();
            count++;
        }

        return count > 0 ? String.valueOf(total / count) : "0";
    }

    /**
     * サーバー稼働時間を取得
     */
    private String getUptime() {
        long uptimeMillis = plugin.getUptimeMillis();

        long hours = uptimeMillis / (1000 * 60 * 60);
        long minutes = (uptimeMillis / (1000 * 60)) % 60;

        return String.format("%dh %dm", hours, minutes);
    }

    /**
     * Pingの色を取得
     */
    private ChatColor getPingColor(Player player) {
        int ping = player.getPing();
        if (ping < 50) return ChatColor.GREEN;
        if (ping < 100) return ChatColor.YELLOW;
        if (ping < 200) return ChatColor.GOLD;
        return ChatColor.RED;
    }

    /**
     * クライアントバージョンを取得
     */
    private String getClientVersion(Player player) {
        try {
            // プロトコルバージョンから推測
            return "1.20.x"; // 実際にはプロトコルバージョンから判定
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * ヘルスバーを生成
     */
    private String getHealthBar(double percent) {
        int bars = (int) (percent / 10);
        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                if (percent > 75) {
                    bar.append(ChatColor.GREEN).append("█");
                } else if (percent > 50) {
                    bar.append(ChatColor.YELLOW).append("█");
                } else if (percent > 25) {
                    bar.append(ChatColor.GOLD).append("█");
                } else {
                    bar.append(ChatColor.RED).append("█");
                }
            } else {
                bar.append(ChatColor.DARK_GRAY).append("█");
            }
        }

        return bar.toString();
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