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

public class PluginGUI {
    
    private final MinecraftServerController plugin;
    
    public PluginGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Loading plugins...");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<APIClient.PluginInfo> plugins = plugin.getAPIClient().listPlugins();
                
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Plugin Management");
                    
                    // プラグインリスト
                    int slot = 0;
                    for (APIClient.PluginInfo p : plugins) {
                        if (slot >= 45) break;
                        
                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.GRAY + "File: " + ChatColor.WHITE + p.name);
                        lore.add(ChatColor.GRAY + "Size: " + ChatColor.WHITE + String.format("%.2f MB", p.sizeMb));
                        
                        inv.setItem(slot++, createItem(Material.PAPER,
                            ChatColor.GOLD + p.name,
                            lore.toArray(new String[0])));
                    }
                    
                    // プラグインリロード
                    inv.setItem(49, createItem(Material.REDSTONE,
                        ChatColor.RED + "Reload Plugins",
                        ChatColor.GRAY + "Reload all plugins",
                        ChatColor.RED + "WARNING: May cause issues",
                        ChatColor.YELLOW + "Click to reload"));
                    
                    // 戻る
                    inv.setItem(53, createItem(Material.ARROW,
                        ChatColor.YELLOW + "Back",
                        ChatColor.GRAY + "Return to main menu"));
                    
                    player.openInventory(inv);
                });
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Failed to load plugins: " + e.getMessage());
            }
        });
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
