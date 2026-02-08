package com.woxloi.minecraftservercontroller.gui;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import com.woxloi.minecraftservercontroller.utils.ChatLogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * チャットログビューアGUI
 */
public class ChatLogViewerGUI {

    private final MinecraftServerController plugin;

    public ChatLogViewerGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        if (!player.hasPermission("msc.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Loading chat logs...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<ChatLogManager.ChatMessage> messages = plugin.getChatLogManager().getRecentMessages(30);
            ChatLogManager.ChatStats stats = plugin.getChatLogManager().getChatStats();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                displayLogs(player, messages, stats);
            });
        });
    }

    private void displayLogs(Player player, List<ChatLogManager.ChatMessage> messages,
                             ChatLogManager.ChatStats stats) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "Chat Log Viewer");

        // チャットメッセージ一覧（最大45件）
        int slot = 0;
        for (ChatLogManager.ChatMessage msg : messages) {
            if (slot >= 45) break;

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
            lore.add(ChatColor.YELLOW + "Time: " + ChatColor.WHITE + msg.getFormattedTimestamp());
            lore.add(ChatColor.YELLOW + "Player: " + ChatColor.WHITE + msg.playerName);
            lore.add(ChatColor.YELLOW + "World: " + ChatColor.WHITE + msg.world);
            lore.add("");
            lore.add(ChatColor.AQUA + "Message:");

            // メッセージを分割（長い場合）
            String message = msg.message;
            if (message.length() > 40) {
                lore.add(ChatColor.WHITE + message.substring(0, 40));
                lore.add(ChatColor.WHITE + message.substring(40));
            } else {
                lore.add(ChatColor.WHITE + message);
            }

            lore.add("");
            lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");

            inv.setItem(slot++, createItem(Material.PAPER,
                    ChatColor.GREEN + msg.playerName,
                    lore.toArray(new String[0])));
        }

        // メッセージがない場合
        if (messages.isEmpty()) {
            inv.setItem(22, createItem(Material.BARRIER,
                    ChatColor.YELLOW + "No Chat Messages",
                    ChatColor.GRAY + "No recent chat messages found"));
        }

        // 統計情報
        inv.setItem(46, createItem(Material.BOOK,
                ChatColor.GOLD + "📊 Chat Statistics",
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                ChatColor.YELLOW + "Total Messages: " + ChatColor.WHITE + stats.totalMessages,
                ChatColor.YELLOW + "Today: " + ChatColor.WHITE + stats.todayMessages,
                ChatColor.YELLOW + "Top Chatter (7d): " + ChatColor.WHITE + stats.topPlayer,
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

        // 検索（コマンド案内）
        inv.setItem(47, createItem(Material.COMPASS,
                ChatColor.AQUA + "🔍 Search",
                ChatColor.GRAY + "Search chat messages",
                "",
                ChatColor.YELLOW + "Use command:",
                ChatColor.WHITE + "/msc chat search <keyword>",
                "",
                ChatColor.GRAY + "Example:",
                ChatColor.WHITE + "/msc chat search hello"));

        // プレイヤー別フィルタ
        inv.setItem(48, createItem(Material.PLAYER_HEAD,
                ChatColor.YELLOW + "👤 Player Filter",
                ChatColor.GRAY + "Filter by player",
                "",
                ChatColor.YELLOW + "Use command:",
                ChatColor.WHITE + "/msc chat player <name>"));

        // リフレッシュ
        inv.setItem(49, createItem(Material.REDSTONE,
                ChatColor.GREEN + "🔄 Refresh",
                ChatColor.GRAY + "Reload chat logs",
                "",
                ChatColor.YELLOW + "Click to refresh"));

        // 戻る
        inv.setItem(53, createItem(Material.ARROW,
                ChatColor.YELLOW + "⬅ Back",
                ChatColor.GRAY + "Return to main menu"));

        player.openInventory(inv);
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