package com.leaderboard.service;

import com.leaderboard.model.TikTokUser;
import java.util.function.Consumer;

/**
 * Service Facade for TikTokConnector that routes static connection calls
 * directly to the active ITikTokConnector instance registered in ServiceLocator.
 * Maintains decoupling across the entire codebase.
 */
public class TikTokConnector {
    public interface ChatListener {
        void onNewComment(TikTokUser user, String comment);
    }

    public interface LikeListener {
        void onNewLike(TikTokUser user, int likesSent, int totalLikes);
    }

    public interface RoomInfoListener {
        void onRoomInfoUpdate(String title, int viewersCount, int totalLikes);
    }

    public interface GiftListener {
        void onNewGift(TikTokUser user, String giftName, int diamonds);
    }

    public interface SocialListener {
        void onSocialEvent(String eventType, TikTokUser user);
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

    public static void setGiftListener(GiftListener listener) {
        getService().setGiftListener(listener);
    }

    public static void setRoomInfoListener(RoomInfoListener listener) {
        getService().setRoomInfoListener(listener);
    }

    public static void setSocialListener(SocialListener listener) {
        getService().setSocialListener(listener);
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
