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
import java.util.logging.Logger;

public class APIClient {

    private final String baseUrl;
    private final String apiKey;
    private final Gson gson;
    private final Logger logger;
    private final boolean debug;

    public APIClient(String baseUrl, String apiKey, Logger logger, boolean debug) {
        // /docs が付いている場合は自動的に削除
        if (baseUrl.endsWith("/docs")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 5);
            logger.warning("API URL had '/docs' suffix, automatically removed. New URL: " + baseUrl);
        }

        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.gson = new Gson();
        this.logger = logger;
        this.debug = debug;

        if (debug) {
            logger.info("APIClient initialized:");
            logger.info("  Base URL: " + this.baseUrl);
            logger.info("  API Key: " + (apiKey.isEmpty() ? "NOT SET" : apiKey.substring(0, Math.min(8, apiKey.length())) + "..."));
        }
    }

    /**
     * GET リクエスト
     */
    private String get(String endpoint) throws IOException {
        String fullUrl = baseUrl + endpoint;

        if (debug) {
            logger.info("GET Request: " + fullUrl);
        }

        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();

        if (debug) {
            logger.info("Response Code: " + responseCode);
        }

        if (responseCode == 200) {
            return readResponse(conn.getInputStream());
        } else {
            String errorBody = readResponse(conn.getErrorStream());
            String errorMsg = "HTTP Error: " + responseCode + " - " + errorBody;

            // 詳細なエラーログ
            logger.warning("API Request Failed:");
            logger.warning("  URL: " + fullUrl);
            logger.warning("  Method: GET");
            logger.warning("  Response Code: " + responseCode);
            logger.warning("  Error Body: " + errorBody);

            // 404エラーの場合は特別な処理
            if (responseCode == 404) {
                logger.warning("404 Not Found - Possible causes:");
                logger.warning("  1. API server is not running");
                logger.warning("  2. Wrong endpoint: " + endpoint);
                logger.warning("  3. API version mismatch");
                logger.warning("  4. Check if baseUrl is correct: " + baseUrl);
            }

            throw new IOException(errorMsg);
        }
    }

    /**
     * POST リクエスト
     */
    private String post(String endpoint, String jsonBody) throws IOException {
        String fullUrl = baseUrl + endpoint;

        if (debug) {
            logger.info("POST Request: " + fullUrl);
            if (jsonBody != null) {
                logger.info("Body: " + jsonBody);
            }
        }

        URL url = new URL(fullUrl);
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

        if (debug) {
            logger.info("Response Code: " + responseCode);
        }

        if (responseCode == 200) {
            return readResponse(conn.getInputStream());
        } else {
            String errorBody = readResponse(conn.getErrorStream());
            String errorMsg = "HTTP Error: " + responseCode + " - " + errorBody;

            logger.warning("API Request Failed:");
            logger.warning("  URL: " + fullUrl);
            logger.warning("  Method: POST");
            logger.warning("  Response Code: " + responseCode);
            logger.warning("  Error Body: " + errorBody);

            if (responseCode == 404) {
                logger.warning("404 Not Found - Check if this endpoint exists in your API");
            } else if (responseCode == 403) {
                logger.warning("403 Forbidden - Check your API key");
            }

            throw new IOException(errorMsg);
        }
    }

    /**
     * POST リクエスト (form-data)
     */
    private String postForm(String endpoint, String key, String value) throws IOException {
        String fullUrl = baseUrl + endpoint;

        if (debug) {
            logger.info("POST (form-data) Request: " + fullUrl);
            logger.info("Form data: " + key + "=" + value);
        }

        URL url = new URL(fullUrl);
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

        if (debug) {
            logger.info("Response Code: " + responseCode);
        }

        if (responseCode == 200) {
            return readResponse(conn.getInputStream());
        } else {
            String errorBody = readResponse(conn.getErrorStream());

            logger.warning("API Request Failed:");
            logger.warning("  URL: " + fullUrl);
            logger.warning("  Method: POST (form-data)");
            logger.warning("  Response Code: " + responseCode);
            logger.warning("  Error Body: " + errorBody);

            throw new IOException("HTTP Error: " + responseCode + " - " + errorBody);
        }
    }

    /**
     * PATCH リクエスト
     */
    private String patch(String endpoint) throws IOException {
        String fullUrl = baseUrl + endpoint;

        if (debug) {
            logger.info("PATCH Request: " + fullUrl);
        }

        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();

        if (debug) {
            logger.info("Response Code: " + responseCode);
        }

        if (responseCode == 200) {
            return readResponse(conn.getInputStream());
        } else {
            String errorBody = readResponse(conn.getErrorStream());

            logger.warning("API Request Failed:");
            logger.warning("  URL: " + fullUrl);
            logger.warning("  Method: PATCH");
            logger.warning("  Response Code: " + responseCode);
            logger.warning("  Error Body: " + errorBody);

            throw new IOException("HTTP Error: " + responseCode + " - " + errorBody);
        }
    }

    /**
     * DELETE リクエスト
     */
    private String delete(String endpoint) throws IOException {
        String fullUrl = baseUrl + endpoint;

        if (debug) {
            logger.info("DELETE Request: " + fullUrl);
        }

        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("X-API-Key", apiKey);
        conn.setRequestProperty("Content-Type", "application/json");

        int responseCode = conn.getResponseCode();

        if (debug) {
            logger.info("Response Code: " + responseCode);
        }

        if (responseCode == 200) {
            return readResponse(conn.getInputStream());
        } else {
            String errorBody = readResponse(conn.getErrorStream());

            logger.warning("API Request Failed:");
            logger.warning("  URL: " + fullUrl);
            logger.warning("  Method: DELETE");
            logger.warning("  Response Code: " + responseCode);
            logger.warning("  Error Body: " + errorBody);

            throw new IOException("HTTP Error: " + responseCode + " - " + errorBody);
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

            String result = response.toString();
            if (debug && !result.isEmpty()) {
                logger.info("Response: " + (result.length() > 500 ? result.substring(0, 500) + "..." : result));
            }

            return result;
        }
    }

    // =============================
    // API メソッド
    // =============================

    /**
     * サーバーステータス取得
     */
    public ServerStatus getServerStatus() throws IOException {
        String response = get("/status");
        JsonObject json = gson.fromJson(response, JsonObject.class);

        String status = json.get("status").getAsString();
        JsonObject container = json.has("container") ? json.getAsJsonObject("container") : null;

        return new ServerStatus(status, container);
    }

    /**
     * バックアップ作成
     */
    public BackupResult createBackup() throws IOException {
        String response = post("/backup", null);
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
            String filename = backup.get("name").getAsString();
            double sizeMb = backup.get("size_mb").getAsDouble();
            String created = backup.get("created").getAsString();

            result.add(new BackupInfo(filename, sizeMb, created));
        }

        return result;
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
     * バックアップスケジュールを作成
     */
    public BackupSchedule createBackupSchedule(String name, String cronExpression, int maxBackups) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", name);
        requestBody.addProperty("cron_expression", cronExpression);
        requestBody.addProperty("max_backups", maxBackups);

        String response = post("/backup/schedules", gson.toJson(requestBody));
        JsonObject json = gson.fromJson(response, JsonObject.class);

        int id = json.get("id").getAsInt();
        String createdName = json.get("name").getAsString();
        boolean enabled = json.get("enabled").getAsBoolean();

        return new BackupSchedule(
                id,
                createdName,
                cronExpression,
                enabled,
                maxBackups,
                null, // created がレスポンスに無いなら
                null  // lastRun
        );
    }

    /**
     * バックアップを削除
     */
    public String deleteBackup(String filename) throws IOException {
        String response = delete("/backups/" + filename);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("status").getAsString();
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
     * バックアップスケジュールの有効/無効を切り替え
     */
    public ScheduleToggleResult toggleBackupSchedule(int scheduleId) throws IOException {
        String response = patch("/backup/schedules/" + scheduleId + "/toggle");
        JsonObject json = gson.fromJson(response, JsonObject.class);

        int id = json.get("id").getAsInt();
        boolean enabled = json.get("enabled").getAsBoolean();

        return new ScheduleToggleResult(id, enabled);
    }

    /**
     * バックアップスケジュールを削除
     */
    public String deleteBackupSchedule(int scheduleId) throws IOException {
        String response = delete("/backup/schedules/" + scheduleId);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("status").getAsString();
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

    /**
     * プレイヤー統計を取得
     */
    public PlayerStats getPlayerStats(String playerName) throws IOException {
        String response = get("/stats/player/" + playerName);
        JsonObject json = gson.fromJson(response, JsonObject.class);

        String playerUuid = json.get("player_uuid").getAsString();
        String name = json.get("player_name").getAsString();
        double totalPlaytimeHours = json.get("total_playtime_hours").getAsDouble();
        int totalSessions = json.get("total_sessions").getAsInt();
        String firstJoin = json.get("first_join").getAsString();
        String lastJoin = json.get("last_join").getAsString();

        List<ActivityRecord> recentActivity = new ArrayList<>();
        JsonArray activityArray = json.getAsJsonArray("recent_activity");
        for (JsonElement elem : activityArray) {
            JsonObject activity = elem.getAsJsonObject();
            String login = activity.get("login").getAsString();
            String logout = activity.has("logout") && !activity.get("logout").isJsonNull()
                    ? activity.get("logout").getAsString() : null;
            Integer durationMinutes = activity.has("duration_minutes") && !activity.get("duration_minutes").isJsonNull()
                    ? activity.get("duration_minutes").getAsInt() : null;

            recentActivity.add(new ActivityRecord(login, logout, durationMinutes));
        }

        return new PlayerStats(playerUuid, name, totalPlaytimeHours, totalSessions,
                firstJoin, lastJoin, recentActivity);
    }

    /**
     * 全プレイヤー統計を取得
     */
    public List<PlayerStatsSummary> getAllPlayerStats() throws IOException {
        String response = get("/stats/players");
        JsonArray array = gson.fromJson(response, JsonArray.class);

        List<PlayerStatsSummary> stats = new ArrayList<>();
        for (JsonElement elem : array) {
            JsonObject obj = elem.getAsJsonObject();
            String playerName = obj.get("player_name").getAsString();
            double totalPlaytimeHours = obj.get("total_playtime_hours").getAsDouble();
            int totalSessions = obj.get("total_sessions").getAsInt();
            String lastJoin = obj.get("last_join").getAsString();

            stats.add(new PlayerStatsSummary(playerName, totalPlaytimeHours, totalSessions, lastJoin));
        }

        return stats;
    }

    /**
     * 現在のパフォーマンス情報を取得
     */
    public CurrentPerformance getCurrentPerformance() throws IOException {
        String response = get("/performance/current");
        JsonObject json = gson.fromJson(response, JsonObject.class);

        if (json.has("message")) {
            // データがない場合
            return null;
        }

        String timestamp = json.get("timestamp").getAsString();
        double tps = json.get("tps").getAsDouble();
        int memoryUsedMb = json.get("memory_used_mb").getAsInt();
        int memoryTotalMb = json.get("memory_total_mb").getAsInt();
        double memoryPercent = json.get("memory_percent").getAsDouble();
        int entities = json.get("entities").getAsInt();
        int chunks = json.get("chunks").getAsInt();
        int players = json.get("players").getAsInt();

        return new CurrentPerformance(timestamp, tps, memoryUsedMb, memoryTotalMb,
                memoryPercent, entities, chunks, players);
    }

    /**
     * パフォーマンス履歴を取得
     */
    public List<PerformanceHistory> getPerformanceHistory(int hours) throws IOException {
        String response = get("/performance/history?hours=" + hours);
        JsonArray array = gson.fromJson(response, JsonArray.class);

        List<PerformanceHistory> history = new ArrayList<>();
        for (JsonElement elem : array) {
            JsonObject obj = elem.getAsJsonObject();
            String timestamp = obj.get("timestamp").getAsString();
            double tps = obj.get("tps").getAsDouble();
            double memoryPercent = obj.get("memory_percent").getAsDouble();
            int entities = obj.get("entities").getAsInt();
            int chunks = obj.get("chunks").getAsInt();

            history.add(new PerformanceHistory(timestamp, tps, memoryPercent, entities, chunks));
        }

        return history;
    }

    /**
     * パフォーマンスデータを記録
     */
    public void recordPerformance(double tps, int memoryUsed, int memoryTotal, double memoryPercent,
                                  int entities, int chunks, int players) throws IOException {
        JsonObject data = new JsonObject();
        data.addProperty("tps", tps);
        data.addProperty("memory_used", memoryUsed);
        data.addProperty("memory_total", memoryTotal);
        data.addProperty("memory_percent", memoryPercent);
        data.addProperty("entities", entities);
        data.addProperty("chunks", chunks);
        data.addProperty("players", players);

        post("/performance/record", gson.toJson(data));
    }

    /**
     * 最近のチャットログを取得
     */
    public List<ChatMessage> getRecentChat(int limit) throws IOException {
        String response = get("/chat/recent?limit=" + limit);
        JsonArray array = gson.fromJson(response, JsonArray.class);

        List<ChatMessage> messages = new ArrayList<>();
        for (JsonElement elem : array) {
            JsonObject obj = elem.getAsJsonObject();
            String timestamp = obj.get("timestamp").getAsString();
            String playerName = obj.get("player_name").getAsString();
            String message = obj.get("message").getAsString();
            String world = obj.get("world").getAsString();

            messages.add(new ChatMessage(timestamp, playerName, message, world));
        }

        return messages;
    }

    /**
     * チャットログをキーワード検索
     */
    public List<ChatMessage> searchChat(String keyword, int limit) throws IOException {
        String response = get("/chat/search?keyword=" + keyword + "&limit=" + limit);
        JsonArray array = gson.fromJson(response, JsonArray.class);

        List<ChatMessage> messages = new ArrayList<>();
        for (JsonElement elem : array) {
            JsonObject obj = elem.getAsJsonObject();
            String timestamp = obj.get("timestamp").getAsString();
            String playerName = obj.get("player_name").getAsString();
            String message = obj.get("message").getAsString();
            String world = obj.get("world").getAsString();

            messages.add(new ChatMessage(timestamp, playerName, message, world));
        }

        return messages;
    }

    /**
     * プレイヤー別チャットログを取得
     */
    public List<ChatMessage> getPlayerChat(String playerName, int limit) throws IOException {
        String response = get("/chat/player/" + playerName + "?limit=" + limit);
        JsonArray array = gson.fromJson(response, JsonArray.class);

        List<ChatMessage> messages = new ArrayList<>();
        for (JsonElement elem : array) {
            JsonObject obj = elem.getAsJsonObject();
            String timestamp = obj.get("timestamp").getAsString();
            String message = obj.get("message").getAsString();
            String world = obj.get("world").getAsString();

            messages.add(new ChatMessage(timestamp, playerName, message, world));
        }

        return messages;
    }

    /**
     * チャット統計を取得
     */
    public ChatStats getChatStats() throws IOException {
        String response = get("/chat/stats");
        JsonObject json = gson.fromJson(response, JsonObject.class);

        long totalMessages = json.get("total_messages").getAsLong();
        long todayMessages = json.get("today_messages").getAsLong();
        JsonObject topChatter = json.getAsJsonObject("top_chatter");
        String topPlayerName = topChatter.get("player_name").isJsonNull()
                ? null : topChatter.get("player_name").getAsString();
        int messageCount = topChatter.get("message_count").getAsInt();

        return new ChatStats(totalMessages, todayMessages, topPlayerName, messageCount);
    }

    /**
     * チャットメッセージを記録
     */
    public void logChatMessage(String playerUuid, String playerName, String message, String world) throws IOException {
        JsonObject data = new JsonObject();
        data.addProperty("player_uuid", playerUuid);
        data.addProperty("player_name", playerName);
        data.addProperty("message", message);
        data.addProperty("world", world);

        post("/chat/log", gson.toJson(data));
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

    public static class ScheduleToggleResult {
        public final int id;
        public final boolean enabled;

        public ScheduleToggleResult(int id, boolean enabled) {
            this.id = id;
            this.enabled = enabled;
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

    public static class PlayerStats {
        public final String playerUuid;
        public final String playerName;
        public final double totalPlaytimeHours;
        public final int totalSessions;
        public final String firstJoin;
        public final String lastJoin;
        public final List<ActivityRecord> recentActivity;

        public PlayerStats(String playerUuid, String playerName, double totalPlaytimeHours,
                           int totalSessions, String firstJoin, String lastJoin,
                           List<ActivityRecord> recentActivity) {
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.totalPlaytimeHours = totalPlaytimeHours;
            this.totalSessions = totalSessions;
            this.firstJoin = firstJoin;
            this.lastJoin = lastJoin;
            this.recentActivity = recentActivity;
        }
    }

    public static class ActivityRecord {
        public final String login;
        public final String logout;
        public final Integer durationMinutes;

        public ActivityRecord(String login, String logout, Integer durationMinutes) {
            this.login = login;
            this.logout = logout;
            this.durationMinutes = durationMinutes;
        }
    }

    public static class PlayerStatsSummary {
        public final String playerName;
        public final double totalPlaytimeHours;
        public final int totalSessions;
        public final String lastJoin;

        public PlayerStatsSummary(String playerName, double totalPlaytimeHours,
                                  int totalSessions, String lastJoin) {
            this.playerName = playerName;
            this.totalPlaytimeHours = totalPlaytimeHours;
            this.totalSessions = totalSessions;
            this.lastJoin = lastJoin;
        }
    }

    public static class CurrentPerformance {
        public final String timestamp;
        public final double tps;
        public final int memoryUsedMb;
        public final int memoryTotalMb;
        public final double memoryPercent;
        public final int entities;
        public final int chunks;
        public final int players;

        public CurrentPerformance(String timestamp, double tps, int memoryUsedMb, int memoryTotalMb,
                                  double memoryPercent, int entities, int chunks, int players) {
            this.timestamp = timestamp;
            this.tps = tps;
            this.memoryUsedMb = memoryUsedMb;
            this.memoryTotalMb = memoryTotalMb;
            this.memoryPercent = memoryPercent;
            this.entities = entities;
            this.chunks = chunks;
            this.players = players;
        }
    }

    public static class PerformanceHistory {
        public final String timestamp;
        public final double tps;
        public final double memoryPercent;
        public final int entities;
        public final int chunks;

        public PerformanceHistory(String timestamp, double tps, double memoryPercent,
                                  int entities, int chunks) {
            this.timestamp = timestamp;
            this.tps = tps;
            this.memoryPercent = memoryPercent;
            this.entities = entities;
            this.chunks = chunks;
        }
    }

    public static class ChatMessage {
        public final String timestamp;
        public final String playerName;
        public final String message;
        public final String world;

        public ChatMessage(String timestamp, String playerName, String message, String world) {
            this.timestamp = timestamp;
            this.playerName = playerName;
            this.message = message;
            this.world = world;
        }
    }

    public static class ChatStats {
        public final long totalMessages;
        public final long todayMessages;
        public final String topPlayer;
        public final int topPlayerMessageCount;

        public ChatStats(long totalMessages, long todayMessages, String topPlayer, int topPlayerMessageCount) {
            this.totalMessages = totalMessages;
            this.todayMessages = todayMessages;
            this.topPlayer = topPlayer;
            this.topPlayerMessageCount = topPlayerMessageCount;
        }
    }
}