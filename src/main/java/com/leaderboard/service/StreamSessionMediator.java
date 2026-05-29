package com.leaderboard.service;

import com.leaderboard.service.TikTokConnector;
import com.leaderboard.service.TTSService;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.overlay.GiftLeaderboardOverlay;
import com.leaderboard.ui.overlay.LikeGoalOverlay;
import com.leaderboard.ui.overlay.LiveChatOverlay;
import com.leaderboard.ui.overlay.TopLikeOverlay;
import com.leaderboard.ui.tab.*;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.I18n;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.model.TikTokUser;
import javafx.application.Platform;
import javafx.scene.control.Label;
import java.util.List;

/**
 * StreamSessionMediator coordinates communication between TikTok events,
 * UI tabs, and independent OBS Overlays, freeing the DashboardStage from business logic.
 */
public class StreamSessionMediator {
    private final DashboardStage dashboard;

    // UI Tabs (registered after stage initialization)
    private OverviewTab overviewTab;
    private OverlaysTab overlaysTab;
    private LeaderboardTab leaderboardTab;
    private TeamTab teamTab;
    private ChatTab chatTab;
    private LikesTab likesTab;
    private TtsTab ttsTab;
    private ActionsEventsTab actionsEventsTab;

    // Independent Stage Overlays
    private GiftLeaderboardOverlay overlayStage;
    private LiveChatOverlay chatOverlayStage;
    private LikeGoalOverlay likeOverlayStage;
    private TopLikeOverlay topLikeOverlayStage;

    private int connectionAttemptId = 0;
    private boolean connectionErrorShown = false;

    public StreamSessionMediator(DashboardStage dashboard) {
        this.dashboard = dashboard;
    }

    public void registerTabs(OverviewTab overviewTab, OverlaysTab overlaysTab, LeaderboardTab leaderboardTab,
                             TeamTab teamTab, ChatTab chatTab, LikesTab likesTab, TtsTab ttsTab, ActionsEventsTab actionsEventsTab) {
        this.overviewTab = overviewTab;
        this.overlaysTab = overlaysTab;
        this.leaderboardTab = leaderboardTab;
        this.teamTab = teamTab;
        this.chatTab = chatTab;
        this.likesTab = likesTab;
        this.ttsTab = ttsTab;
        this.actionsEventsTab = actionsEventsTab;
    }

    public void setupTikTokListeners() {
        TikTokConnector.setRoomInfoListener((title, viewers, likes) -> {
            // 1. Update Subtitle in header bar
            if (title != null && !title.trim().isEmpty()) {
                String username = ConfigManager.getConfig().getStreamerUsername();
                Label lblSubtitle = dashboard.getLblSubtitle();
                if (lblSubtitle != null) {
                    lblSubtitle.setText(I18n.get("header.session", username, title));
                }
            }

            // 2. Update diagnostics in OverviewTab
            if (overviewTab != null && overviewTab.getLblSyncDiag() != null) {
                overviewTab.getLblSyncDiag().setText(I18n.get("overview.diag.sync.viewers", viewers));
                overviewTab.getLblSyncDiag()
                        .setStyle("-fx-text-fill: #25f4ee; -fx-font-size: 11px; -fx-font-weight: bold;");
            }

            // 3. Update total likes progress
            if (likesTab != null) {
                likesTab.updateProgress(likes);
            }

            // 4. Update Like Goal Overlay Stage
            if (likeOverlayStage != null) {
                likeOverlayStage.setLikes(likes);
            }
        });

        TikTokConnector.setChatListener((user, comment) -> {
            String uniqueId = user.getUniqueId();
            String nickname = user.getNickname();
            String avatarUrl = user.getAvatarUrl();
            List<String> badgeUrls = user.getBadgeUrls();

            // Update team database
            processTeamMemberUpdate(user);

            // Handle actions engine rule triggers
            ServiceLocator.get(IActionRulesEngine.class).handleComment(uniqueId, nickname, comment);

            if (chatTab != null) {
                chatTab.addChatRow(uniqueId, nickname, comment, avatarUrl);
            }
            if (chatOverlayStage != null) {
                chatOverlayStage.addMessage(uniqueId, nickname, comment, avatarUrl, badgeUrls);
            }

            // Text-to-Speech integration
            if (ConfigManager.getConfig().isTtsEnabled()) {
                if (TTSService.isCommentAllowed(comment)) {
                    String speakText;
                    if (ConfigManager.getConfig().isTtsReadUsername()) {
                        String name = (nickname != null && !nickname.trim().isEmpty()) ? nickname.trim() : uniqueId;
                        speakText = name + " nói: " + comment;
                    } else {
                        speakText = comment;
                    }
                    TTSService.enqueueTTS(speakText);
                }
            }
        });

        TikTokConnector.setLikeListener((user, likesSent, totalLikesVal) -> {
            String uniqueId = user.getUniqueId();
            String nickname = user.getNickname();
            String avatarUrl = user.getAvatarUrl();
            List<String> badgeUrls = user.getBadgeUrls();

            // Update team database
            processTeamMemberUpdate(user);

            // Add row log
            if (likesTab != null) {
                likesTab.addLikeRow(uniqueId, nickname, likesSent);
                likesTab.updateProgress(totalLikesVal);
            }

            if (likeOverlayStage != null) {
                likeOverlayStage.setLikes(totalLikesVal);
            }

            // Update data model
            DataManager.addLike(uniqueId, nickname, avatarUrl, likesSent, badgeUrls);

            // Update Top Like Overlay Stage
            if (topLikeOverlayStage != null) {
                topLikeOverlayStage.updateLeaderboard();
            }
        });

        TikTokConnector.setGiftListener((user, giftName, diamonds) -> {
            String uniqueId = user.getUniqueId();
            String nickname = user.getNickname();
            String avatarUrl = user.getAvatarUrl();
            List<String> badgeUrls = user.getBadgeUrls();

            // Update team database
            processTeamMemberUpdate(user);

            // Handle actions rules engine
            ServiceLocator.get(IActionRulesEngine.class).handleGift(uniqueId, nickname, giftName, diamonds);

            // Save and update gifter rank
            DataManager.addOrUpdateGifter(uniqueId, nickname, avatarUrl, diamonds, badgeUrls);
        });

        TikTokConnector.setSocialListener((eventType, user) -> {
            String uniqueId = user.getUniqueId();
            String nickname = user.getNickname();

            // Update team database
            processTeamMemberUpdate(user);

            // Handle rules engine event triggers
            IActionRulesEngine rulesEngine = ServiceLocator.get(IActionRulesEngine.class);
            switch (eventType) {
                case "FOLLOW" -> rulesEngine.handleFollow(uniqueId, nickname);
                case "SHARE" -> rulesEngine.handleShare(uniqueId, nickname);
                case "SUBSCRIBE" -> rulesEngine.handleSubscribe(uniqueId, nickname);
            }
        });
    }

