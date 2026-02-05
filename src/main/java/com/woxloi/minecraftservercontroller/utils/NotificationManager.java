package com.woxloi.minecraftservercontroller.utils;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class NotificationManager {
    
    private final MinecraftServerController plugin;
    
    public NotificationManager(MinecraftServerController plugin) {
        this.plugin = plugin;
    }
    
    // =============================
    // バックアップ通知
    // =============================
    
    public void notifyBackupCreated(String filename) {
        broadcastToAdmins(
            ChatColor.GREEN + "✓ Backup created: " + ChatColor.WHITE + filename,
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP
        );
    }
    
    public void notifyBackupRestored(String filename) {
        broadcastToAll(
            ChatColor.GOLD + "⚠ Server restored from backup: " + ChatColor.WHITE + filename,
            Sound.ENTITY_ENDER_DRAGON_GROWL
        );
    }
    
    public void notifyBackupDeleted(String filename) {
        broadcastToAdmins(
            ChatColor.RED + "✗ Backup deleted: " + ChatColor.WHITE + filename,
            Sound.ENTITY_ITEM_BREAK
        );
    }
    
    public void notifyScheduledBackup(String scheduleName) {
        broadcastToAdmins(
            ChatColor.AQUA + "⏰ Scheduled backup completed: " + ChatColor.WHITE + scheduleName,
            Sound.BLOCK_NOTE_BLOCK_PLING
        );
    }
    
    // =============================
    // サーバー制御通知
    // =============================
    
    public void notifyServerStarting() {
        broadcastToAll(
            ChatColor.GREEN + "⚡ Server is starting...",
            Sound.BLOCK_BEACON_ACTIVATE
        );
    }
    
    public void notifyServerStopping() {
        broadcastToAll(
            ChatColor.RED + "⚠ Server is stopping...",
            Sound.BLOCK_BEACON_DEACTIVATE
        );
    }
    
    public void notifyServerRestarting() {
        broadcastToAll(
            ChatColor.YELLOW + "🔄 Server is restarting...",
            Sound.ENTITY_ENDERMAN_TELEPORT
        );
    }
    
    // =============================
    // プラグイン通知
    // =============================
    
    public void notifyPluginUploaded(String pluginName) {
        broadcastToAdmins(
            ChatColor.GREEN + "✓ Plugin uploaded: " + ChatColor.WHITE + pluginName,
            Sound.ENTITY_PLAYER_LEVELUP
        );
    }
    
    public void notifyPluginDeleted(String pluginName) {
        broadcastToAdmins(
            ChatColor.RED + "✗ Plugin deleted: " + ChatColor.WHITE + pluginName,
            Sound.ENTITY_ITEM_BREAK
        );
    }
    
    public void notifyPluginReloaded() {
        broadcastToAdmins(
            ChatColor.AQUA + "🔄 Plugins reloaded",
            Sound.BLOCK_ENCHANTMENT_TABLE_USE
        );
    }
    
    // =============================
    // ホワイトリスト/OP通知
    // =============================
    
    public void notifyWhitelistAdded(String playerName) {
        broadcastToAdmins(
            ChatColor.GREEN + "✓ " + ChatColor.WHITE + playerName + 
            ChatColor.GREEN + " added to whitelist",
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP
        );
    }
    
    public void notifyWhitelistRemoved(String playerName) {
        broadcastToAdmins(
            ChatColor.RED + "✗ " + ChatColor.WHITE + playerName + 
            ChatColor.RED + " removed from whitelist",
            Sound.ENTITY_ITEM_BREAK
        );
    }
    
    public void notifyOpGranted(String playerName) {
        broadcastToAdmins(
            ChatColor.GOLD + "★ " + ChatColor.WHITE + playerName + 
            ChatColor.GOLD + " granted OP",
            Sound.ENTITY_PLAYER_LEVELUP
        );
    }
    
    public void notifyOpRevoked(String playerName) {
        broadcastToAdmins(
            ChatColor.GRAY + "○ " + ChatColor.WHITE + playerName + 
            ChatColor.GRAY + " OP revoked",
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP
        );
    }
    
    // =============================
    // エラー通知
    // =============================
    
    public void notifyError(String message) {
        broadcastToAdmins(
            ChatColor.DARK_RED + "⚠ ERROR: " + ChatColor.RED + message,
            Sound.ENTITY_ENDERMAN_SCREAM
        );
    }
    
    public void notifyWarning(String message) {
        broadcastToAdmins(
            ChatColor.GOLD + "⚠ WARNING: " + ChatColor.YELLOW + message,
            Sound.BLOCK_NOTE_BLOCK_BASS
        );
    }
    
    // =============================
    // メモリ警告
    // =============================
    
    public void notifyHighMemoryUsage(double percent) {
        broadcastToAdmins(
            ChatColor.RED + "⚠ High memory usage: " + ChatColor.WHITE + 
            String.format("%.1f%%", percent),
            Sound.BLOCK_ANVIL_LAND
        );
    }
    
    public void notifyCriticalMemoryUsage(double percent) {
        broadcastToAll(
            ChatColor.DARK_RED + "⚠⚠⚠ CRITICAL MEMORY USAGE: " + ChatColor.RED + 
            String.format("%.1f%%", percent),
            Sound.ENTITY_WITHER_SPAWN
        );
    }
    
    // =============================
    // ヘルパーメソッド
    // =============================
    
    private void broadcastToAll(String message, Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
        plugin.getLogger().info(ChatColor.stripColor(message));
    }
    
    private void broadcastToAdmins(String message, Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("msc.admin")) {
                player.sendMessage(message);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
        plugin.getLogger().info(ChatColor.stripColor(message));
    }
    
    public void sendNotification(Player player, String message, Sound sound) {
        player.sendMessage(message);
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }
    
    // =============================
    // タイトル通知（大きく表示）
    // =============================
    
    public void sendTitle(Player player, String title, String subtitle) {
        player.sendTitle(title, subtitle, 10, 70, 20);
    }
    
    public void broadcastTitle(String title, String subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(title, subtitle, 10, 70, 20);
        }
    }
}
