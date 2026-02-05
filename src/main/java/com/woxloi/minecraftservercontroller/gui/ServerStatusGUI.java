package com.woxloi.minecraftservercontroller.gui;

import com.google.gson.JsonObject;
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

public class ServerStatusGUI {
    
    private final MinecraftServerController plugin;
    
    public ServerStatusGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Loading server status...");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.ServerStatus status = plugin.getAPIClient().getServerStatus();
                APIClient.MetricsInfo metrics = plugin.getAPIClient().getMetrics();
                APIClient.PlayerList players = plugin.getAPIClient().getPlayers();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Server Status");
                    
                    // サーバーステータス
                    boolean isRunning = status.status.toLowerCase().contains("up");
                    Material statusMaterial = isRunning ? Material.GREEN_WOOL : Material.RED_WOOL;
                    
                    List<String> statusLore = new ArrayList<>();
                    statusLore.add(ChatColor.GRAY + "Status: " + 
                        (isRunning ? ChatColor.GREEN + "RUNNING ✓" : ChatColor.RED + "STOPPED ✗"));
                    
                    if (status.container != null) {
                        if (status.container.has("State")) {
                            statusLore.add(ChatColor.GRAY + "Container State: " + ChatColor.WHITE + 
                                status.container.get("State").getAsString());
                        }
                        if (status.container.has("Status")) {
                            statusLore.add(ChatColor.GRAY + "Container Status: " + ChatColor.WHITE + 
                                status.container.get("Status").getAsString());
                        }
                        if (status.container.has("Name")) {
                            statusLore.add(ChatColor.GRAY + "Container Name: " + ChatColor.WHITE + 
                                status.container.get("Name").getAsString());
                        }
                    }
                    
                    inv.setItem(10, createItem(statusMaterial,
                        ChatColor.YELLOW + "Server Status",
                        statusLore.toArray(new String[0])));
                    
                    // メモリ使用率
                    double memPercent = metrics.percent;
                    Material memMaterial = memPercent < 70 ? Material.LIME_WOOL :
                                          memPercent < 90 ? Material.YELLOW_WOOL : Material.RED_WOOL;
                    
                    List<String> memLore = new ArrayList<>();
                    memLore.add(ChatColor.GRAY + "Total: " + ChatColor.WHITE + 
                        String.format("%.2f GB", metrics.totalGb));
                    memLore.add(ChatColor.GRAY + "Used: " + ChatColor.WHITE + 
                        String.format("%.2f GB", metrics.usedGb));
                    memLore.add(ChatColor.GRAY + "Free: " + ChatColor.WHITE + 
                        String.format("%.2f GB", metrics.totalGb - metrics.usedGb));
                    memLore.add("");
                    memLore.add(ChatColor.GRAY + "Usage: " + getMemoryBar(memPercent));
                    memLore.add(ChatColor.WHITE + String.format("%.1f%%", memPercent));
                    
                    if (memPercent > 90) {
                        memLore.add("");
                        memLore.add(ChatColor.RED + "⚠ CRITICAL - Consider restart!");
                    } else if (memPercent > 70) {
                        memLore.add("");
                        memLore.add(ChatColor.YELLOW + "⚠ Warning - High usage");
                    }
                    
                    inv.setItem(12, createItem(memMaterial,
                        ChatColor.RED + "Memory Usage",
                        memLore.toArray(new String[0])));
                    
                    // プレイヤー情報
                    List<String> playerLore = new ArrayList<>();
                    playerLore.add(ChatColor.GRAY + "Online: " + ChatColor.WHITE + players.count);
                    playerLore.add(ChatColor.GRAY + "Max: " + ChatColor.WHITE + 
                        Bukkit.getMaxPlayers());
                    
                    if (!players.players.isEmpty()) {
                        playerLore.add("");
                        playerLore.add(ChatColor.YELLOW + "Players:");
                        for (String p : players.players) {
                            playerLore.add(ChatColor.WHITE + "  • " + p);
                        }
                    }
                    
                    inv.setItem(14, createItem(Material.PLAYER_HEAD,
                        ChatColor.GREEN + "Online Players",
                        playerLore.toArray(new String[0])));
                    
                    // サーバーバージョン
                    String mcVersion = Bukkit.getVersion();
                    String bukkitVersion = Bukkit.getBukkitVersion();
                    
                    inv.setItem(16, createItem(Material.BOOKSHELF,
                        ChatColor.AQUA + "Server Version",
                        ChatColor.GRAY + "Minecraft: " + ChatColor.WHITE + bukkitVersion,
                        ChatColor.GRAY + "Server: " + ChatColor.WHITE + mcVersion));
                    
                    // リフレッシュボタン
                    inv.setItem(22, createItem(Material.COMPASS,
                        ChatColor.GREEN + "Refresh",
                        ChatColor.GRAY + "Reload server status",
                        "",
                        ChatColor.YELLOW + "Click to refresh"));
                    
                    // 戻る
                    inv.setItem(26, createItem(Material.ARROW,
                        ChatColor.YELLOW + "Back",
                        ChatColor.GRAY + "Return to main menu"));
                    
                    player.openInventory(inv);
                });
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Failed to load server status: " + e.getMessage());
                plugin.getLogger().warning("Server status GUI error: " + e.getMessage());
            }
        });
    }
    
    private String getMemoryBar(double percent) {
        int bars = (int) (percent / 10);
        StringBuilder bar = new StringBuilder();
        
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                if (percent > 90) {
                    bar.append(ChatColor.RED).append("█");
                } else if (percent > 70) {
                    bar.append(ChatColor.YELLOW).append("█");
                } else {
                    bar.append(ChatColor.GREEN).append("█");
                }
            } else {
                bar.append(ChatColor.GRAY).append("█");
            }
        }
        
        return bar.toString();
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
