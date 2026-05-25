package com.leaderboard.ui;

import com.leaderboard.service.TikTokConnector;
import com.leaderboard.ui.overlay.GiftLeaderboardOverlay;
import com.leaderboard.ui.overlay.LikeGoalOverlay;
import com.leaderboard.ui.overlay.LiveChatOverlay;
import com.leaderboard.ui.overlay.TopLikeOverlay;
import com.leaderboard.ui.tab.*;
import com.leaderboard.util.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;

public class DashboardStage extends Stage {
    private OverviewTab overviewTab;
    private LeaderboardTab leaderboardTab;
    private TeamTab teamTab;
    private ChatTab chatTab;
    private LikesTab likesTab;

    private GiftLeaderboardOverlay overlayStage;
    private LiveChatOverlay chatOverlayStage;
    private LikeGoalOverlay likeOverlayStage;
    private TopLikeOverlay topLikeOverlayStage;

    private Label lblSubtitle;
    private Label lblPageTitle;
    private StackPane contentArea;
    private Button activeNavButton;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private int connectionAttemptId = 0;
    private boolean connectionErrorShown = false;

    public DashboardStage() {
        setTitle("TikTok Live Stream Suite - Bảng Điều Khiển");
        setWidth(1200);
        setHeight(850);

        // Load application window icon
        IconManager.applyAppIcon(this);

        // Configs and data are pre-loaded asynchronously during the SplashScreen phase!

        // Main Layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #131313;"); // Level 0 Base Dark background

        // 1. Build Header Bar
        buildHeaderBar(root);

        // 2. Build sidebar dashboard layout
        initComponents(root);

        // 3. Setup TikTok Connect Event Listeners
        setupTikTokListeners();

        // 4. Update initial Overlay Stage button states
        updateOverlayButtonStates();

        Scene scene = new Scene(root);
        var dashboardCss = getClass().getResource("/css/dashboard.css");
        if (dashboardCss != null) {
            scene.getStylesheets().add(dashboardCss.toExternalForm());
        }
        setScene(scene);

        // Window Closing hook
        setOnCloseRequest(e -> {
            saveInputSettings();
            TikTokConnector.disconnect();
            if (overlayStage != null)
                overlayStage.dispose();
            if (chatOverlayStage != null)
                chatOverlayStage.dispose();
            if (likeOverlayStage != null)
                likeOverlayStage.dispose();
            if (topLikeOverlayStage != null)
                topLikeOverlayStage.dispose();
        });
    }

    private void buildHeaderBar(BorderPane root) {
        BorderPane headerBar = new BorderPane();
        headerBar.setPadding(new Insets(15, 20, 10, 20));
        headerBar.setStyle("-fx-background-color: #131313;");

        HBox headerTextGroup = new HBox(12);
        headerTextGroup.setAlignment(Pos.CENTER_LEFT);

        // Pulsing glow tech dot
        StackPane iconDot = new StackPane();
        iconDot.setPrefSize(20, 20);

        Circle pulseCircle = new Circle(10, Color.web("#25f4ee", 0.45)); // cyan glow halo
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#25f4ee"));
        glow.setRadius(5);
        pulseCircle.setEffect(glow);

        Circle coreCircle = new Circle(6, Color.web("#fe2c55")); // TikTok Pink core
        iconDot.getChildren().addAll(pulseCircle, coreCircle);

        VBox titleGroup = new VBox(2);
        Label lblTitle = new Label("LIVE STREAM SUITE");
        lblTitle.setStyle(
                "-fx-text-fill: #f4f4f5;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-family: 'Segoe UI', system-ui;");

        lblSubtitle = new Label("Kết nối livestream, overlay OBS và trạng thái hệ thống");
        lblSubtitle.setStyle(
                "-fx-text-fill: #a1a1aa;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-family: 'Segoe UI', system-ui;");

        titleGroup.getChildren().addAll(lblTitle, lblSubtitle);
        headerTextGroup.getChildren().addAll(iconDot, titleGroup);
        headerBar.setLeft(headerTextGroup);

        lblPageTitle = new Label("Tổng Quan");
        lblPageTitle.setStyle(
                "-fx-text-fill: #f4f4f5;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI', system-ui;");
        headerBar.setRight(lblPageTitle);
        BorderPane.setAlignment(lblPageTitle, Pos.CENTER_RIGHT);

        // Divider
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #27272a;");

        VBox headerContainer = new VBox(headerBar, divider);
        root.setTop(headerContainer);
    }

