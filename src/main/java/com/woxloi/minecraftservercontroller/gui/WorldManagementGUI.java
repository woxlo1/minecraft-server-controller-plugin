package com.woxloi.minecraftservercontroller.gui;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import com.woxloi.minecraftservercontroller.utils.WorldManager;
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
 * ワールド管理GUI
 */
public class WorldManagementGUI {

    private final MinecraftServerController plugin;

    public WorldManagementGUI(MinecraftServerController plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        if (!player.hasPermission("msc.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission!");
            return;
        }

        player.sendMessage(ChatColor.YELLOW + "Loading worlds...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<WorldManager.WorldInfo> worlds = plugin.getWorldManager().getAvailableWorlds();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                displayWorlds(player, worlds);
            });
        });
    }

    private void displayWorlds(Player player, List<WorldManager.WorldInfo> worlds) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "World Management");

        // ワールド一覧（最大45個）
        int slot = 0;
        for (WorldManager.WorldInfo world : worlds) {
            if (slot >= 45) break;

            Material material = world.loaded ? Material.GRASS_BLOCK : Material.GRAY_WOOL;

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
            lore.add(ChatColor.YELLOW + "Status: " +
                    (world.loaded ? ChatColor.GREEN + "✓ Loaded" : ChatColor.RED + "✗ Unloaded"));
            lore.add(ChatColor.YELLOW + "Environment: " + ChatColor.WHITE + world.environment);
            lore.add(ChatColor.YELLOW + "Size: " + ChatColor.WHITE + world.sizeMB + " MB");
            lore.add("");
            lore.add(ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━");
            lore.add("");
            lore.add(ChatColor.GOLD + "⚡ Actions:");

            if (world.loaded) {
                lore.add(ChatColor.RED + "  LEFT-CLICK: " + ChatColor.WHITE + "Unload World");
                lore.add(ChatColor.YELLOW + "  RIGHT-CLICK: " + ChatColor.WHITE + "Teleport to Spawn");
            } else {
                lore.add(ChatColor.GREEN + "  LEFT-CLICK: " + ChatColor.WHITE + "Load World");
            }

            lore.add(ChatColor.AQUA + "  SHIFT-CLICK: " + ChatColor.WHITE + "Backup World");

            inv.setItem(slot++, createItem(material,
                    (world.loaded ? ChatColor.GREEN + "✓ " : ChatColor.GRAY + "○ ") +
                            ChatColor.AQUA + world.name,
                    lore.toArray(new String[0])));
        }

        // 空の場合
        if (worlds.isEmpty()) {
            inv.setItem(22, createItem(Material.BARRIER,
                    ChatColor.YELLOW + "No Worlds Found",
                    ChatColor.GRAY + "No worlds available"));
        }

        // 統計情報
        long loadedCount = worlds.stream().filter(w -> w.loaded).count();
        long totalSize = worlds.stream().mapToLong(w -> w.sizeMB).sum();

        inv.setItem(48, createItem(Material.BOOK,
                ChatColor.GOLD + "📊 World Statistics",
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                ChatColor.YELLOW + "Total Worlds: " + ChatColor.WHITE + worlds.size(),
                ChatColor.GREEN + "Loaded: " + ChatColor.WHITE + loadedCount,
                ChatColor.RED + "Unloaded: " + ChatColor.WHITE + (worlds.size() - loadedCount),
                ChatColor.YELLOW + "Total Size: " + ChatColor.WHITE + totalSize + " MB",
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━"));

        // リフレッシュ
        inv.setItem(49, createItem(Material.COMPASS,
                ChatColor.GREEN + "🔄 Refresh",
                ChatColor.GRAY + "Reload world list",
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