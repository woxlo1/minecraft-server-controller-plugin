package com.woxloi.minecraftservercontroller.utils;

import com.woxloi.minecraftservercontroller.MinecraftServerController;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ワールド管理機能
 * - ワールドのインポート/エクスポート
 * - ワールド別バックアップ
 * - ワールドの読み込み/アンロード
 */
public class WorldManager {

    private final MinecraftServerController plugin;
    private final File worldContainer;
    private final File backupDir;

    public WorldManager(MinecraftServerController plugin, File backupDir) {
        this.plugin = plugin;
        this.worldContainer = Bukkit.getWorldContainer();
        this.backupDir = new File(backupDir, "worlds");
        this.backupDir.mkdirs();
    }

    /**
     * 利用可能なワールド一覧を取得
     */
    public List<WorldInfo> getAvailableWorlds() {
        List<WorldInfo> worlds = new ArrayList<>();

        File[] files = worldContainer.listFiles();
        if (files == null) return worlds;

        for (File file : files) {
            if (!file.isDirectory()) continue;

            // level.dat が存在するかチェック
            File levelDat = new File(file, "level.dat");
            if (!levelDat.exists()) continue;

            String worldName = file.getName();
            World loadedWorld = Bukkit.getWorld(worldName);
            boolean isLoaded = loadedWorld != null;

            WorldInfo info = new WorldInfo(
                    worldName,
                    isLoaded,
                    getWorldSize(file),
                    loadedWorld != null ? loadedWorld.getEnvironment().name() : "UNKNOWN"
            );

            worlds.add(info);
        }

        return worlds;
    }

    /**
     * ワールドを読み込む
     */
    public boolean loadWorld(String worldName) {
        try {
            // 既に読み込まれているか確認
            if (Bukkit.getWorld(worldName) != null) {
                return true;
            }

            // ワールドを読み込み
            WorldCreator creator = new WorldCreator(worldName);
            World world = creator.createWorld();

            if (world != null) {
                plugin.getLogger().info("World loaded: " + worldName);
                return true;
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load world: " + e.getMessage());
        }

        return false;
    }

    /**
     * ワールドをアンロード
     */
    public boolean unloadWorld(String worldName, boolean save) {
        try {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return true; // 既にアンロード済み
            }

            // メインワールドはアンロードできない
            if (world.equals(Bukkit.getWorlds().get(0))) {
                plugin.getLogger().warning("Cannot unload main world!");
                return false;
            }

            // プレイヤーをメインワールドに移動
            World mainWorld = Bukkit.getWorlds().get(0);
            world.getPlayers().forEach(player -> {
                player.teleport(mainWorld.getSpawnLocation());
            });

            // アンロード
            boolean result = Bukkit.unloadWorld(world, save);

            if (result) {
                plugin.getLogger().info("World unloaded: " + worldName);
            }

            return result;

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unload world: " + e.getMessage());
            return false;
        }
    }

    /**
     * ワールドをバックアップ
     */
    public File backupWorld(String worldName) throws IOException {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            world.save();
        }

        File worldFolder = new File(worldContainer, worldName);
        if (!worldFolder.exists()) {
            throw new IOException("World not found: " + worldName);
        }

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupFile = new File(backupDir, worldName + "_" + timestamp + ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(backupFile.toPath()))) {
            zipDirectory(worldFolder.toPath(), worldFolder.toPath(), zos);
        }

        plugin.getLogger().info("World backed up: " + worldName + " -> " + backupFile.getName());
        return backupFile;
    }

    /**
     * ワールドバックアップ一覧を取得
     */
    public List<File> getWorldBackups(String worldName) {
        List<File> backups = new ArrayList<>();

        File[] files = backupDir.listFiles();
        if (files == null) return backups;

        String prefix = worldName + "_";
        for (File file : files) {
            if (file.getName().startsWith(prefix) && file.getName().endsWith(".zip")) {
                backups.add(file);
            }
        }

        backups.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return backups;
    }

    /**
     * ワールドバックアップからリストア
     */
    public boolean restoreWorld(String worldName, File backupFile) throws IOException {
        // ワールドをアンロード
        unloadWorld(worldName, false);

        File worldFolder = new File(worldContainer, worldName);

        // 既存のワールドフォルダを削除
        if (worldFolder.exists()) {
            deleteDirectory(worldFolder.toPath());
        }

        // バックアップを展開
        worldFolder.mkdirs();
        unzipFile(backupFile.toPath(), worldFolder.toPath());

        plugin.getLogger().info("World restored: " + worldName + " from " + backupFile.getName());
        return true;
    }

    /**
     * ワールドのサイズを取得（MB）
     */
    private long getWorldSize(File worldFolder) {
        try {
            return Files.walk(worldFolder.toPath())
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum() / (1024 * 1024);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * ディレクトリをZIP化
     */
    private void zipDirectory(Path sourceDir, Path basePath, ZipOutputStream zos) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // session.lock はスキップ
                if (file.getFileName().toString().equals("session.lock")) {
                    return FileVisitResult.CONTINUE;
                }

                Path relativePath = basePath.relativize(file);
                ZipEntry entry = new ZipEntry(relativePath.toString());
                zos.putNextEntry(entry);
                Files.copy(file, zos);
                zos.closeEntry();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!dir.equals(sourceDir)) {
                    Path relativePath = basePath.relativize(dir);
                    ZipEntry entry = new ZipEntry(relativePath.toString() + "/");
                    zos.putNextEntry(entry);
                    zos.closeEntry();
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * ZIPファイルを展開
     */
    private void unzipFile(Path zipFile, Path destDir) throws IOException {
        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
                Files.newInputStream(zipFile))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path targetPath = destDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }

                zis.closeEntry();
            }
        }
    }

    /**
     * ディレクトリを削除
     */
    private void deleteDirectory(Path path) throws IOException {
        if (!Files.exists(path)) return;

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * ワールド情報クラス
     */
    public static class WorldInfo {
        public final String name;
        public final boolean loaded;
        public final long sizeMB;
        public final String environment;

        public WorldInfo(String name, boolean loaded, long sizeMB, String environment) {
            this.name = name;
            this.loaded = loaded;
            this.sizeMB = sizeMB;
            this.environment = environment;
        }
    }
}