    private void initComponents(BorderPane root) {
        overviewTab = new OverviewTab(this);
        leaderboardTab = new LeaderboardTab(this);
        teamTab = new TeamTab(this);
        chatTab = new ChatTab(this);
        likesTab = new LikesTab(this);

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(0, 20, 20, 0));
        contentArea.getChildren().addAll(
                overviewTab, leaderboardTab, teamTab, chatTab, likesTab);

        VBox sidebar = buildSidebar();
        HBox dashboardBody = new HBox(0, sidebar, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        dashboardBody.setPadding(new Insets(0, 0, 0, 0));

        root.setCenter(dashboardBody);
        selectNav(activeNavButton, overviewTab, "Tổng Quan", "Kết nối livestream, overlay OBS và trạng thái hệ thống");
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(230);
        sidebar.setMinWidth(230);
        sidebar.setMaxWidth(230);
        sidebar.setPadding(new Insets(16, 10, 16, 16));
        sidebar.setStyle(
                "-fx-background-color: #0c0c0e;" +
                        "-fx-border-color: #27272a;" +
                        "-fx-border-width: 0 1 0 0;");

        Label lblMenu = new Label("ĐIỀU HƯỚNG");
        lblMenu.setStyle("-fx-text-fill: #52525b; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 0 0 4 8;");

        Separator menuSep = new Separator();
        menuSep.setStyle("-fx-opacity: 0.15; -fx-padding: 0 0 8 0;");

        sidebar.getChildren().addAll(
                lblMenu,
                menuSep,
                createNavButton("Tổng Quan", Feather.HOME, overviewTab,
                        "Kết nối livestream, overlay OBS và trạng thái hệ thống"),
                createNavButton("Bảng Xếp Hạng", Feather.BAR_CHART_2, leaderboardTab,
                        "Theo dõi và quản lý top quà tặng"),
                createNavButton("Mục Tiêu Tim", Feather.HEART, likesTab,
                        "Theo dõi lượt tim và mục tiêu"),
                createNavButton("Trò Chuyện", Feather.MESSAGE_CIRCLE, chatTab,
                        "Live chat và overlay tin nhắn"),
                createNavButton("Thành Viên", Feather.USERS, teamTab,
                        "Danh sách thành viên và fan club"));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        return sidebar;
    }

