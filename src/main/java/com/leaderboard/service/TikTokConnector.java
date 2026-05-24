package com.leaderboard.service;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomInfoEvent;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.models.badges.Badge;
import io.github.jwdeveloper.tiktok.data.models.badges.CombineBadge;
import io.github.jwdeveloper.tiktok.data.models.badges.TextBadge;
import io.github.jwdeveloper.tiktok.data.models.badges.StringBadge;
import com.leaderboard.model.Gifter;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.DataManager;

import javax.swing.SwingUtilities;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TikTokConnector {
    private static LiveClient liveClient;
    private static boolean isConnected = false;
    private static boolean isConnecting = false;

    public interface ChatListener {
        void onNewComment(String uniqueId, String nickname, String comment, String avatarUrl);
    }

    public interface LikeListener {
        void onNewLike(String uniqueId, String nickname, int likeCount, int totalLikes, String avatarUrl);
    }

    public interface RoomInfoListener {
        void onRoomInfoUpdate(String title, int viewersCount, int totalLikes);
    }

    private static ChatListener chatListener;
    private static LikeListener likeListener;
    private static RoomInfoListener roomInfoListener;

    public static synchronized void setChatListener(ChatListener listener) {
        chatListener = listener;
    }

    public static synchronized void setLikeListener(LikeListener listener) {
        likeListener = listener;
    }

    public static synchronized void setRoomInfoListener(RoomInfoListener listener) {
        roomInfoListener = listener;
    }

    public static synchronized boolean isConnected() {
        return isConnected;
    }

    public static synchronized boolean isConnecting() {
        return isConnecting;
    }

    public static synchronized void connect(String username, String apiKey,
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
                // Initialize building TikTokLive client
                liveClient = TikTokLive.newClient(username)
                        .configure(settings -> {
                            if (apiKey != null && !apiKey.trim().isEmpty()) {
                                settings.setApiKey(apiKey.trim());
                            }
                            settings.setClientLanguage("vi_VN"); // display localized gift events
                        })
                        .onConnected((client, event) -> {
                            isConnecting = false;
                            isConnected = true;
                            SwingUtilities.invokeLater(onConnected);
                        })
                        .onDisconnected((client, event) -> {
                            isConnecting = false;
                            isConnected = false;
                            SwingUtilities.invokeLater(onDisconnected);
                        })
                        .onError((client, event) -> {
                            isConnecting = false;
                            String errorMsg = event.getException().getMessage();
                            SwingUtilities.invokeLater(
                                    () -> onError.accept(errorMsg != null ? errorMsg : "Connection failed."));
                        })
                        .onComment((client, event) -> {
                            String userId = event.getUser().getName();
                            String nickname = event.getUser().getProfileName();
                            String comment = event.getText();
                            String avatarUrl = null;
                            if (event.getUser().getPicture() != null) {
                                avatarUrl = event.getUser().getPicture().getLink();
                            }

                            final String finalAvatarUrl = avatarUrl;
                            processUserForTeam(event.getUser());
                            SwingUtilities.invokeLater(() -> {
                                synchronized (TikTokConnector.class) {
                                    if (chatListener != null) {
                                        chatListener.onNewComment(userId, nickname, comment, finalAvatarUrl);
                                    }
                                }
                                onDataChanged.run();
                            });
                        })
                        .onLike((client, event) -> {
                            String userId = event.getUser().getName();
                            String nickname = event.getUser().getProfileName();
                            int likesSent = event.getLikes(); // Use real likes chunk count
                            int totalLikes = event.getTotalLikes();

                            String avatarUrl = null;
                            if (event.getUser().getPicture() != null) {
                                avatarUrl = event.getUser().getPicture().getLink();
                            }

                            processUserForTeam(event.getUser());
                            final String finalAvatarUrl = avatarUrl;
                            SwingUtilities.invokeLater(() -> {
                                synchronized (TikTokConnector.class) {
                                    if (likeListener != null) {
                                        likeListener.onNewLike(userId, nickname, likesSent, totalLikes, finalAvatarUrl);
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
                            SwingUtilities.invokeLater(() -> {
                                synchronized (TikTokConnector.class) {
                                    if (roomInfoListener != null) {
                                        roomInfoListener.onRoomInfoUpdate(title, viewers, likes);
                                    }
                                }
                            });
                        })
                        .onGift((client, event) -> {
                            processUserForTeam(event.getUser());
                            // Extract gift details
                            String userId = event.getUser().getName(); // unique handle/username (e.g. macgikhot)
                            String nickname = event.getUser().getProfileName(); // display nickname (e.g. ai do🍧)
                            String avatarUrl = null;
                            if (event.getUser().getPicture() != null) {
                                avatarUrl = event.getUser().getPicture().getLink();
                            }

                            // 1 diamond = 1 point
                            int diamonds = event.getGift().getDiamondCost() * event.getCombo();
                            if (diamonds <= 0) {
                                diamonds = 1; // fallback
                            }

                            final int pointsToAdd = diamonds;
                            final String finalAvatarUrl = avatarUrl;
                            final String finalNickname = nickname;

                            SwingUtilities.invokeLater(() -> {
                                synchronized (DataManager.class) {
                                    List<Gifter> list = DataManager.getGifters();
                                    Optional<Gifter> existing = list.stream()
                                            .filter(g -> g.getUniqueId().equalsIgnoreCase(userId))
                                            .findFirst();

                                    if (existing.isPresent()) {
                                        existing.get().addPoints(pointsToAdd);
                                        if (finalAvatarUrl != null) {
                                            existing.get().setAvatarUrl(finalAvatarUrl);
                                        }
                                        if (finalNickname != null && !finalNickname.trim().isEmpty()) {
                                            existing.get().setNickname(finalNickname);
                                        }
                                    } else {
                                        list.add(new Gifter(userId, finalNickname != null ? finalNickname : userId,
                                                finalAvatarUrl, pointsToAdd));
                                    }

                                    // Sort descending
                                    Collections.sort(list);

                                    // Save changes immediately
                                    DataManager.save();
                                }
                                // Notify UI
                                onDataChanged.run();
                            });
                        })
                        .onJoin((client, event) -> {
                            processUserForTeam(event.getUser());
                            SwingUtilities.invokeLater(onDataChanged);
                        })
                        .onFollow((client, event) -> {
                            processUserForTeam(event.getUser());
                            SwingUtilities.invokeLater(onDataChanged);
                        })
                        .onShare((client, event) -> {
                            processUserForTeam(event.getUser());
                            SwingUtilities.invokeLater(onDataChanged);
                        })
                        .onSubscribe((client, event) -> {
                            processUserForTeam(event.getUser());
                            SwingUtilities.invokeLater(onDataChanged);
                        })
                        .build();

                liveClient.connect();
            } catch (Exception e) {
                isConnecting = false;
                isConnected = false;
                SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
            }
        }).start();
    }

    public static synchronized void disconnect() {
        if (liveClient != null) {
            new Thread(() -> {
                try {
                    liveClient.disconnect();
                } catch (Exception e) {
                    // Ignore disconnect issues
                } finally {
                    liveClient = null;
                    isConnecting = false;
                    isConnected = false;
                }
            }).start();
        }
    }

    private static void processUserForTeam(User user) {
        if (user == null) return;
        
        boolean isSubscriber = user.isSubscriber();
        String teamName = null;
        int teamLevel = 0;
        int giftGiverLevel = 0;

        if (user.getBadges() != null) {
            for (Badge badge : user.getBadges()) {
                if (badge instanceof CombineBadge) {
                    CombineBadge cb = (CombineBadge) badge;
                    
                    String picLink = (cb.getPicture() != null) ? cb.getPicture().getLink() : "";
                    if (picLink != null && picLink.contains("fans_badge_icon")) {
                        // Fan Club Badge!
                        if (teamName == null) {
                            if (cb.getSubText() != null && !cb.getSubText().trim().isEmpty()) {
                                teamName = cb.getSubText().trim();
                            } else if (cb.getText() != null && !cb.getText().trim().isEmpty()) {
                                teamName = cb.getText().trim();
                            }
                        }
                        
                        // Extract level from URL as fallback if exact level not found in protobuf
                        if (teamLevel == 0) {
                            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("fans_badge_icon_lv(\\d+)").matcher(picLink);
                            if (matcher.find()) {
                                teamLevel = Integer.parseInt(matcher.group(1));
                            }
                        }
                    } else {
                        // Gift Level Badge (Cấp xanh)
                        if (cb.getSubText() != null && cb.getSubText().matches("\\d+")) {
                            giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(cb.getSubText()));
                        } else if (cb.getText() != null && cb.getText().matches("\\d+")) {
                            giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(cb.getText()));
                        }
                    }
                } else if (badge instanceof TextBadge) {
                    TextBadge tb = (TextBadge) badge;
                    if (tb.getText() != null && tb.getText().matches("\\d+")) {
                        giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(tb.getText()));
                    }
                } else if (badge instanceof StringBadge) {
                    StringBadge sb = (StringBadge) badge;
                    if (sb.getText() != null && sb.getText().matches("\\d+")) {
                        giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(sb.getText()));
                    }
                }
            }
        }

        if (teamName != null || isSubscriber || giftGiverLevel > 0) {
            String avatarUrl = null;
            if (user.getPicture() != null) {
                avatarUrl = user.getPicture().getLink();
            }
            DataManager.addOrUpdateTeamMember(user.getName(), user.getProfileName(), 
                avatarUrl, teamName, teamLevel, giftGiverLevel, isSubscriber);
        }
    }
}
