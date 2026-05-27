package com.leaderboard.service;

import java.util.function.Consumer;

/**
 * Service Facade for TikTokConnector that routes static connection calls
 * directly to the active ITikTokConnector instance registered in ServiceLocator.
 * Maintains backwards compatibility and decoupling across the entire codebase.
 */
public class TikTokConnector {
    public interface ChatListener {
        void onNewComment(String uniqueId, String nickname, String comment, String avatarUrl);
    }

    public interface LikeListener {
        void onNewLike(String uniqueId, String nickname, int likeCount, int totalLikes, String avatarUrl);
    }

    public interface RoomInfoListener {
        void onRoomInfoUpdate(String title, int viewersCount, int totalLikes);
    }

    private static ITikTokConnector getService() {
        return ServiceLocator.get(ITikTokConnector.class);
    }

    public static void setChatListener(ChatListener listener) {
        getService().setChatListener(listener);
    }

    public static void setLikeListener(LikeListener listener) {
        getService().setLikeListener(listener);
    }

    public static void setRoomInfoListener(RoomInfoListener listener) {
        getService().setRoomInfoListener(listener);
    }

    public static boolean isConnected() {
        return getService().isConnected();
    }

    public static boolean isConnecting() {
        return getService().isConnecting();
    }

    public static void connect(String username, String apiKey,
                               Runnable onConnected,
                               Runnable onDisconnected,
                               Consumer<String> onError,
                               Runnable onDataChanged) {
        getService().connect(username, apiKey, onConnected, onDisconnected, onError, onDataChanged);
    }

    public static void disconnect() {
        getService().disconnect();
    }
}