    private Button createNavButton(String title, Feather icon, Pane view, String subtitle) {
        FontIcon navIcon = new FontIcon(icon);
        navIcon.setIconSize(16);

        Button btn = new Button(title, navIcon);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphicTextGap(10);
        btn.setPrefHeight(40);
        btn.setUserData(view);
        applyNavStyle(btn, false);
        navIcon.setIconColor(Color.web("#71717a"));

        btn.setOnAction(e -> selectNav(btn, view, title, subtitle));
        btn.setOnMouseEntered(e -> {
            if (btn != activeNavButton) {
                btn.setStyle(
                        "-fx-background-color: rgba(255, 255, 255, 0.04);" +
                                "-fx-background-radius: 8px;" +
                                "-fx-text-fill: #e4e4e7;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: " + (int) DashboardLayout.NAV_BUTTON_FONT_SIZE + "px;" +
                                "-fx-border-color: transparent;" +
                                "-fx-border-radius: 8px;" +
                                "-fx-cursor: hand;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (btn != activeNavButton) {
                applyNavStyle(btn, false);
            }
        });

        if (activeNavButton == null) {
            activeNavButton = btn;
        }
        return btn;
    }

    private void selectNav(Button btn, Pane view, String title, String subtitle) {
        if (activeNavButton != null && activeNavButton != btn) {
            applyNavStyle(activeNavButton, false);
            FontIcon oldIcon = (FontIcon) activeNavButton.getGraphic();
            if (oldIcon != null) {
                oldIcon.setIconColor(Color.web("#71717a"));
            }
        }

        activeNavButton = btn;
        applyNavStyle(btn, true);
        FontIcon icon = (FontIcon) btn.getGraphic();
        if (icon != null) {
            icon.setIconColor(Color.web("#fe2c55"));
        }

        for (javafx.scene.Node node : contentArea.getChildren()) {
            boolean active = node == view;
            node.setVisible(active);
            node.setManaged(active);
        }

        if (lblPageTitle != null) {
            lblPageTitle.setText(title);
        }
        if (subtitle != null && !subtitle.isBlank()) {
            lblSubtitle.setText(subtitle);
        }
    }

    private void applyNavStyle(Button btn, boolean active) {
        String font = "-fx-font-size: " + (int) DashboardLayout.NAV_BUTTON_FONT_SIZE + "px;";
        if (active) {
            btn.setStyle(
                    "-fx-background-color: rgba(254, 44, 85, 0.12);" +
                            "-fx-background-radius: 8px;" +
                            "-fx-text-fill: #fe2c55;" +
                            "-fx-font-weight: bold;" +
                            font +
                            "-fx-border-color: rgba(254, 44, 85, 0.25);" +
                            "-fx-border-radius: 8px;" +
                            "-fx-border-width: 1px;" +
                            "-fx-cursor: hand;");
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-text-fill: #a1a1aa;" +
                            "-fx-font-weight: bold;" +
                            font +
                            "-fx-border-color: transparent;" +
                            "-fx-border-radius: 8px;" +
                            "-fx-cursor: hand;");
        }
    }

    private void setupTikTokListeners() {
        TikTokConnector.setRoomInfoListener((title, viewers, likes) -> {
            Platform.runLater(() -> {
                // 1. Update Subtitle in header bar
                if (title != null && !title.trim().isEmpty()) {
                    String username = ConfigManager.getConfig().getStreamerUsername();
                    lblSubtitle.setText("Phiên LIVE: @" + username + " - \"" + title + "\"");
                }

                // 2. Update diagnostics
                overviewTab.getLblSyncDiag().setText(String.format("HOẠT ĐỘNG (%,d người xem)", viewers));
                overviewTab.getLblSyncDiag()
                        .setStyle("-fx-text-fill: #25f4ee; -fx-font-size: 11px; -fx-font-weight: bold;");

                // 3. Update total likes
                likesTab.updateProgress(likes);

                // 4. Update Like Goal Overlay Stage
                if (likeOverlayStage != null) {
                    likeOverlayStage.setLikes(likes);
                }
            });
        });

        TikTokConnector.setChatListener((uniqueId, nickname, comment, avatarUrl) -> {
            Platform.runLater(() -> {
                chatTab.addChatRow(uniqueId, nickname, comment, avatarUrl);
                if (chatOverlayStage != null) {
                    chatOverlayStage.addMessage(uniqueId, nickname, comment, avatarUrl);
                }
            });
        });

        TikTokConnector.setLikeListener((uniqueId, nickname, likesSent, totalLikesVal, avatarUrl) -> {
            Platform.runLater(() -> {
                // Add row log
                likesTab.addLikeRow(uniqueId, nickname, likesSent);

                // Update Total Likes & Goal
                likesTab.updateProgress(totalLikesVal);

                if (likeOverlayStage != null) {
                    likeOverlayStage.setLikes(totalLikesVal);
                }

                // Update data model
                DataManager.addLike(uniqueId, nickname, avatarUrl, likesSent);

                // Update Top Like Overlay Stage
                if (topLikeOverlayStage != null) {
                    topLikeOverlayStage.updateLeaderboard();
                }
            });
        });
    }

