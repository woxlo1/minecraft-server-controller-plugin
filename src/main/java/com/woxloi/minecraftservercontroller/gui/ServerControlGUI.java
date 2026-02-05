package com.woxloi.minecraftservercontroller.gui;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ServerControlGUI {
    
    private final MinecraftServerController plugin;
    
    public ServerControlGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Server Control");
        
        // サーバー起動
        inv.setItem(11, createItem(Material.GREEN_WOOL,
            ChatColor.GREEN + "Start Server",
            ChatColor.GRAY + "Start the Minecraft server",
            ChatColor.RED + "Requires admin permission",
            "",
            ChatColor.YELLOW + "Click to start"));
        
        // サーバー停止
        inv.setItem(13, createItem(Material.RED_WOOL,
            ChatColor.RED + "Stop Server",
            ChatColor.GRAY + "Stop the Minecraft server",
            ChatColor.RED + "Requires admin permission",
            "",
            ChatColor.YELLOW + "Click to stop"));
        
        // サーバーステータス
        inv.setItem(15, createItem(Material.COMPASS,
            ChatColor.YELLOW + "Server Status",
            ChatColor.GRAY + "View current server status",
            "",
            ChatColor.YELLOW + "Click to view"));
        
        // 戻る
        inv.setItem(22, createItem(Material.ARROW,
            ChatColor.YELLOW + "Back",
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
