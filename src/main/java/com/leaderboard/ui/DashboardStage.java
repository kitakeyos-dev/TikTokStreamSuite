package com.leaderboard.ui;

import com.leaderboard.service.TikTokConnector;
import com.leaderboard.service.StreamSessionMediator;
import com.leaderboard.service.ServiceLocator;
import com.leaderboard.service.ITTSService;
import com.leaderboard.service.TTSServiceImpl;
import com.leaderboard.service.ITikTokConnector;
import com.leaderboard.service.TikTokConnectorImpl;
import com.leaderboard.ui.tab.*;
import com.leaderboard.util.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Decoupled DashboardStage that focuses strictly on Main UI layout,
 * sidebar navigation, and visual styling. It delegates connection logic
 * and OBS overlay stage management to StreamSessionMediator.
 */
public class DashboardStage extends Stage {
    private OverviewTab overviewTab;
    private OverlaysTab overlaysTab;
    private LeaderboardTab leaderboardTab;
    private TeamTab teamTab;
    private ChatTab chatTab;
    private LikesTab likesTab;
    private TtsTab ttsTab;
    private ActionsEventsTab actionsEventsTab;

    // Coordinator Mediator
    private final StreamSessionMediator mediator;

    private Label lblSubtitle;
    private Label lblPageTitle;
    private StackPane contentArea;
    private Button activeNavButton;

