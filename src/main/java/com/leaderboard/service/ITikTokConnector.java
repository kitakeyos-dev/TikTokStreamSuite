package com.leaderboard.service;

import java.util.function.Consumer;

/**
 * Interface representing TikTok Live streaming connector API.
 */
public interface ITikTokConnector {
    void setChatListener(TikTokConnector.ChatListener listener);
    void setLikeListener(TikTokConnector.LikeListener listener);
    void setGiftListener(TikTokConnector.GiftListener listener);
    void setRoomInfoListener(TikTokConnector.RoomInfoListener listener);
    void setSocialListener(TikTokConnector.SocialListener listener);

    boolean isConnected();
    boolean isConnecting();

    void connect(String username, String apiKey,
                 Runnable onConnected,
                 Runnable onDisconnected,
                 Consumer<String> onError,
                 Runnable onDataChanged);

    void disconnect();
}
