package com.leaderboard.service.impl;

import com.leaderboard.service.ITikTokConnector;
import com.leaderboard.service.TikTokConnector;
import com.leaderboard.ui.Dialogs;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import com.leaderboard.model.TikTokUser;
import javafx.application.Platform;
import java.util.function.Consumer;

/**
 * Decoupled implementation of ITikTokConnector.
 * Manages active live socket connections without static state locking or business logic leaks.
 * Dispatches events consistently on the JavaFX Application Thread.
 */
public class TikTokConnectorImpl implements ITikTokConnector {
    private LiveClient liveClient;
    private boolean isConnected = false;
    private boolean isConnecting = false;

    private TikTokConnector.ChatListener chatListener;
    private TikTokConnector.LikeListener likeListener;
    private TikTokConnector.GiftListener giftListener;
    private TikTokConnector.RoomInfoListener roomInfoListener;
    private TikTokConnector.SocialListener socialListener;

    @Override
    public synchronized void setChatListener(TikTokConnector.ChatListener listener) {
        chatListener = listener;
    }

    @Override
    public synchronized void setLikeListener(TikTokConnector.LikeListener listener) {
        likeListener = listener;
    }

    @Override
    public synchronized void setGiftListener(TikTokConnector.GiftListener listener) {
        giftListener = listener;
    }

    @Override
    public synchronized void setRoomInfoListener(TikTokConnector.RoomInfoListener listener) {
        roomInfoListener = listener;
    }

    @Override
    public synchronized void setSocialListener(TikTokConnector.SocialListener listener) {
        socialListener = listener;
    }

    @Override
    public synchronized boolean isConnected() {
        return isConnected;
    }

    @Override
    public synchronized boolean isConnecting() {
        return isConnecting;
    }

    @Override
    public synchronized void connect(String username, String apiKey,
                                     Runnable onConnected,
                                     Runnable onDisconnected,
                                     Consumer<String> onError,
                                     Runnable onDataChanged) {
        if (isConnected() || isConnecting) {
            return;
        }

        isConnecting = true;

        new Thread(() -> {
            try {
                liveClient = TikTokLive.newClient(username)
                        .configure(settings -> {
                            if (apiKey != null && !apiKey.trim().isEmpty()) {
                                settings.setApiKey(apiKey.trim());
                                settings.setUseEulerstreamWebsocket(true);
                            }
                            settings.setClientLanguage("vi_VN");
                        })
                        .onConnected((client, event) -> {
                            synchronized (this) {
                                isConnecting = false;
                                isConnected = true;
                            }
                            Platform.runLater(onConnected);
                        })
                        .onDisconnected((client, event) -> {
                            synchronized (this) {
                                isConnecting = false;
                                isConnected = false;
                            }
                            Platform.runLater(onDisconnected);
                        })
                        .onLiveEnded((client, event) -> {
                            Platform.runLater(() -> {
                                Dialogs.info(null, "Livestream Kết thúc",
                                    "Buổi phát sóng trực tiếp trên TikTok đã kết thúc!");
                                disconnect();
                            });
                        })
                        .onError((client, event) -> {
                            synchronized (this) {
                                isConnecting = false;
                            }
                            String errorMsg = event.getException().getMessage();
                            Platform.runLater(() -> onError.accept(errorMsg != null ? errorMsg : "Connection failed."));
                        })
                        .onComment((client, event) -> {
                            TikTokUser user = TikTokUser.fromSDK(event.getUser());
                            String comment = event.getText();

                            Platform.runLater(() -> {
                                synchronized (this) {
                                    if (chatListener != null) {
                                        chatListener.onNewComment(user, comment);
                                    }
                                }
                                onDataChanged.run();
                            });
                        })
                        .onLike((client, event) -> {
                            TikTokUser user = TikTokUser.fromSDK(event.getUser());
                            int likesSent = event.getLikes();
                            int totalLikes = event.getTotalLikes();

                            Platform.runLater(() -> {
                                synchronized (this) {
                                    if (likeListener != null) {
                                        likeListener.onNewLike(user, likesSent, totalLikes);
                                    }
                                }
                                onDataChanged.run();
                            });
                        })
                        .onRoomInfo((client, event) -> {
                            LiveRoomInfo roomInfo = event.getRoomInfo();
                            String title = roomInfo.getTitle();
                            int viewers = roomInfo.getViewersCount();
                            int likes = roomInfo.getLikesCount();

                            Platform.runLater(() -> {
                                synchronized (this) {
                                    if (roomInfoListener != null) {
                                        roomInfoListener.onRoomInfoUpdate(title, viewers, likes);
                                    }
                                }
                            });
                        })
                        .onGift((client, event) -> {
                            TikTokUser user = TikTokUser.fromSDK(event.getUser());
                            String giftName = event.getGift().getName();
                            int diamonds = event.getGift().getDiamondCost() * event.getCombo();
                            if (diamonds <= 0) {
                                diamonds = 1;
                            }

                            final int finalDiamonds = diamonds;
                            Platform.runLater(() -> {
                                synchronized (this) {
                                    if (giftListener != null) {
                                        giftListener.onNewGift(user, giftName, finalDiamonds);
                                    }
                                }
                                onDataChanged.run();
                            });
                        })
                        .onJoin((client, event) -> {
                            TikTokUser user = TikTokUser.fromSDK(event.getUser());
                            Platform.runLater(() -> {
                                synchronized (this) {
                                    if (socialListener != null) {
                                        socialListener.onSocialEvent("JOIN", user);
                                    }
                                }
                                onDataChanged.run();
                            });
                        })
                        .onFollow((client, event) -> {
                            TikTokUser user = TikTokUser.fromSDK(event.getUser());
                            Platform.runLater(() -> {
                                synchronized (this) {
                                    if (socialListener != null) {
                                        socialListener.onSocialEvent("FOLLOW", user);
                                    }
                                }
                                onDataChanged.run();
                            });
                        })
                        .onShare((client, event) -> {
                            TikTokUser user = TikTokUser.fromSDK(event.getUser());
                            Platform.runLater(() -> {
                                synchronized (this) {
                                    if (socialListener != null) {
                                        socialListener.onSocialEvent("SHARE", user);
                                    }
                                }
                                onDataChanged.run();
                            });
                        })
                        .onSubscribe((client, event) -> {
                            TikTokUser user = TikTokUser.fromSDK(event.getUser());
                            Platform.runLater(() -> {
                                synchronized (this) {
                                    if (socialListener != null) {
                                        socialListener.onSocialEvent("SUBSCRIBE", user);
                                    }
                                }
                                onDataChanged.run();
                            });
                        })
                        .build();

                liveClient.connect();
            } catch (Exception e) {
                synchronized (this) {
                    isConnecting = false;
                    isConnected = false;
                }
                Platform.runLater(() -> onError.accept(e.getMessage()));
            }
        }).start();
    }

    @Override
    public synchronized void disconnect() {
        if (liveClient != null) {
            new Thread(() -> {
                try {
                    liveClient.disconnect();
                } catch (Exception e) {
                    // Ignore
                } finally {
                    synchronized (this) {
                        liveClient = null;
                        isConnecting = false;
                        isConnected = false;
                    }
                }
            }).start();
        }
    }
}