    public DashboardStage() {
        setTitle("TikTok Live Stream Suite - " + I18n.get("nav.overview"));
        setWidth(1200);
        setHeight(850);

        // Load application window icon
        IconManager.applyAppIcon(this);

        // Register Core Services to ServiceLocator
        ServiceLocator.register(ITTSService.class, new TTSServiceImpl());
        ServiceLocator.register(ITikTokConnector.class, new TikTokConnectorImpl());

        // Initialize Mediator
        mediator = new StreamSessionMediator(this);

        // Main Layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #131313;"); // Level 0 Base Dark background

        // 1. Build Header Bar
        buildHeaderBar(root);

        // 2. Build sidebar dashboard layout
        initComponents(root);

        // 3. Setup TikTok Connect Event Listeners via Mediator
        mediator.setupTikTokListeners();

        // 4. Update initial Overlay Stage button states
        mediator.updateOverlayButtonStates();

        Scene scene = new Scene(root);
        var dashboardCss = getClass().getResource("/css/dashboard.css");
        if (dashboardCss != null) {
            scene.getStylesheets().add(dashboardCss.toExternalForm());
        }
        setScene(scene);

        // Window Closing hook
        setOnCloseRequest(e -> {
            mediator.saveInputSettings();
            TikTokConnector.disconnect();
            mediator.disposeAllOverlays();
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
        Label lblTitle = new Label(I18n.get("header.title"));
        lblTitle.setStyle(
                "-fx-text-fill: #f4f4f5;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-family: 'Segoe UI', system-ui;");

        lblSubtitle = new Label(I18n.get("header.subtitle"));
        lblSubtitle.setStyle(
                "-fx-text-fill: #a1a1aa;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-family: 'Segoe UI', system-ui;");

        titleGroup.getChildren().addAll(lblTitle, lblSubtitle);
        headerTextGroup.getChildren().addAll(iconDot, titleGroup);
        headerBar.setLeft(headerTextGroup);

        lblPageTitle = new Label(I18n.get("nav.overview"));
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
        overlaysTab = new OverlaysTab(this);
        leaderboardTab = new LeaderboardTab(this);
        teamTab = new TeamTab(this);
        chatTab = new ChatTab(this);
        likesTab = new LikesTab(this);
        ttsTab = new TtsTab(this);
        actionsEventsTab = new ActionsEventsTab(this);

        // Register Tabs within the mediator for event broadcasting
        mediator.registerTabs(overviewTab, overlaysTab, leaderboardTab, teamTab, chatTab, likesTab, ttsTab, actionsEventsTab);

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(0, 0, 0, 0));
        contentArea.getChildren().addAll(
                overviewTab, overlaysTab, leaderboardTab, teamTab, chatTab, likesTab, ttsTab, actionsEventsTab);

        VBox sidebar = buildSidebar();
        HBox dashboardBody = new HBox(0, sidebar, contentArea);
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        dashboardBody.setPadding(new Insets(0, 0, 0, 0));

        root.setCenter(dashboardBody);
        selectNav(activeNavButton, overviewTab, I18n.get("nav.overview"), I18n.get("nav.overview.desc"));
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(230);
        sidebar.setMinWidth(230);
        sidebar.setMaxWidth(230);
        sidebar.setPadding(new Insets(12, 10, 12, 16));
        sidebar.setStyle(
                "-fx-background-color: #0c0c0e;" +
                        "-fx-border-color: #27272a;" +
                        "-fx-border-width: 0 1 0 0;");

        sidebar.getChildren().addAll(
                createCategoryHeader(I18n.get("nav.cat.setup")),
                createNavButton(I18n.get("nav.overview"), Feather.HOME, overviewTab,
                        I18n.get("nav.overview.desc")),

                createCategoryHeader(I18n.get("nav.cat.interaction")),
                createNavButton(I18n.get("nav.chat"), Feather.MESSAGE_CIRCLE, chatTab,
                        I18n.get("nav.chat.desc")),
                createNavButton(I18n.get("nav.tts"), Feather.VOLUME_2, ttsTab,
                        I18n.get("nav.tts.desc")),
                createNavButton(I18n.get("nav.actionsEvents"), Feather.SLIDERS, actionsEventsTab,
                        I18n.get("nav.actionsEvents.desc")),

                createCategoryHeader(I18n.get("nav.cat.analytics")),
                createNavButton(I18n.get("nav.leaderboard"), Feather.BAR_CHART_2, leaderboardTab,
                        I18n.get("nav.leaderboard.desc")),
                createNavButton(I18n.get("nav.likes"), Feather.HEART, likesTab,
                        I18n.get("nav.likes.desc")),
                createNavButton(I18n.get("nav.members"), Feather.USERS, teamTab,
                        I18n.get("nav.members.desc")),

                createCategoryHeader(I18n.get("nav.cat.overlays")),
                createNavButton(I18n.get("nav.overlays"), Feather.MONITOR, overlaysTab,
                        I18n.get("nav.overlays.desc"))
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        return sidebar;
    }

    private VBox createCategoryHeader(String title) {
        VBox box = new VBox();
        box.setPadding(new Insets(10, 0, 4, 8));
        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: #52525b; -fx-font-size: 9.5px; -fx-font-weight: bold; -fx-letter-spacing: 0.5px;");
        box.getChildren().add(lbl);
        return box;
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

    // --- Delegate Methods routing requests to StreamSessionMediator ---

    public StreamSessionMediator getMediator() {
        return mediator;
    }

    public Label getLblSubtitle() {
        return lblSubtitle;
    }

    public void toggleConnection() {
        mediator.toggleConnection();
    }

    public void toggleOverlayWindow() {
        mediator.toggleOverlayWindow();
    }

    public void toggleChatOverlayWindow() {
        mediator.toggleChatOverlayWindow();
    }

    public void toggleLikeOverlayWindow() {
        mediator.toggleLikeOverlayWindow();
    }

    public void toggleTopLikeOverlayWindow() {
        mediator.toggleTopLikeOverlayWindow();
    }

    public void updateOverlayAlwaysOnTop() {
        mediator.updateOverlayAlwaysOnTop();
    }

    public void updateOverlayButtonStates() {
        mediator.updateOverlayButtonStates();
    }

    public void updateLeaderboardOverlay() {
        mediator.updateLeaderboardOverlay();
    }

    public void updateTopLikeOverlay() {
        mediator.updateTopLikeOverlay();
    }

    public void updateLikeTargetOverlay(int target) {
        mediator.updateLikeTargetOverlay(target);
    }

    public void resetLikesOverlay() {
        mediator.resetLikesOverlay();
    }
}
