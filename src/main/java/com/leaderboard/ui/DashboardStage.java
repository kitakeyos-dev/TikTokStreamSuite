package com.leaderboard.ui;

import com.leaderboard.service.TikTokConnector;
import com.leaderboard.ui.overlay.GiftLeaderboardOverlay;
import com.leaderboard.ui.overlay.LikeGoalOverlay;
import com.leaderboard.ui.overlay.LiveChatOverlay;
import com.leaderboard.ui.overlay.TopLikeOverlay;
import com.leaderboard.ui.tab.*;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.DataManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.LocalTime;
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
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public DashboardStage() {
        setTitle("TikTok Live Stream Suite - Bảng Điều Khiển");
        setWidth(980);
        setHeight(680);

        // Load application window icon
        try {
            java.io.InputStream imgStream = getClass().getResourceAsStream("/icons/logo.png");
            if (imgStream != null) {
                getIcons().add(new Image(imgStream));
            }
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        // Load configs and data
        ConfigManager.load();
        DataManager.load();

        // Main Layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #131313;"); // Level 0 Base Dark background

        // 1. Build Header Bar
        buildHeaderBar(root);

        // 2. Build Tabbed Pane
        initComponents(root);

        // 3. Setup TikTok Connect Event Listeners
        setupTikTokListeners();

        // 4. Update initial Overlay Stage button states
        updateOverlayButtonStates();

        Scene scene = new Scene(root);
        setScene(scene);

        // Window Closing hook
        setOnCloseRequest(e -> {
            saveInputSettings();
            TikTokConnector.disconnect();
            if (overlayStage != null) overlayStage.dispose();
            if (chatOverlayStage != null) chatOverlayStage.dispose();
            if (likeOverlayStage != null) likeOverlayStage.dispose();
            if (topLikeOverlayStage != null) topLikeOverlayStage.dispose();
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
            "-fx-font-family: 'Segoe UI', system-ui;"
        );

        lblSubtitle = new Label("Bảng điều khiển quản lý OBS Overlays & Kết nối Livestream");
        lblSubtitle.setStyle(
            "-fx-text-fill: #a1a1aa;" +
            "-fx-font-size: 11px;" +
            "-fx-font-family: 'Segoe UI', system-ui;"
        );

        titleGroup.getChildren().addAll(lblTitle, lblSubtitle);
        headerTextGroup.getChildren().addAll(iconDot, titleGroup);
        headerBar.setLeft(headerTextGroup);

        // Divider
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #27272a;");
        
        VBox headerContainer = new VBox(headerBar, divider);
        root.setTop(headerContainer);
    }

    private void initComponents(BorderPane root) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPadding(new Insets(5, 15, 15, 15));
        tabPane.setStyle("-fx-background-color: transparent;");

        // Instantiate Tabs
        overviewTab = new OverviewTab(this);
        leaderboardTab = new LeaderboardTab(this);
        teamTab = new TeamTab(this);
        chatTab = new ChatTab(this);
        likesTab = new LikesTab(this);

        // Add to TabPane
        addTab(tabPane, "Tổng Quan", overviewTab);
        addTab(tabPane, "Bảng Xếp Hạng", leaderboardTab);
        addTab(tabPane, "Thành Viên", teamTab);
        addTab(tabPane, "Trò Chuyện", chatTab);
        addTab(tabPane, "Mục Tiêu Tim", likesTab);

        root.setCenter(tabPane);
    }

    private void addTab(TabPane tabPane, String title, Pane content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tabPane.getTabs().add(tab);
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
                overviewTab.getLblSyncDiag().setStyle("-fx-text-fill: #25f4ee; -fx-font-size: 11px; -fx-font-weight: bold;");

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
            lblSubtitle.setText("Bảng điều khiển quản lý OBS Overlays & Kết nối Livestream");
        } else {
            String username = overviewTab.getUsername();
            if (username.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng nhập TikTok Username!", ButtonType.OK);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText(null);
                alert.showAndWait();
                return;
            }
            saveInputSettings();
            overviewTab.setConnectingState();

            TikTokConnector.connect(
                username,
                overviewTab.getApiKey(),
                () -> Platform.runLater(() -> {
                    overviewTab.setConnectionState(true);
                    overviewTab.updateDiagnostics(true, "42ms");
                }),
                () -> Platform.runLater(() -> {
                    overviewTab.setConnectionState(false);
                    overviewTab.updateDiagnostics(false, "--");
                    lblSubtitle.setText("Bảng điều khiển quản lý OBS Overlays & Kết nối Livestream");
                }),
                errorMsg -> Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Kết nối thất bại: " + errorMsg, ButtonType.OK);
                    alert.setTitle("Lỗi kết nối");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                    overviewTab.setConnectionState(false);
                    overviewTab.updateDiagnostics(false, "--");
                    lblSubtitle.setText("Bảng điều khiển quản lý OBS Overlays & Kết nối Livestream");
                }),
                () -> Platform.runLater(() -> {
                    leaderboardTab.refreshTableData();
                    teamTab.refreshTableData();
                    if (overlayStage != null) {
                        overlayStage.updateLeaderboard();
                    }
                })
            );
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
        if (overlayStage != null) overlayStage.setAlwaysOnTop(overviewTab.getChkLeaderboardOnTop().isSelected());
        if (chatOverlayStage != null) chatOverlayStage.setAlwaysOnTop(overviewTab.getChkChatOnTop().isSelected());
        if (likeOverlayStage != null) likeOverlayStage.setAlwaysOnTop(overviewTab.getChkLikeOnTop().isSelected());
        if (topLikeOverlayStage != null) topLikeOverlayStage.setAlwaysOnTop(overviewTab.getChkTopLikeOnTop().isSelected());
    }

    public void updateOverlayButtonStates() {
        boolean isLeaderboardOpen = (overlayStage != null);
        boolean isChatOpen = (chatOverlayStage != null);
        boolean isLikeOpen = (likeOverlayStage != null);
        boolean isTopLikeOpen = (topLikeOverlayStage != null);

        overviewTab.updateOverlayButtonStates(isLeaderboardOpen, isChatOpen, isLikeOpen, isTopLikeOpen);
        leaderboardTab.updateOverlayButtonState(isLeaderboardOpen);
        chatTab.updateOverlayButtonState(isChatOpen);
        likesTab.updateOverlayButtonStates(isLikeOpen, isTopLikeOpen);
    }

    public void updateLeaderboardOverlay() {
        if (overlayStage != null) {
            overlayStage.updateLeaderboard();
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
