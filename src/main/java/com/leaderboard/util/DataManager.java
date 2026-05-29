package com.leaderboard.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.leaderboard.model.TikTokUser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DataManager acts as our central persistent storage coordinator.
 * Maintains a single unified list of TikTokUser entities (Single Source of Truth)
 * and serializes it cleanly to data.json. Includes seamless legacy schema migration.
 */
public class DataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static File getDataFile() {
        return ConfigManager.getStorageFile("data.json");
    }

    public static class AppData {
        private List<TikTokUser> users = new ArrayList<>();

        public List<TikTokUser> getUsers() {
            if (users == null) {
                users = new ArrayList<>();
            }
            return users;
        }

        public void setUsers(List<TikTokUser> users) {
            this.users = users;
        }
    }

    private static AppData currentData = new AppData();

    static {
        load();
    }

    public static synchronized List<TikTokUser> getUsers() {
        return currentData.getUsers();
    }

    // --- Query Projections (Virtual Lists) ---

    /**
     * Projection returning all gifters sorted by points descending.
     */
    public static synchronized List<TikTokUser> getGifters() {
        return getUsers().stream()
                .filter(u -> u.getGiftPoints() > 0)
                .sorted((u1, u2) -> {
                    int diff = Integer.compare(u2.getGiftPoints(), u1.getGiftPoints());
                    if (diff != 0) return diff;
                    return u1.getNickname().compareToIgnoreCase(u2.getNickname());
                })
                .collect(Collectors.toList());
    }

    /**
     * Projection returning all likers sorted by likes count descending.
     */
    public static synchronized List<TikTokUser> getLikers() {
        return getUsers().stream()
                .filter(u -> u.getLikesSent() > 0)
                .sorted((u1, u2) -> {
                    int diff = Integer.compare(u2.getLikesSent(), u1.getLikesSent());
                    if (diff != 0) return diff;
                    return u1.getNickname().compareToIgnoreCase(u2.getNickname());
                })
                .collect(Collectors.toList());
    }

    /**
     * Projection returning all active team members/subscribers sorted by last active time descending.
     */
    public static synchronized List<TikTokUser> getTeamMembers() {
        return getUsers().stream()
                .filter(u -> u.getTeamName() != null || u.isSubscriber() || u.getGiftGiverLevel() > 0)
                .sorted((u1, u2) -> Long.compare(u2.getLastActive(), u1.getLastActive()))
                .collect(Collectors.toList());
    }

    // --- Event Processing APIs ---

    public static synchronized void addLike(TikTokUser incoming, int amount) {
        TikTokUser user = findOrCreateUser(incoming);
        user.addLikesSent(amount);
        user.setLastActive(System.currentTimeMillis());
        save();
    }

    public static synchronized void addGift(TikTokUser incoming, int diamonds) {
        TikTokUser user = findOrCreateUser(incoming);
        user.addGiftPoints(diamonds);
        user.setLastActive(System.currentTimeMillis());
        save();
    }

    public static synchronized void updateSocial(TikTokUser incoming) {
        TikTokUser user = findOrCreateUser(incoming);
        user.setLastActive(System.currentTimeMillis());
        save();
    }

    /**
     * Looks up a user by uniqueId (case-insensitive) and merges identity details.
     * Inserts new users if not present.
     */
    public static synchronized TikTokUser findOrCreateUser(TikTokUser incoming) {
        if (incoming == null) return null;
        List<TikTokUser> list = getUsers();
        Optional<TikTokUser> existing = list.stream()
                .filter(u -> u.getUniqueId().equalsIgnoreCase(incoming.getUniqueId()))
                .findFirst();

        if (existing.isPresent()) {
            TikTokUser user = existing.get();
            // Merge updated profile properties from the event
            if (incoming.getNickname() != null && !incoming.getNickname().trim().isEmpty()) {
                user.setNickname(incoming.getNickname());
            }
            if (incoming.getAvatarUrl() != null) {
                user.setAvatarUrl(incoming.getAvatarUrl());
            }
            if (incoming.getBadgeUrls() != null && !incoming.getBadgeUrls().isEmpty()) {
                user.setBadgeUrls(incoming.getBadgeUrls());
            }
            if (incoming.getTeamName() != null) {
                user.setTeamName(incoming.getTeamName());
            }
            if (incoming.getTeamLevel() > user.getTeamLevel()) {
                user.setTeamLevel(incoming.getTeamLevel());
            }
            if (incoming.getGiftGiverLevel() > user.getGiftGiverLevel()) {
                user.setGiftGiverLevel(incoming.getGiftGiverLevel());
            }
            if (incoming.isSubscriber()) {
                user.setSubscriber(true);
            }
            return user;
        } else {
            list.add(incoming);
            return incoming;
        }
    }

    // --- Loading & Saving ---

    public static synchronized void load() {
        File dataFile = getDataFile();
        if (!dataFile.exists()) {
            currentData = new AppData();
            seedMockData();
            save();
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
            JsonObject rootObj = GSON.fromJson(reader, JsonObject.class);
            if (rootObj != null) {
                if (rootObj.has("users")) {
                    currentData = GSON.fromJson(rootObj, AppData.class);
                } else {
                    // Legacy migration: merge flat Gifter, Liker, and TeamMember objects into a unified list
                    currentData = new AppData();
                    
                    // Migrate gifters
                    if (rootObj.has("gifters")) {
                        JsonArray arr = rootObj.getAsJsonArray("gifters");
                        for (JsonElement el : arr) {
                            JsonObject g = el.getAsJsonObject();
                            String uid = g.get("uniqueId").getAsString();
                            TikTokUser u = findOrCreateUserInMemory(uid);
                            u.setNickname(g.has("nickname") ? g.get("nickname").getAsString() : uid);
                            u.setAvatarUrl(g.has("avatarUrl") && !g.get("avatarUrl").isJsonNull() ? g.get("avatarUrl").getAsString() : null);
                            u.setGiftPoints(g.has("points") ? g.get("points").getAsInt() : 0);
                            if (g.has("badgeUrls")) {
                                List<String> bList = new ArrayList<>();
                                for (JsonElement b : g.getAsJsonArray("badgeUrls")) {
                                    bList.add(b.getAsString());
                                }
                                u.setBadgeUrls(bList);
                            }
                        }
                    }
                    
                    // Migrate likers
                    if (rootObj.has("likers")) {
                        JsonArray arr = rootObj.getAsJsonArray("likers");
                        for (JsonElement el : arr) {
                            JsonObject l = el.getAsJsonObject();
                            String uid = l.get("uniqueId").getAsString();
                            TikTokUser u = findOrCreateUserInMemory(uid);
                            u.setNickname(l.has("nickname") ? l.get("nickname").getAsString() : uid);
                            u.setAvatarUrl(l.has("avatarUrl") && !l.get("avatarUrl").isJsonNull() ? l.get("avatarUrl").getAsString() : null);
                            u.setLikesSent(l.has("likes") ? l.get("likes").getAsInt() : 0);
                        }
                    }
                    
                    // Migrate team members
                    if (rootObj.has("teamMembers")) {
                        JsonArray arr = rootObj.getAsJsonArray("teamMembers");
                        for (JsonElement el : arr) {
                            JsonObject m = el.getAsJsonObject();
                            String uid = m.get("uniqueId").getAsString();
                            TikTokUser u = findOrCreateUserInMemory(uid);
                            u.setNickname(m.has("nickname") ? m.get("nickname").getAsString() : uid);
                            u.setAvatarUrl(m.has("avatarUrl") && !m.get("avatarUrl").isJsonNull() ? m.get("avatarUrl").getAsString() : null);
                            u.setTeamName(m.has("teamName") && !m.get("teamName").isJsonNull() ? m.get("teamName").getAsString() : null);
                            u.setTeamLevel(m.has("teamLevel") ? m.get("teamLevel").getAsInt() : 0);
                            u.setGiftGiverLevel(m.has("giftGiverLevel") ? m.get("giftGiverLevel").getAsInt() : 0);
                            u.setSubscriber(m.has("isSubscriber") && m.get("isSubscriber").getAsBoolean());
                            u.setLastActive(m.has("lastActive") ? m.get("lastActive").getAsLong() : System.currentTimeMillis());
                        }
                    }
                    
                    save(); // Write newly migrated schema back to disk
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading data.json: " + e.getMessage());
            currentData = new AppData();
        }
    }

    private static TikTokUser findOrCreateUserInMemory(String uniqueId) {
        Optional<TikTokUser> existing = currentData.getUsers().stream()
                .filter(u -> u.getUniqueId().equalsIgnoreCase(uniqueId))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        } else {
            TikTokUser u = new TikTokUser(uniqueId, uniqueId, null);
            currentData.getUsers().add(u);
            return u;
        }
    }

    public static synchronized void save() {
        File file = getDataFile();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(currentData, writer);
        } catch (Exception e) {
            System.err.println("Error saving data.json: " + e.getMessage());
        }
    }

    private static void seedMockData() {
        TikTokUser user1 = new TikTokUser("minhtuan", "Minh Tuấn", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuASGsiqxha-Kg_fYNkmESMQuRMMRyeLWddXDrC19jQHsGcK8YbJybz4eAuDOzemzDt8k8QMtqYqFs0x8BKRutKkZmFAaxPm7NFpQC7EJHgvgeQuX3KUCSGVVb5fjP4p_7qQEQnODu6Ys1Xkw-3Sxz_4QCiH6VQYTgZwdNDXAatl3cHMNzIn9W0GC-mst7A10ip8msy16zGujR2O9zblYTQzpkd-NdT_mmj0_WctWlSyMIj1r8Jx6HRWAu2TEVUHMLCvfTeCZqOt6J4P");
        user1.setGiftPoints(120500);

        TikTokUser user2 = new TikTokUser("thuyvan", "Thúy Vân", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuASGsiqxha-Kg_fYNkmESMQuRMMRyeLWddXDrC19jQHsGcK8YbJybz4eAuDOzemzDt8k8QMtqYqFs0x8BKRutKkZmFAaxPm7NFpQC7EJHgvgeQuX3KUCSGVVb5fjP4p_7qQEQnODu6Ys1Xkw-3Sxz_4QCiH6VQYTgZwdNDXAatl3cHMNzIn9W0GC-mst7A10ip8msy16zGujR2O9zblYTQzpkd-NdT_mmj0_WctWlSyMIj1r8Jx6HRWAu2TEVUHMLCvfTeCZqOt6J4P");
        user2.setGiftPoints(98200);

        TikTokUser user3 = new TikTokUser("hoanglong", "Hoàng Long", 
            "https://lh3.googleusercontent.com/aida-public/AB6AXuASGsiqxha-Kg_fYNkmESMQuRMMRyeLWddXDrC19jQHsGcK8YbJybz4eAuDOzemzDt8k8QMtqYqFs0x8BKRutKkZmFAaxPm7NFpQC7EJHgvgeQuX3KUCSGVVb5fjP4p_7qQEQnODu6Ys1Xkw-3Sxz_4QCiH6VQYTgZwdNDXAatl3cHMNzIn9W0GC-mst7A10ip8msy16zGujR2O9zblYTQzpkd-NdT_mmj0_WctWlSyMIj1r8Jx6HRWAu2TEVUHMLCvfTeCZqOt6J4P");
        user3.setGiftPoints(75000);

        TikTokUser user4 = new TikTokUser("nguyenvan_a", "Nguyễn Văn A", null);
        user4.setLikesSent(1500);

        TikTokUser user5 = new TikTokUser("hoangthi_b", "Hoàng Thị B", null);
        user5.setLikesSent(980);

        TikTokUser user6 = new TikTokUser("tranvan_c", "Trần Văn C", null);
        user6.setLikesSent(420);

        TikTokUser user7 = new TikTokUser("hoanganh", "Hoàng Anh 👑", null, "Gia đình", 15, 28, true, new ArrayList<>());
        user7.setLastActive(System.currentTimeMillis() - 50000);

        TikTokUser user8 = new TikTokUser("linhchi", "Linh Chi 🌸", null, "Gia đình", 8, 12, false, new ArrayList<>());
        user8.setLastActive(System.currentTimeMillis() - 120000);

        TikTokUser user9 = new TikTokUser("quanghuy", "Quang Huy", null, null, 0, 35, true, new ArrayList<>());
        user9.setLastActive(System.currentTimeMillis() - 300000);

        currentData.users.add(user1);
        currentData.users.add(user2);
        currentData.users.add(user3);
        currentData.users.add(user4);
        currentData.users.add(user5);
        currentData.users.add(user6);
        currentData.users.add(user7);
        currentData.users.add(user8);
        currentData.users.add(user9);
    }
}
