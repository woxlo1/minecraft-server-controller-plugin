package com.woxloi.minecraftservercontroller;

import org.bukkit.plugin.java.JavaPlugin;
import com.woxloi.minecraftservercontroller.commands.MSCCommand;
import com.woxloi.minecraftservercontroller.api.APIClient;
import com.woxloi.minecraftservercontroller.gui.GUIListener;

public class MinecraftServerController extends JavaPlugin {
    
    private static MinecraftServerController instance;
    private APIClient apiClient;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // コンフィグの保存
        saveDefaultConfig();
        
        // API クライアントの初期化
        String apiUrl = getConfig().getString("api.url", "http://localhost:8000");
        String apiKey = getConfig().getString("api.key", "");

        apiClient = new APIClient(apiUrl, apiKey, getLogger(), debug);

        if (apiKey.isEmpty()) {
            getLogger().warning("API Key is not set in config.yml!");
            getLogger().warning("Please set api.key in config.yml and reload the plugin.");
        }
        
        apiClient = new APIClient(apiUrl, apiKey);
        
        // コマンドの登録
        getCommand("msc").setExecutor(new MSCCommand(this));
        
        // GUIリスナーの登録
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        
        getLogger().info("MinecraftServerController has been enabled!");
        getLogger().info("API URL: " + apiUrl);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("MinecraftServerController has been disabled!");
    }
    
    public static MinecraftServerController getInstance() {
        return instance;
    }
    
    public APIClient getAPIClient() {
        return apiClient;
    }
}
