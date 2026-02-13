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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 監査ログGUI (v1.4.2: Pagination support)
 */
public class AuditLogGUI {

    private final MinecraftServerController plugin;
    private static final int LOGS_PER_PAGE = 45;
    private static final Map<UUID, Integer> playerPages = new HashMap<>();

    public AuditLogGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        if (!player.hasPermission("msc.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }

        playerPages.put(player.getUniqueId(), page);
        player.sendMessage(ChatColor.YELLOW + "Loading audit logs...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<APIClient.AuditLog> logs = plugin.getAPIClient().getAuditLogs();

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    displayLogs(player, logs, page);
                });

            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Failed to load audit logs: " + e.getMessage());
                plugin.getLogger().warning("Audit log GUI error: " + e.getMessage());
            }
        });
    }

    private void displayLogs(Player player, List<APIClient.AuditLog> allLogs, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Audit Logs");

        // ページネーション計算
        int totalLogs = allLogs.size();
        int totalPages = (int) Math.ceil((double) totalLogs / LOGS_PER_PAGE);
        int startIndex = page * LOGS_PER_PAGE;
        int endIndex = Math.min(startIndex + LOGS_PER_PAGE, totalLogs);

        // ログ一覧（45件）
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            APIClient.AuditLog log = allLogs.get(i);

            // アクションに応じてアイコンを変更
            Material material = getMaterialForAction(log.action);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Time: " + ChatColor.WHITE + log.time);
            lore.add(ChatColor.GRAY + "User: " + ChatColor.WHITE + log.apiKey);
            lore.add(ChatColor.GRAY + "Role: " + ChatColor.AQUA + log.role.toUpperCase());
            lore.add(ChatColor.GRAY + "Action: " + ChatColor.YELLOW + log.action);
            lore.add(ChatColor.GRAY + "Detail: " + ChatColor.WHITE + log.detail);
            lore.add(ChatColor.GRAY + "IP: " + ChatColor.WHITE + log.ip);

            inv.setItem(slot++, createItem(material,
                    getColorForRole(log.role) + log.action,
                    lore.toArray(new String[0])));
        }

        // 統計情報
        if (!allLogs.isEmpty()) {
            long adminActions = allLogs.stream().filter(l -> "admin".equals(l.role)).count();
            long playerActions = allLogs.stream().filter(l -> "player".equals(l.role)).count();
            long rootActions = allLogs.stream().filter(l -> "root".equals(l.role)).count();

            inv.setItem(46, createItem(Material.BOOK,
                    ChatColor.YELLOW + "Statistics",
                    ChatColor.GRAY + "Total Logs: " + ChatColor.WHITE + totalLogs,
                    ChatColor.GRAY + "Root Actions: " + ChatColor.RED + rootActions,
                    ChatColor.GRAY + "Admin Actions: " + ChatColor.GOLD + adminActions,
                    ChatColor.GRAY + "Player Actions: " + ChatColor.GREEN + playerActions,
                    "",
                    ChatColor.YELLOW + "Showing: " + (startIndex + 1) + "-" + endIndex + " of " + totalLogs,
                    ChatColor.YELLOW + "Page: " + (page + 1) + "/" + totalPages));
        }

        // ページネーションボタン
        if (page > 0) {
            inv.setItem(48, createItem(Material.ARROW,
                    ChatColor.GREEN + "◀ Previous Page",
                    ChatColor.GRAY + "Go to page " + page));
        }

        if (endIndex < totalLogs) {
            inv.setItem(50, createItem(Material.ARROW,
                    ChatColor.GREEN + "Next Page ▶",
                    ChatColor.GRAY + "Go to page " + (page + 2)));
        }

        // リフレッシュ
        inv.setItem(49, createItem(Material.COMPASS,
                ChatColor.GREEN + "Refresh",
                ChatColor.GRAY + "Reload audit logs",
                ChatColor.GRAY + "Current page: " + (page + 1)));

        // 戻る
        inv.setItem(53, createItem(Material.BARRIER,
                ChatColor.YELLOW + "Back",
                ChatColor.GRAY + "Return to main menu"));

        player.openInventory(inv);
    }

    /**
     * 次のページを開く
     */
    public void nextPage(Player player) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        open(player, currentPage + 1);
    }

    /**
     * 前のページを開く
     */
    public void previousPage(Player player) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 0);
        open(player, Math.max(0, currentPage - 1));
    }

    private Material getMaterialForAction(String action) {
        if (action.contains("backup")) return Material.CHEST;
        if (action.contains("start")) return Material.GREEN_WOOL;
        if (action.contains("stop")) return Material.RED_WOOL;
        if (action.contains("whitelist")) return Material.PAPER;
        if (action.contains("op")) return Material.NETHER_STAR;
        if (action.contains("plugin")) return Material.HOPPER;
        if (action.contains("exec")) return Material.COMMAND_BLOCK;
        if (action.contains("delete")) return Material.BARRIER;
        if (action.contains("create")) return Material.EMERALD;
        return Material.BOOK;
    }

    private ChatColor getColorForRole(String role) {
        switch (role.toLowerCase()) {
            case "root": return ChatColor.RED;
            case "admin": return ChatColor.GOLD;
            case "player": return ChatColor.GREEN;
            default: return ChatColor.GRAY;
        }
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