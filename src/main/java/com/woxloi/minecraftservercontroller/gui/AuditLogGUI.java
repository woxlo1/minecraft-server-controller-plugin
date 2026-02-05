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
import java.util.List;

public class AuditLogGUI {
    
    private final MinecraftServerController plugin;
    
    public AuditLogGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        if (!player.hasPermission("msc.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }
        
        player.sendMessage(ChatColor.YELLOW + "Loading audit logs...");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<APIClient.AuditLog> logs = plugin.getAPIClient().getAuditLogs();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Audit Logs");
                    
                    // ログ一覧（最新30件）
                    int slot = 0;
                    int maxLogs = Math.min(45, logs.size());
                    
                    for (int i = 0; i < maxLogs; i++) {
                        APIClient.AuditLog log = logs.get(i);
                        
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
                    if (!logs.isEmpty()) {
                        long adminActions = logs.stream().filter(l -> "admin".equals(l.role)).count();
                        long playerActions = logs.stream().filter(l -> "player".equals(l.role)).count();
                        long rootActions = logs.stream().filter(l -> "root".equals(l.role)).count();
                        
                        inv.setItem(49, createItem(Material.BOOK,
                            ChatColor.YELLOW + "Statistics",
                            ChatColor.GRAY + "Total Logs: " + ChatColor.WHITE + logs.size(),
                            ChatColor.GRAY + "Root Actions: " + ChatColor.RED + rootActions,
                            ChatColor.GRAY + "Admin Actions: " + ChatColor.GOLD + adminActions,
                            ChatColor.GRAY + "Player Actions: " + ChatColor.GREEN + playerActions,
                            "",
                            ChatColor.YELLOW + "Showing latest " + maxLogs + " logs"));
                    }
                    
                    // リフレッシュ
                    inv.setItem(51, createItem(Material.COMPASS,
                        ChatColor.GREEN + "Refresh",
                        ChatColor.GRAY + "Reload audit logs"));
                    
                    // 戻る
                    inv.setItem(53, createItem(Material.ARROW,
                        ChatColor.YELLOW + "Back",
                        ChatColor.GRAY + "Return to main menu"));
                    
                    player.openInventory(inv);
                });
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Failed to load audit logs: " + e.getMessage());
                plugin.getLogger().warning("Audit log GUI error: " + e.getMessage());
            }
        });
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
