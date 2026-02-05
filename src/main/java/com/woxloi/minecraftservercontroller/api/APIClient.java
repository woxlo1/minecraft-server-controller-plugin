package com.woxloi.minecraftservercontroller.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class APIClient {
    
    private final String baseUrl;
    private final String apiKey;
    private final Gson gson;
    
    public APIClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.gson = new Gson();
    }
    
    /**
     * GET リクエスト
     */
    private String get(String endpoint) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        
        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200) {
            return readResponse(conn.getInputStream());
        } else {
            throw new IOException("HTTP Error: " + responseCode + " - " + readResponse(conn.getErrorStream()));
        }
    }
    
    /**
     * POST リクエスト
     */
    private String post(String endpoint, String jsonBody) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        if (jsonBody != null && !jsonBody.isEmpty()) {
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        
        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200) {
            return readResponse(conn.getInputStream());
        } else {
            throw new IOException("HTTP Error: " + responseCode + " - " + readResponse(conn.getErrorStream()));
        }
    }
    
    /**
     * POST リクエスト (form-data)
     */
    private String postForm(String endpoint, String key, String value) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setDoOutput(true);
        
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        
        try (OutputStream os = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {
            
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"\r\n");
            writer.append("\r\n");
            writer.append(value).append("\r\n");
            writer.append("--").append(boundary).append("--\r\n");
            writer.flush();
        }
        
        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200) {
            return readResponse(conn.getInputStream());
        } else {
            throw new IOException("HTTP Error: " + responseCode + " - " + readResponse(conn.getErrorStream()));
        }
    }
    
    /**
     * レスポンスを読み取る
     */
    private String readResponse(InputStream is) throws IOException {
        if (is == null) return "";
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    // =============================
    // API メソッド
    // =============================
    
    /**
     * サーバーステータス取得
     */
    public ServerStatus getServerStatus() throws IOException {
        String response = get("/server/status");
        JsonObject json = gson.fromJson(response, JsonObject.class);
        
        String status = json.get("status").getAsString();
        JsonObject container = json.has("container") ? json.getAsJsonObject("container") : null;
        
        return new ServerStatus(status, container);
    }
    
    /**
     * バックアップ作成
     */
    public BackupResult createBackup() throws IOException {
        String response = post("/backups", null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        
        String filename = json.get("backup").getAsString();
        return new BackupResult(filename);
    }
    
    /**
     * バックアップ一覧取得
     */
    public List<BackupInfo> listBackups() throws IOException {
        String response = get("/backups");
        JsonObject json = gson.fromJson(response, JsonObject.class);
        JsonArray backups = json.getAsJsonArray("backups");
        
        List<BackupInfo> result = new ArrayList<>();
        for (JsonElement elem : backups) {
            JsonObject backup = elem.getAsJsonObject();
            String filename = backup.get("filename").getAsString();
            double sizeMb = backup.get("size_mb").getAsDouble();
            String modified = backup.get("modified").getAsString();
            
            result.add(new BackupInfo(filename, sizeMb, modified));
        }
        
        return result;
    }
    
    /**
     * オンラインプレイヤー一覧取得
     */
    public PlayerList getPlayers() throws IOException {
        String response = get("/players");
        JsonObject json = gson.fromJson(response, JsonObject.class);
        
        int count = json.get("count").getAsInt();
        JsonArray playersArray = json.getAsJsonArray("players");
        
        List<String> players = new ArrayList<>();
        for (JsonElement elem : playersArray) {
            players.add(elem.getAsString());
        }
        
        return new PlayerList(count, players);
    }
    
    /**
     * コンソールコマンド実行
     */
    public CommandResult executeCommand(String command) throws IOException {
        String response = postForm("/exec", "command", command);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        
        String time = json.get("time").getAsString();
        String cmd = json.get("command").getAsString();
        String output = json.get("output").getAsString();
        
        return new CommandResult(time, cmd, output);
    }
    
    /**
     * メトリクス取得
     */
    public MetricsInfo getMetrics() throws IOException {
        String response = get("/metrics");
        JsonObject json = gson.fromJson(response, JsonObject.class);
        JsonObject memory = json.getAsJsonObject("memory");
        
        double totalGb = memory.get("total_gb").getAsDouble();
        double usedGb = memory.get("used_gb").getAsDouble();
        double percent = memory.get("percent").getAsDouble();
        
        return new MetricsInfo(totalGb, usedGb, percent);
    }
    
    /**
     * サーバー起動
     */
    public String startServer() throws IOException {
        String response = post("/start", null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("status").getAsString();
    }
    
    /**
     * サーバー停止
     */
    public String stopServer() throws IOException {
        String response = post("/stop", null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("status").getAsString();
    }
    
    /**
     * ホワイトリストにプレイヤーを追加
     */
    public String whitelistAdd(String player) throws IOException {
        String response = post("/whitelist/add/" + player, null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("output").getAsString();
    }
    
    /**
     * ホワイトリストからプレイヤーを削除
     */
    public String whitelistRemove(String player) throws IOException {
        String response = post("/whitelist/remove/" + player, null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("output").getAsString();
    }
    
    /**
     * ホワイトリスト一覧取得
     */
    public List<String> getWhitelist() throws IOException {
        String response = get("/whitelist");
        JsonObject json = gson.fromJson(response, JsonObject.class);
        JsonArray playersArray = json.getAsJsonArray("players");
        
        List<String> players = new ArrayList<>();
        for (JsonElement elem : playersArray) {
            players.add(elem.getAsString());
        }
        
        return players;
    }
    
    /**
     * ホワイトリスト有効化
     */
    public String whitelistEnable() throws IOException {
        String response = post("/whitelist/enable", null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("output").getAsString();
    }
    
    /**
     * ホワイトリスト無効化
     */
    public String whitelistDisable() throws IOException {
        String response = post("/whitelist/disable", null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("output").getAsString();
    }
    
    /**
     * OP権限付与
     */
    public String opAdd(String player) throws IOException {
        String response = post("/op/add/" + player, null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("output").getAsString();
    }
    
    /**
     * OP権限削除
     */
    public String opRemove(String player) throws IOException {
        String response = post("/op/remove/" + player, null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("output").getAsString();
    }
    
    /**
     * プラグイン一覧取得
     */
    public List<PluginInfo> listPlugins() throws IOException {
        String response = get("/plugins");
        JsonObject json = gson.fromJson(response, JsonObject.class);
        JsonArray pluginsArray = json.getAsJsonArray("plugins");
        
        List<PluginInfo> plugins = new ArrayList<>();
        for (JsonElement elem : pluginsArray) {
            JsonObject plugin = elem.getAsJsonObject();
            String name = plugin.get("name").getAsString();
            double sizeMb = plugin.get("size_mb").getAsDouble();
            
            plugins.add(new PluginInfo(name, sizeMb));
        }
        
        return plugins;
    }
    
    /**
     * プラグインリロード
     */
    public String reloadPlugins() throws IOException {
        String response = post("/plugins/reload", null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("output").getAsString();
    }
    
    /**
     * バックアップをリストアする
     */
    public RestoreResult restoreBackup(String filename) throws IOException {
        String response = post("/backups/restore/" + filename, null);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        
        String status = json.get("status").getAsString();
        String backup = json.get("backup").getAsString();
        String preRestoreBackup = json.get("pre_restore_backup").getAsString();
        
        return new RestoreResult(status, backup, preRestoreBackup);
    }
    
    /**
     * バックアップを削除
     */
    public String deleteBackup(String filename) throws IOException {
        URL url = new URL(baseUrl + "/backups/" + filename);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        
        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200) {
            String response = readResponse(conn.getInputStream());
            JsonObject json = gson.fromJson(response, JsonObject.class);
            return json.get("status").getAsString();
        } else {
            throw new IOException("HTTP Error: " + responseCode);
        }
    }
    
    /**
     * バックアップスケジュール一覧取得
     */
    public List<BackupSchedule> listBackupSchedules() throws IOException {
        String response = get("/backup/schedules");
        JsonArray schedules = gson.fromJson(response, JsonArray.class);
        
        List<BackupSchedule> result = new ArrayList<>();
        for (JsonElement elem : schedules) {
            JsonObject schedule = elem.getAsJsonObject();
            int id = schedule.get("id").getAsInt();
            String name = schedule.get("name").getAsString();
            String cronExpression = schedule.get("cron_expression").getAsString();
            boolean enabled = schedule.get("enabled").getAsBoolean();
            int maxBackups = schedule.get("max_backups").getAsInt();
            String created = schedule.get("created").getAsString();
            String lastRun = schedule.has("last_run") && !schedule.get("last_run").isJsonNull() 
                ? schedule.get("last_run").getAsString() : null;
            
            result.add(new BackupSchedule(id, name, cronExpression, enabled, maxBackups, created, lastRun));
        }
        
        return result;
    }
    
    /**
     * 監査ログ取得
     */
    public List<AuditLog> getAuditLogs() throws IOException {
        String response = get("/audit/logs");
        JsonArray logs = gson.fromJson(response, JsonArray.class);
        
        List<AuditLog> result = new ArrayList<>();
        for (JsonElement elem : logs) {
            JsonObject log = elem.getAsJsonObject();
            String time = log.get("time").getAsString();
            String apiKey = log.get("api_key").getAsString();
            String role = log.get("role").getAsString();
            String action = log.get("action").getAsString();
            String detail = log.get("detail").getAsString();
            String ip = log.get("ip").getAsString();
            
            result.add(new AuditLog(time, apiKey, role, action, detail, ip));
        }
        
        return result;
    }
    
    /**
     * ログ取得
     */
    public String getLogs() throws IOException {
        String response = get("/logs");
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("logs").getAsString();
    }
    
    // =============================
    // データクラス
    // =============================
    
    public static class ServerStatus {
        public final String status;
        public final JsonObject container;
        
        public ServerStatus(String status, JsonObject container) {
            this.status = status;
            this.container = container;
        }
    }
    
    public static class BackupResult {
        public final String filename;
        
        public BackupResult(String filename) {
            this.filename = filename;
        }
    }
    
    public static class BackupInfo {
        public final String filename;
        public final double sizeMb;
        public final String modified;
        
        public BackupInfo(String filename, double sizeMb, String modified) {
            this.filename = filename;
            this.sizeMb = sizeMb;
            this.modified = modified;
        }
    }
    
    public static class PlayerList {
        public final int count;
        public final List<String> players;
        
        public PlayerList(int count, List<String> players) {
            this.count = count;
            this.players = players;
        }
    }
    
    public static class CommandResult {
        public final String time;
        public final String command;
        public final String output;
        
        public CommandResult(String time, String command, String output) {
            this.time = time;
            this.command = command;
            this.output = output;
        }
    }
    
    public static class MetricsInfo {
        public final double totalGb;
        public final double usedGb;
        public final double percent;
        
        public MetricsInfo(double totalGb, double usedGb, double percent) {
            this.totalGb = totalGb;
            this.usedGb = usedGb;
            this.percent = percent;
        }
    }
    
    public static class PluginInfo {
        public final String name;
        public final double sizeMb;
        
        public PluginInfo(String name, double sizeMb) {
            this.name = name;
            this.sizeMb = sizeMb;
        }
    }
    
    public static class RestoreResult {
        public final String status;
        public final String backup;
        public final String preRestoreBackup;
        
        public RestoreResult(String status, String backup, String preRestoreBackup) {
            this.status = status;
            this.backup = backup;
            this.preRestoreBackup = preRestoreBackup;
        }
    }
    
    public static class BackupSchedule {
        public final int id;
        public final String name;
        public final String cronExpression;
        public final boolean enabled;
        public final int maxBackups;
        public final String created;
        public final String lastRun;
        
        public BackupSchedule(int id, String name, String cronExpression, boolean enabled, 
                            int maxBackups, String created, String lastRun) {
            this.id = id;
            this.name = name;
            this.cronExpression = cronExpression;
            this.enabled = enabled;
            this.maxBackups = maxBackups;
            this.created = created;
            this.lastRun = lastRun;
        }
    }
    
    public static class AuditLog {
        public final String time;
        public final String apiKey;
        public final String role;
        public final String action;
        public final String detail;
        public final String ip;
        
        public AuditLog(String time, String apiKey, String role, String action, String detail, String ip) {
            this.time = time;
            this.apiKey = apiKey;
            this.role = role;
            this.action = action;
            this.detail = detail;
            this.ip = ip;
        }
    }
}
