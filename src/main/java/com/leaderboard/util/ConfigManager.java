package com.leaderboard.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private static final String FILE_NAME = "config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class AppConfig {
        private String streamerUsername = "streamer_live";
        private String eulerstreamKey = "euler_test_key_abc123";
        private int likeTarget = 10000;
        private boolean overlayLeaderboardOnTop = true;
        private boolean overlayChatOnTop = true;
        private boolean overlayLikeOnTop = true;

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
    }

    private static AppConfig currentConfig = new AppConfig();

    static {
        load();
    }

    public static synchronized AppConfig getConfig() {
        return currentConfig;
    }

    public static synchronized void load() {
        File file = new File(FILE_NAME);
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
        File file = new File(FILE_NAME);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(currentConfig, writer);
        } catch (Exception e) {
            System.err.println("Error saving config.json: " + e.getMessage());
        }
    }
}