    public void toggleConnection() {
        if (TikTokConnector.isConnected()) {
            overviewTab.setDisconnectingState();
            TikTokConnector.disconnect();
            overviewTab.updateDiagnostics(false, "--");
            lblSubtitle.setText("Kết nối livestream, overlay OBS và trạng thái hệ thống");
        } else {
            String username = overviewTab.getUsername();
            if (username.isEmpty()) {
                Dialogs.warning(this, "Cảnh báo", "Vui lòng nhập TikTok Username!");
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
                        lblSubtitle.setText("Kết nối livestream, overlay OBS và trạng thái hệ thống");
                    }),
                    errorMsg -> Platform.runLater(() -> {
                        if (attemptId != connectionAttemptId || connectionErrorShown)
                            return;
                        connectionErrorShown = true;
                        Dialogs.error(this, "Lỗi kết nối", "Kết nối thất bại: " + errorMsg);
                        overviewTab.setConnectionState(false);
                        overviewTab.updateDiagnostics(false, "--");
                        lblSubtitle.setText("Kết nối livestream, overlay OBS và trạng thái hệ thống");
                    }),
                    () -> Platform.runLater(() -> {
                        leaderboardTab.refreshTableData();
                        teamTab.refreshTableData();
                        likesTab.refreshLikerTableData();
                        if (overlayStage != null) {
                            overlayStage.updateLeaderboard();
                        }
                    }));
        }
    }

    public void toggleOverlayWindow() {
        if (overlayStage == null) {
            overlayStage = new GiftLeaderboardOverlay();
            overlayStage.setAlwaysOnTop(overviewTab.getChkLeaderboardOnTop().isSelected());
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
            chatOverlayStage.setAlwaysOnTop(overviewTab.getChkChatOnTop().isSelected());
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
            likeOverlayStage.setAlwaysOnTop(overviewTab.getChkLikeOnTop().isSelected());
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
            topLikeOverlayStage.setAlwaysOnTop(overviewTab.getChkTopLikeOnTop().isSelected());
            topLikeOverlayStage.show();
        } else {
            topLikeOverlayStage.dispose();
            topLikeOverlayStage = null;
        }
        updateOverlayButtonStates();
    }

    public void updateOverlayAlwaysOnTop() {
        if (overlayStage != null)
            overlayStage.setAlwaysOnTop(overviewTab.getChkLeaderboardOnTop().isSelected());
        if (chatOverlayStage != null)
            chatOverlayStage.setAlwaysOnTop(overviewTab.getChkChatOnTop().isSelected());
        if (likeOverlayStage != null)
            likeOverlayStage.setAlwaysOnTop(overviewTab.getChkLikeOnTop().isSelected());
        if (topLikeOverlayStage != null)
            topLikeOverlayStage.setAlwaysOnTop(overviewTab.getChkTopLikeOnTop().isSelected());
    }

    public void updateOverlayButtonStates() {
        boolean isLeaderboardOpen = !(overlayStage == null || !overlayStage.isShowing());
        boolean isChatOpen = !(chatOverlayStage == null || !chatOverlayStage.isShowing());
        boolean isLikeOpen = !(likeOverlayStage == null || !likeOverlayStage.isShowing());
        boolean isTopLikeOpen = !(topLikeOverlayStage == null || !topLikeOverlayStage.isShowing());

        overviewTab.updateOverlayButtonStates(isLeaderboardOpen, isChatOpen, isLikeOpen, isTopLikeOpen);
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

    private void saveInputSettings() {
        ConfigManager.AppConfig config = ConfigManager.getConfig();
        config.setStreamerUsername(overviewTab.getUsername());
        config.setEulerstreamKey(overviewTab.getApiKey());
        ConfigManager.save();
    }
}
