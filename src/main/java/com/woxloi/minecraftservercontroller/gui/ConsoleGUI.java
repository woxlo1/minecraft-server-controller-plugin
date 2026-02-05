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

import java.util.Arrays;

public class ConsoleGUI {
    
    private final MinecraftServerController plugin;
    
    public ConsoleGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    public void open(Player player) {
        if (!player.hasPermission("msc.exec")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + "Console Commands");
        
        // よく使うコマンドのショートカット
        inv.setItem(10, createItem(Material.PAPER,
            ChatColor.YELLOW + "say Hello",
            ChatColor.GRAY + "Send a message to all players",
            "",
            ChatColor.YELLOW + "Click to execute"));
        
        inv.setItem(11, createItem(Material.ENDER_PEARL,
            ChatColor.YELLOW + "tp @a ~ ~ ~",
            ChatColor.GRAY + "Teleport all players",
            "",
            ChatColor.YELLOW + "Click to execute"));
        
        inv.setItem(12, createItem(Material.CLOCK,
            ChatColor.YELLOW + "time set day",
            ChatColor.GRAY + "Set time to day",
            "",
            ChatColor.YELLOW + "Click to execute"));
        
        inv.setItem(13, createItem(Material.SUNFLOWER,
            ChatColor.YELLOW + "weather clear",
            ChatColor.GRAY + "Clear weather",
            "",
            ChatColor.YELLOW + "Click to execute"));
        
        inv.setItem(14, createItem(Material.EXPERIENCE_BOTTLE,
            ChatColor.YELLOW + "give @a minecraft:diamond 64",
            ChatColor.GRAY + "Give all players 64 diamonds",
            ChatColor.RED + "⚠ Cheating!",
            "",
            ChatColor.YELLOW + "Click to execute"));
        
        inv.setItem(15, createItem(Material.BARRIER,
            ChatColor.YELLOW + "difficulty peaceful",
            ChatColor.GRAY + "Set difficulty to peaceful",
            "",
            ChatColor.YELLOW + "Click to execute"));
        
        inv.setItem(16, createItem(Material.DIAMOND_SWORD,
            ChatColor.YELLOW + "difficulty hard",
            ChatColor.GRAY + "Set difficulty to hard",
            "",
            ChatColor.YELLOW + "Click to execute"));
        
        // カスタムコマンド入力（チャット入力を促す）
        inv.setItem(22, createItem(Material.WRITABLE_BOOK,
            ChatColor.GREEN + "Custom Command",
            ChatColor.GRAY + "Use: /msc exec <command>",
            "",
            ChatColor.YELLOW + "Close GUI and use command"));
        
        // 戻る
        inv.setItem(26, createItem(Material.ARROW,
            ChatColor.YELLOW + "Back",
            ChatColor.GRAY + "Return to main menu"));
        
        player.openInventory(inv);
    }
    
    public void executeCommand(Player player, String command) {
        player.sendMessage(ChatColor.YELLOW + "Executing: " + ChatColor.WHITE + command);
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                APIClient.CommandResult result = plugin.getAPIClient().executeCommand(command);
                
                player.sendMessage(ChatColor.GREEN + "✓ Command executed!");
                if (!result.output.isEmpty()) {
                    player.sendMessage(ChatColor.GRAY + "Output: " + ChatColor.WHITE + result.output);
                }
                
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "✗ Failed: " + e.getMessage());
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
