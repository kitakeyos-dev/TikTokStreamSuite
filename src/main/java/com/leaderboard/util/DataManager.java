package com.leaderboard.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leaderboard.model.Gifter;
import com.leaderboard.model.Liker;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataManager {
    private static final String FILE_NAME = "data.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static class AppData {
        private List<Gifter> gifters = new ArrayList<>();
        private List<Liker> likers = new ArrayList<>();

        public List<Gifter> getGifters() {
            if (gifters == null) {
                gifters = new ArrayList<>();
            }
            return gifters;
        }

        public void setGifters(List<Gifter> gifters) {
            this.gifters = gifters;
        }

        public List<Liker> getLikers() {
            if (likers == null) {
                likers = new ArrayList<>();
            }
            return likers;
        }

        public void setLikers(List<Liker> likers) {
            this.likers = likers;
        }
    }

    private static AppData currentData = new AppData();

    static {
        load();
    }

    public static synchronized List<Gifter> getGifters() {
        return currentData.getGifters();
    }

    public static synchronized List<Liker> getLikers() {
        return currentData.getLikers();
    }

    public static synchronized void load() {
        File dataFile = new File(FILE_NAME);
        if (!dataFile.exists()) {
            currentData = new AppData();
            // Check if we can migrate from config.json first!
            boolean migrated = tryMigrateFromConfig();
            if (!migrated) {
                seedMockData();
            }
            save();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
            AppData loaded = GSON.fromJson(reader, AppData.class);
            if (loaded != null) {
                currentData = loaded;
                if (currentData.gifters == null) {
                    currentData.gifters = new ArrayList<>();
                }
                if (currentData.likers == null) {
                    currentData.likers = new ArrayList<>();
                }
                
                // Auto-migrate standard rules (e.g. invalid username swap etc.)
                boolean migrated = false;
                for (Gifter g : currentData.gifters) {
                    if (g.getUniqueId() != null && !g.getUniqueId().matches("^[a-zA-Z0-9_.-]+$")) {
                        String tempId = g.getUniqueId();
                        g.setUniqueId(g.getNickname());
                        g.setNickname(tempId);
                        migrated = true;
                    }
                }
                if (migrated) {
                    save();
                }
                Collections.sort(currentData.gifters);
                Collections.sort(currentData.likers);
            }
        } catch (Exception e) {
            System.err.println("Error reading data.json: " + e.getMessage());
            currentData = new AppData();
        }
    }

    public static synchronized void save() {
        File file = new File(FILE_NAME);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(currentData, writer);
        } catch (Exception e) {
            System.err.println("Error saving data.json: " + e.getMessage());
        }
    }

    public static synchronized void addLike(String uniqueId, String nickname, String avatarUrl, int amount) {
        List<Liker> list = getLikers();
        java.util.Optional<Liker> existing = list.stream()
                .filter(l -> l.getUniqueId().equalsIgnoreCase(uniqueId))
                .findFirst();

        if (existing.isPresent()) {
            Liker liker = existing.get();
            liker.addLikes(amount);
            if (avatarUrl != null) {
                liker.setAvatarUrl(avatarUrl);
            }
            if (nickname != null && !nickname.trim().isEmpty()) {
                liker.setNickname(nickname);
            }
        } else {
            list.add(new Liker(uniqueId, nickname != null ? nickname : uniqueId, avatarUrl, amount));
        }

        // Sort descending by likes
        Collections.sort(list);
        
        // Save changes immediately
        save();
    }

    private static boolean tryMigrateFromConfig() {
        File configFile = new File("config.json");
        if (!configFile.exists()) {
            return false;
        }

        try {
            com.google.gson.JsonObject jsonObject = null;
            try (Reader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
                jsonObject = GSON.fromJson(reader, com.google.gson.JsonObject.class);
            }

            if (jsonObject != null && jsonObject.has("gifters")) {
                com.google.gson.JsonArray giftersArray = jsonObject.getAsJsonArray("gifters");
                if (giftersArray != null && giftersArray.size() > 0) {
                    java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<Gifter>>(){}.getType();
                    List<Gifter> migratedGifters = GSON.fromJson(giftersArray, listType);
                    if (migratedGifters != null) {
                        currentData.gifters = migratedGifters;
                        Collections.sort(currentData.gifters);
                        
                        // Delete "gifters" from JsonObject and save config.json back
                        jsonObject.remove("gifters");
                        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
                            GSON.toJson(jsonObject, writer);
                        }
                        System.out.println("Successfully migrated gifters from config.json to data.json!");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to migrate data: " + e.getMessage());
        }
        return false;
    }

    private static void seedMockData() {
        currentData.gifters.add(new Gifter("minhtuan", "Minh Tuấn", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuASGsiqxha-Kg_fYNkmESMQuRMMRyeLWddXDrC19jQHsGcK8YbJybz4eAuDOzemzDt8k8QMtqYqFs0x8BKRutKkZmFAaxPm7NFpQC7EJHgvgeQuX3KUCSGVVb5fjP4p_7qQEQnODu6Ys1Xkw-3Sxz_4QCiH6VQYTgZwdNDXAatl3cHMNzIn9W0GC-mst7A10ip8msy16zGujR2O9zblYTQzpkd-NdT_mmj0_WctWlSyMIj1r8Jx6HRWAu2TEVUHMLCvfTeCZqOt6J4P", 
            120500));
        currentData.gifters.add(new Gifter("thuyvan", "Thúy Vân", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuASGsiqxha-Kg_fYNkmESMQuRMMRyeLWddXDrC19jQHsGcK8YbJybz4eAuDOzemzDt8k8QMtqYqFs0x8BKRutKkZmFAaxPm7NFpQC7EJHgvgeQuX3KUCSGVVb5fjP4p_7qQEQnODu6Ys1Xkw-3Sxz_4QCiH6VQYTgZwdNDXAatl3cHMNzIn9W0GC-mst7A10ip8msy16zGujR2O9zblYTQzpkd-NdT_mmj0_WctWlSyMIj1r8Jx6HRWAu2TEVUHMLCvfTeCZqOt6J4P", 
            98200));
        currentData.gifters.add(new Gifter("hoanglong", "Hoàng Long", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuASGsiqxha-Kg_fYNkmESMQuRMMRyeLWddXDrC19jQHsGcK8YbJybz4eAuDOzemzDt8k8QMtqYqFs0x8BKRutKkZmFAaxPm7NFpQC7EJHgvgeQuX3KUCSGVVb5fjP4p_7qQEQnODu6Ys1Xkw-3Sxz_4QCiH6VQYTgZwdNDXAatl3cHMNzIn9W0GC-mst7A10ip8msy16zGujR2O9zblYTQzpkd-NdT_mmj0_WctWlSyMIj1r8Jx6HRWAu2TEVUHMLCvfTeCZqOt6J4P", 
            75000));
        Collections.sort(currentData.gifters);

        currentData.likers.add(new Liker("nguyenvan_a", "Nguyễn Văn A", null, 1500));
        currentData.likers.add(new Liker("hoangthi_b", "Hoàng Thị B", null, 980));
        currentData.likers.add(new Liker("tranvan_c", "Trần Văn C", null, 420));
        Collections.sort(currentData.likers);
    }
}
