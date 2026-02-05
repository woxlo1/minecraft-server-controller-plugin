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

public class PlayerManagementGUI {
    
    private final MinecraftServerController plugin;
    
    public PlayerManagementGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Player Management");
        
        // ホワイトリスト管理
        inv.setItem(10, createItem(Material.PAPER,
            ChatColor.WHITE + "Whitelist Management",
            ChatColor.GRAY + "Add, remove, list whitelist",
            ChatColor.YELLOW + "Use: /msc whitelist"));
        
        // OP管理
        if (player.hasPermission("msc.admin")) {
            inv.setItem(12, createItem(Material.NETHER_STAR,
                ChatColor.GOLD + "OP Management",
                ChatColor.GRAY + "Add or remove OP status",
                ChatColor.RED + "Admin only",
                ChatColor.YELLOW + "Use: /msc op"));
        }
        
        // オンラインプレイヤー
        inv.setItem(14, createItem(Material.EMERALD,
            ChatColor.GREEN + "Online Players",
            ChatColor.GRAY + "View online players",
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
