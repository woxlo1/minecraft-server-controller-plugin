package com.woxloi.minecraftservercontroller.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * コマンドテンプレート管理
 * - お気に入りコマンドの保存
 * - プレースホルダー対応
 * - コマンド履歴
 */
public class CommandTemplateManager {

    private final File dataFile;
    private final Gson gson;
    private Map<UUID, List<CommandTemplate>> playerTemplates;
    private Map<UUID, List<String>> commandHistory;
    private static final int MAX_HISTORY = 50;

    public CommandTemplateManager(File dataDir) {
        this.dataFile = new File(dataDir, "command_templates.json");
        this.gson = new Gson();
        this.playerTemplates = new HashMap<>();
        this.commandHistory = new HashMap<>();
        load();
    }

    /**
     * テンプレートを追加
     */
    public void addTemplate(UUID playerId, String name, String command, String description) {
        List<CommandTemplate> templates = playerTemplates.computeIfAbsent(playerId, k -> new ArrayList<>());

        // 同名テンプレートを削除
        templates.removeIf(t -> t.name.equals(name));

        templates.add(new CommandTemplate(name, command, description));
        save();
    }

    /**
     * テンプレートを削除
     */
    public boolean removeTemplate(UUID playerId, String name) {
        List<CommandTemplate> templates = playerTemplates.get(playerId);
        if (templates == null) return false;

        boolean removed = templates.removeIf(t -> t.name.equals(name));
        if (removed) {
            save();
        }
        return removed;
    }

    /**
     * テンプレート一覧を取得
     */
    public List<CommandTemplate> getTemplates(UUID playerId) {
        return playerTemplates.getOrDefault(playerId, new ArrayList<>());
    }

    /**
     * テンプレートを取得
     */
    public CommandTemplate getTemplate(UUID playerId, String name) {
        List<CommandTemplate> templates = playerTemplates.get(playerId);
        if (templates == null) return null;

        return templates.stream()
                .filter(t -> t.name.equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * プレースホルダーを置換
     */
    public String replacePlaceholders(String command, Map<String, String> values) {
        String result = command;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return result;
    }

    /**
     * コマンド履歴に追加
     */
    public void addToHistory(UUID playerId, String command) {
        List<String> history = commandHistory.computeIfAbsent(playerId, k -> new ArrayList<>());

        // 重複を削除
        history.remove(command);

        // 先頭に追加
        history.add(0, command);

        // 上限を超えたら古いものを削除
        while (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }

        save();
    }

    /**
     * コマンド履歴を取得
     */
    public List<String> getHistory(UUID playerId, int limit) {
        List<String> history = commandHistory.getOrDefault(playerId, new ArrayList<>());
        return history.subList(0, Math.min(limit, history.size()));
    }

    /**
     * デフォルトテンプレートを作成
     */
    public void createDefaultTemplates(UUID playerId) {
        if (playerTemplates.containsKey(playerId)) return;

        addTemplate(playerId, "day", "time set day", "Set time to day");
        addTemplate(playerId, "night", "time set night", "Set time to night");
        addTemplate(playerId, "clear", "weather clear", "Clear weather");
        addTemplate(playerId, "tp", "tp {player} {x} {y} {z}", "Teleport player");
        addTemplate(playerId, "give", "give {player} {item} {amount}", "Give items");
        addTemplate(playerId, "gamemode", "gamemode {mode} {player}", "Change gamemode");
    }

    /**
     * データを保存
     */
    private void save() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            Map<String, Object> data = new HashMap<>();
            data.put("templates", playerTemplates);
            data.put("history", commandHistory);
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * データを読み込み
     */
    private void load() {
        if (!dataFile.exists()) return;

        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> data = gson.fromJson(reader, type);

            if (data != null) {
                if (data.containsKey("templates")) {
                    Type templatesType = new TypeToken<Map<UUID, List<CommandTemplate>>>(){}.getType();
                    playerTemplates = gson.fromJson(gson.toJson(data.get("templates")), templatesType);
                }

                if (data.containsKey("history")) {
                    Type historyType = new TypeToken<Map<UUID, List<String>>>(){}.getType();
                    commandHistory = gson.fromJson(gson.toJson(data.get("history")), historyType);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * コマンドテンプレートクラス
     */
    public static class CommandTemplate {
        public final String name;
        public final String command;
        public final String description;

        public CommandTemplate(String name, String command, String description) {
            this.name = name;
            this.command = command;
            this.description = description;
        }

        /**
         * プレースホルダーを抽出
         */
        public List<String> getPlaceholders() {
            List<String> placeholders = new ArrayList<>();
            String[] parts = command.split("\\{");

            for (int i = 1; i < parts.length; i++) {
                int endIndex = parts[i].indexOf('}');
                if (endIndex != -1) {
                    placeholders.add(parts[i].substring(0, endIndex));
                }
            }

            return placeholders;
        }

        public boolean hasPlaceholders() {
            return command.contains("{") && command.contains("}");
        }
    }
}