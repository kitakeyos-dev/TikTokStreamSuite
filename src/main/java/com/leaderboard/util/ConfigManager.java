package com.leaderboard.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class AppConfig {
        private String streamerUsername = "streamer_live";
        private String eulerstreamKey = "euler_test_key_abc123";
        private int likeTarget = 10000;
        private boolean overlayLeaderboardOnTop = true;
        private boolean overlayChatOnTop = true;
        private boolean overlayLikeOnTop = true;
        private boolean overlayTopLikeOnTop = true;
        private boolean ttsEnabled = false;
        private boolean ttsReadUsername = true;
        private String ttsBlockedWords = null;
        private String ttsBlockedWordsVi = "địt, cặc, lồn, buồi, đéo, đĩ, chó ngu, ngu lồn, cặc chó, ăn cứt, ăn lồn, óc chó, ngu si, vcl, vkl, dcm, đkm, đm, vú, chim, bướm, fuck, bitch";
        private String ttsBlockedWordsEn = "fuck, bitch, shit, asshole, bastard, cunt, dick, pussy, motherfucker, cock, whore, slut, retard, trash";
        private int ttsMaxQueue = 8;
        private double ttsVolume = 1.0;
        private String ttsAudioDeviceName = "Default";
        private String updateMetadataUrl = "https://raw.githubusercontent.com/kitakeyos-dev/TikTokStreamSuite/master/update.json";
        private String language = "vi";

        public String getStreamerUsername() { return streamerUsername; }
        public void setStreamerUsername(String v) { this.streamerUsername = v; }

        public String getEulerstreamKey() { return eulerstreamKey; }
        public void setEulerstreamKey(String v) { this.eulerstreamKey = v; }

        public int getLikeTarget() {
            if (likeTarget <= 0) likeTarget = 10000;
            return likeTarget;
        }
        public void setLikeTarget(int v) { this.likeTarget = v; }

        public boolean isOverlayLeaderboardOnTop() { return overlayLeaderboardOnTop; }
        public void setOverlayLeaderboardOnTop(boolean v) { this.overlayLeaderboardOnTop = v; }

        public boolean isOverlayChatOnTop() { return overlayChatOnTop; }
        public void setOverlayChatOnTop(boolean v) { this.overlayChatOnTop = v; }

        public boolean isOverlayLikeOnTop() { return overlayLikeOnTop; }
        public void setOverlayLikeOnTop(boolean v) { this.overlayLikeOnTop = v; }

        public boolean isOverlayTopLikeOnTop() { return overlayTopLikeOnTop; }
        public void setOverlayTopLikeOnTop(boolean v) { this.overlayTopLikeOnTop = v; }

        public boolean isTtsEnabled() { return ttsEnabled; }
        public void setTtsEnabled(boolean v) { this.ttsEnabled = v; }

        public boolean isTtsReadUsername() { return ttsReadUsername; }
        public void setTtsReadUsername(boolean v) { this.ttsReadUsername = v; }

        public String getTtsBlockedWords() {
            // Migration check: if they have a legacy config with custom blocked words, migrate it to the active language!
            if (ttsBlockedWords != null && !ttsBlockedWords.trim().isEmpty()) {
                if ("en".equals(getLanguage())) {
                    ttsBlockedWordsEn = ttsBlockedWords;
                } else {
                    ttsBlockedWordsVi = ttsBlockedWords;
                }
                ttsBlockedWords = null; // Clear migration flag
            }

            if ("en".equals(getLanguage())) {
                if (ttsBlockedWordsEn == null) {
                    ttsBlockedWordsEn = "fuck, bitch, shit, asshole, bastard, cunt, dick, pussy, motherfucker, cock, whore, slut, retard, trash";
                }
                return ttsBlockedWordsEn;
            } else {
                if (ttsBlockedWordsVi == null) {
                    ttsBlockedWordsVi = "địt, cặc, lồn, buồi, đéo, đĩ, chó ngu, ngu lồn, cặc chó, ăn cứt, ăn lồn, óc chó, ngu si, vcl, vkl, dcm, đkm, đm, vú, chim, bướm, fuck, bitch";
                }
                return ttsBlockedWordsVi;
            }
        }

        public void setTtsBlockedWords(String v) {
            if ("en".equals(getLanguage())) {
                this.ttsBlockedWordsEn = v;
            } else {
                this.ttsBlockedWordsVi = v;
            }
        }

        public int getTtsMaxQueue() {
            if (ttsMaxQueue <= 0) ttsMaxQueue = 8;
            return ttsMaxQueue;
        }
        public void setTtsMaxQueue(int v) { this.ttsMaxQueue = v; }

        public double getTtsVolume() {
            if (ttsVolume < 0.0 || ttsVolume > 1.0) ttsVolume = 1.0;
            return ttsVolume;
        }
        public void setTtsVolume(double v) { this.ttsVolume = v; }

        public String getTtsAudioDeviceName() {
            if (ttsAudioDeviceName == null) ttsAudioDeviceName = "Default";
            return ttsAudioDeviceName;
        }
        public void setTtsAudioDeviceName(String v) { this.ttsAudioDeviceName = v; }

        public String getUpdateMetadataUrl() {
            if (updateMetadataUrl == null || updateMetadataUrl.trim().isEmpty()) {
                updateMetadataUrl = "https://raw.githubusercontent.com/kitakeyos-dev/TikTokStreamSuite/master/update.json";
            }
            return updateMetadataUrl;
        }
        public void setUpdateMetadataUrl(String v) { this.updateMetadataUrl = v; }

        public String getLanguage() {
            if (language == null || (!language.equals("vi") && !language.equals("en"))) {
                language = "vi";
            }
            return language;
        }
        public void setLanguage(String v) { this.language = v; }
    }

    private static AppConfig currentConfig = new AppConfig();

    static {
        load();
    }

    public static File getStorageFile(String filename) {
        String os = System.getProperty("os.name").toLowerCase();
        String baseDir;
        String appName = "TikTokStreamSuite";
        
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                baseDir = appData + File.separator + appName;
            } else {
                baseDir = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + appName;
            }
        } else if (os.contains("mac")) {
            baseDir = System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + appName;
        } else {
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfig != null && !xdgConfig.isEmpty()) {
                baseDir = xdgConfig + File.separator + appName;
            } else {
                baseDir = System.getProperty("user.home") + File.separator + ".config" + File.separator + appName;
            }
        }

        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File targetFile = new File(dir, filename);

        // Triple-layer Migration check: AppData <- ~/.tiktokstream <- local root
        if (!targetFile.exists()) {
            // Check in intermediate ~/.tiktokstream
            File intermediateFile = new File(System.getProperty("user.home") + File.separator + ".tiktokstream" + File.separator + filename);
            if (intermediateFile.exists()) {
                try {
                    intermediateFile.renameTo(targetFile);
                    System.out.println("Migrated " + filename + " from ~/.tiktokstream to AppData: " + targetFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Failed to migrate " + filename + " from ~/.tiktokstream: " + e.getMessage());
                }
            } else {
                // Check in local project root
                File oldLocalFile = new File(filename);
                if (oldLocalFile.exists()) {
                    try {
                        oldLocalFile.renameTo(targetFile);
                        System.out.println("Migrated " + filename + " from project root to AppData: " + targetFile.getAbsolutePath());
                    } catch (Exception e) {
                        System.err.println("Failed to migrate " + filename + " from project root: " + e.getMessage());
                    }
                }
            }
        }

        migrateLegacyBankFile(targetFile, filename);

        return targetFile;
    }

    private static void migrateLegacyBankFile(File targetFile, String filename) {
        if (targetFile.exists()) {
            return;
        }

        String legacyName = switch (filename) {
            case "bank_config.dat" -> "config.dat";
            case "bank_deposits.json" -> "deposits.json";
            default -> null;
        };
        if (legacyName == null) {
            return;
        }

        File legacyFile = new File(
                System.getProperty("user.home") + File.separator + ".bankpusher" + File.separator + legacyName);
        if (legacyFile.exists()) {
            try {
                legacyFile.renameTo(targetFile);
                System.out.println("Migrated " + legacyName + " from ~/.bankpusher to AppData: "
                        + targetFile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Failed to migrate " + legacyName + " from ~/.bankpusher: " + e.getMessage());
            }
        }
    }

    public static synchronized AppConfig getConfig() {
        return currentConfig;
    }

    public static synchronized void load() {
        File file = getStorageFile("config.json");
        if (!file.exists()) {
            currentConfig = new AppConfig();
            save();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            AppConfig loaded = GSON.fromJson(reader, AppConfig.class);
            if (loaded != null) {
                currentConfig = loaded;
            }
        } catch (Exception e) {
            System.err.println("Error reading config.json: " + e.getMessage());
            currentConfig = new AppConfig();
        }
    }

    public static synchronized void save() {
        File file = getStorageFile("config.json");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(currentConfig, writer);
        } catch (Exception e) {
            System.err.println("Error saving config.json: " + e.getMessage());
        }
    }
}
