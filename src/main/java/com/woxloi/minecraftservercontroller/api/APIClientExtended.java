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

public class APIClientExtended {
    
    private final String baseUrl;
    private final String apiKey;
    private final Gson gson;
    
    public APIClientExtended(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.gson = new Gson();
    }
    
    /**
     * PATCH リクエスト（スケジュールトグル用）
     */
    private String patch(String endpoint) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
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
     * DELETE リクエスト（スケジュール削除用）
     */
    private String deleteRequest(String endpoint) throws IOException {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
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
    // スケジュール管理 API（新規追加）
    // =============================
    
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
        String response = deleteRequest("/backup/schedules/" + scheduleId);
        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json.get("status").getAsString();
    }
    
    // =============================
    // データクラス（新規追加）
    // =============================
    
    public static class ScheduleToggleResult {
        public final int id;
        public final boolean enabled;
        
        public ScheduleToggleResult(int id, boolean enabled) {
            this.id = id;
            this.enabled = enabled;
        }
    }
}