    private void processTeamMemberUpdate(TikTokUser user) {
        if (user == null) return;
        if (user.getTeamName() != null || user.isSubscriber() || user.getGiftGiverLevel() > 0) {
            DataManager.addOrUpdateTeamMember(
                user.getUniqueId(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getTeamName(),
                user.getTeamLevel(),
                user.getGiftGiverLevel(),
                user.isSubscriber()
            );
        }
    }

    public void toggleConnection() {
        if (TikTokConnector.isConnected()) {
            if (overviewTab != null) {
                overviewTab.setDisconnectingState();
            }
            TikTokConnector.disconnect();
            if (overviewTab != null) {
                overviewTab.updateDiagnostics(false, "--");
            }
            Label lblSubtitle = dashboard.getLblSubtitle();
            if (lblSubtitle != null) {
                lblSubtitle.setText(I18n.get("header.subtitle"));
            }
        } else {
            if (overviewTab == null) return;
            String username = overviewTab.getUsername();
            if (username.isEmpty()) {
                Dialogs.warning(dashboard, "Cảnh báo", "Vui lòng nhập TikTok Username!");
                return;
            }
            saveInputSettings();
            overviewTab.setConnectingState();
            final int attemptId = ++connectionAttemptId;
            connectionErrorShown = false;

            TikTokConnector.connect(
                    username,
                    overviewTab.getApiKey(),
                    () -> Platform.runLater(() -> {
                        if (attemptId != connectionAttemptId)
                            return;
                        overviewTab.setConnectionState(true);
                        overviewTab.updateDiagnostics(true, "42ms");
                    }),
                    () -> Platform.runLater(() -> {
                        if (attemptId != connectionAttemptId)
                            return;
                        overviewTab.setConnectionState(false);
                        overviewTab.updateDiagnostics(false, "--");
                        Label lblSubtitle = dashboard.getLblSubtitle();
                        if (lblSubtitle != null) {
                            lblSubtitle.setText(I18n.get("header.subtitle"));
                        }
                    }),
                    errorMsg -> Platform.runLater(() -> {
                        if (attemptId != connectionAttemptId || connectionErrorShown)
                            return;
                        connectionErrorShown = true;
                        Dialogs.error(dashboard, "Lỗi kết nối", "Kết nối thất bại: " + errorMsg);
                        overviewTab.setConnectionState(false);
                        overviewTab.updateDiagnostics(false, "--");
                        Label lblSubtitle = dashboard.getLblSubtitle();
                        if (lblSubtitle != null) {
                            lblSubtitle.setText(I18n.get("header.subtitle"));
                        }
                    }),
                    () -> Platform.runLater(() -> {
                        if (leaderboardTab != null) leaderboardTab.refreshTableData();
                        if (teamTab != null) teamTab.refreshTableData();
                        if (likesTab != null) likesTab.refreshLikerTableData();
                        if (overlayStage != null) {
                            overlayStage.updateLeaderboard();
                        }
                    }));
        }
    }

    public void toggleOverlayWindow() {
        if (overlayStage == null) {
            overlayStage = new GiftLeaderboardOverlay();
            overlayStage.setAlwaysOnTop(overlaysTab.getChkLeaderboardOnTop().isSelected());
            overlayStage.show();
        } else {
            overlayStage.dispose();
            overlayStage = null;
        }
        updateOverlayButtonStates();
    }

    public void toggleChatOverlayWindow() {
        if (chatOverlayStage == null) {
            chatOverlayStage = new LiveChatOverlay();
            chatOverlayStage.setAlwaysOnTop(overlaysTab.getChkChatOnTop().isSelected());
            chatOverlayStage.show();
        } else {
            chatOverlayStage.dispose();
            chatOverlayStage = null;
        }
        updateOverlayButtonStates();
    }

    public void toggleLikeOverlayWindow() {
        if (likeOverlayStage == null) {
            likeOverlayStage = new LikeGoalOverlay();
            likeOverlayStage.setAlwaysOnTop(overlaysTab.getChkLikeOnTop().isSelected());
            likeOverlayStage.show();
        } else {
            likeOverlayStage.dispose();
            likeOverlayStage = null;
        }
        updateOverlayButtonStates();
    }

    public void toggleTopLikeOverlayWindow() {
        if (topLikeOverlayStage == null) {
            topLikeOverlayStage = new TopLikeOverlay();
            topLikeOverlayStage.setAlwaysOnTop(overlaysTab.getChkTopLikeOnTop().isSelected());
            topLikeOverlayStage.show();
        } else {
            topLikeOverlayStage.dispose();
            topLikeOverlayStage = null;
        }
        updateOverlayButtonStates();
    }

    public void updateOverlayAlwaysOnTop() {
        if (overlaysTab == null) return;
        if (overlayStage != null)
            overlayStage.setAlwaysOnTop(overlaysTab.getChkLeaderboardOnTop().isSelected());
        if (chatOverlayStage != null)
            chatOverlayStage.setAlwaysOnTop(overlaysTab.getChkChatOnTop().isSelected());
        if (likeOverlayStage != null)
            likeOverlayStage.setAlwaysOnTop(overlaysTab.getChkLikeOnTop().isSelected());
        if (topLikeOverlayStage != null)
            topLikeOverlayStage.setAlwaysOnTop(overlaysTab.getChkTopLikeOnTop().isSelected());
    }

    public void updateOverlayButtonStates() {
        if (overlaysTab == null) return;
        boolean isLeaderboardOpen = !(overlayStage == null || !overlayStage.isShowing());
        boolean isChatOpen = !(chatOverlayStage == null || !chatOverlayStage.isShowing());
        boolean isLikeOpen = !(likeOverlayStage == null || !likeOverlayStage.isShowing());
        boolean isTopLikeOpen = !(topLikeOverlayStage == null || !topLikeOverlayStage.isShowing());

        overlaysTab.updateOverlayButtonStates(isLeaderboardOpen, isChatOpen, isLikeOpen, isTopLikeOpen);
    }

    public void updateLeaderboardOverlay() {
        if (overlayStage != null) {
            overlayStage.updateLeaderboard();
        }
    }

    public void updateTopLikeOverlay() {
        if (topLikeOverlayStage != null) {
            topLikeOverlayStage.updateLeaderboard();
        }
    }

    public void updateLikeTargetOverlay(int target) {
        if (likeOverlayStage != null) {
            likeOverlayStage.setTargetLikes(target);
        }
    }

    public void resetLikesOverlay() {
        if (likeOverlayStage != null) {
            likeOverlayStage.setLikes(0);
        }
    }

    public void disposeAllOverlays() {
        if (overlayStage != null) overlayStage.dispose();
        if (chatOverlayStage != null) chatOverlayStage.dispose();
        if (likeOverlayStage != null) likeOverlayStage.dispose();
        if (topLikeOverlayStage != null) topLikeOverlayStage.dispose();
    }

    public void saveInputSettings() {
        if (overviewTab == null) return;
        ConfigManager.AppConfig config = ConfigManager.getConfig();
        config.setStreamerUsername(overviewTab.getUsername());
        config.setEulerstreamKey(overviewTab.getApiKey());
        ConfigManager.save();
    }
}
