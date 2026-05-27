package com.leaderboard.service;

import com.leaderboard.ui.Dialogs;
import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.models.badges.Badge;
import io.github.jwdeveloper.tiktok.data.models.badges.CombineBadge;
import io.github.jwdeveloper.tiktok.data.models.badges.TextBadge;
import io.github.jwdeveloper.tiktok.data.models.badges.StringBadge;
import com.leaderboard.model.Gifter;
import com.leaderboard.util.DataManager;
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Object instance implementation of ITikTokConnector.
 * Manages active live sockets without static state locking.
 */
public class TikTokConnectorImpl implements ITikTokConnector {
    private LiveClient liveClient;
    private boolean isConnected = false;
    private boolean isConnecting = false;

    private TikTokConnector.ChatListener chatListener;
    private TikTokConnector.LikeListener likeListener;
    private TikTokConnector.RoomInfoListener roomInfoListener;

    @Override
    public synchronized void setChatListener(TikTokConnector.ChatListener listener) {
        chatListener = listener;
    }

    @Override
    public synchronized void setLikeListener(TikTokConnector.LikeListener listener) {
        likeListener = listener;
    }

    @Override
    public synchronized void setRoomInfoListener(TikTokConnector.RoomInfoListener listener) {
        roomInfoListener = listener;
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
                            }
                            settings.setClientLanguage("vi_VN");
                        })
                        .onConnected((client, event) -> {
                            synchronized (this) {
                                isConnecting = false;
                                isConnected = true;
                            }
                            SwingUtilities.invokeLater(onConnected);
                        })
                        .onDisconnected((client, event) -> {
                            synchronized (this) {
                                isConnecting = false;
                                isConnected = false;
                            }
                            SwingUtilities.invokeLater(onDisconnected);
                        })
                        .onLiveEnded((client, event) -> {
                            Platform.runLater(() -> {
                                Dialogs.info(null, "Livestream Kết thúc",
                                    "Buổi phát sóng trực tiếp trên TikTok đã kết thúc!");
                            });
                            disconnect();
                        })
                        .onError((client, event) -> {
                            synchronized (this) {
                                isConnecting = false;
                            }
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
                            com.leaderboard.service.action.ActionRulesEngine.handleComment(userId, nickname, comment);
                            SwingUtilities.invokeLater(() -> {
                                synchronized (this) {
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
                            int likesSent = event.getLikes();
                            int totalLikes = event.getTotalLikes();

                            String avatarUrl = null;
                            if (event.getUser().getPicture() != null) {
                                avatarUrl = event.getUser().getPicture().getLink();
                            }

                            processUserForTeam(event.getUser());
                            final String finalAvatarUrl = avatarUrl;
                            SwingUtilities.invokeLater(() -> {
                                synchronized (this) {
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
                                synchronized (this) {
                                    if (roomInfoListener != null) {
                                        roomInfoListener.onRoomInfoUpdate(title, viewers, likes);
                                    }
                                }
                            });
                        })
                        .onGift((client, event) -> {
                            processUserForTeam(event.getUser());
                            String userId = event.getUser().getName();
                            String nickname = event.getUser().getProfileName();
                            String avatarUrl = null;
                            if (event.getUser().getPicture() != null) {
                                avatarUrl = event.getUser().getPicture().getLink();
                            }

                            int diamonds = event.getGift().getDiamondCost() * event.getCombo();
                            if (diamonds <= 0) {
                                diamonds = 1;
                            }

                            final int pointsToAdd = diamonds;
                            final String finalAvatarUrl = avatarUrl;
                            final String finalNickname = nickname;

                            com.leaderboard.service.action.ActionRulesEngine.handleGift(userId, nickname, event.getGift().getName(), diamonds);

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

                                    Collections.sort(list);
                                    DataManager.save();
                                }
                                onDataChanged.run();
                            });
                        })
                        .onJoin((client, event) -> {
                            processUserForTeam(event.getUser());
                            SwingUtilities.invokeLater(onDataChanged);
                        })
                        .onFollow((client, event) -> {
                            processUserForTeam(event.getUser());
                            com.leaderboard.service.action.ActionRulesEngine.handleFollow(event.getUser().getName(), event.getUser().getProfileName());
                            SwingUtilities.invokeLater(onDataChanged);
                        })
                        .onShare((client, event) -> {
                            processUserForTeam(event.getUser());
                            com.leaderboard.service.action.ActionRulesEngine.handleShare(event.getUser().getName(), event.getUser().getProfileName());
                            SwingUtilities.invokeLater(onDataChanged);
                        })
                        .onSubscribe((client, event) -> {
                            processUserForTeam(event.getUser());
                            com.leaderboard.service.action.ActionRulesEngine.handleSubscribe(event.getUser().getName(), event.getUser().getProfileName());
                            SwingUtilities.invokeLater(onDataChanged);
                        })
                        .build();

                liveClient.connect();
            } catch (Exception e) {
                synchronized (this) {
                    isConnecting = false;
                    isConnected = false;
                }
                SwingUtilities.invokeLater(() -> onError.accept(e.getMessage()));
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

    private void processUserForTeam(User user) {
        if (user == null) return;
        
        boolean isSubscriber = user.isSubscriber();
        String teamName = null;
        int teamLevel = 0;
        int giftGiverLevel = 0;

        if (user.getBadges() != null) {
            for (Badge badge : user.getBadges()) {
                if (badge instanceof CombineBadge cb) {
                    String picLink = (cb.getPicture() != null) ? cb.getPicture().getLink() : "";
                    if (picLink != null && picLink.contains("fans_badge_icon")) {
                        if (teamName == null) {
                            if (cb.getSubText() != null && !cb.getSubText().trim().isEmpty()) {
                                teamName = cb.getSubText().trim();
                            } else if (cb.getText() != null && !cb.getText().trim().isEmpty()) {
                                teamName = cb.getText().trim();
                            }
                        }
                        
                        if (teamLevel == 0) {
                            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("fans_badge_icon_lv(\\d+)").matcher(picLink);
                            if (matcher.find()) {
                                teamLevel = Integer.parseInt(matcher.group(1));
                            }
                        }
                    } else {
                        if (cb.getSubText() != null && cb.getSubText().matches("\\d+")) {
                            giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(cb.getSubText()));
                        } else if (cb.getText() != null && cb.getText().matches("\\d+")) {
                            giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(cb.getText()));
                        }
                    }
                } else if (badge instanceof TextBadge tb) {
                    if (tb.getText() != null && tb.getText().matches("\\d+")) {
                        giftGiverLevel = Math.max(giftGiverLevel, Integer.parseInt(tb.getText()));
                    }
                } else if (badge instanceof StringBadge sb) {
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
