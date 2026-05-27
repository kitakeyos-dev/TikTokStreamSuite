package com.leaderboard.ui.tab;

import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.ToggleSwitch;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A dedicated dashboard page for managing OBS livestream widgets and overlays.
 * Modeled after TikFinity overlay settings.
 */
public class OverlaysTab extends BorderPane {
    private final DashboardStage parent;

    private final ToggleSwitch swToggleOverlay;
    private final ToggleSwitch swToggleChatOverlay;
    private final ToggleSwitch swToggleLikeOverlay;
    private final ToggleSwitch swToggleTopLikeOverlay;

    private final CheckBox chkLeaderboardOnTop;
    private final CheckBox chkChatOnTop;
    private final CheckBox chkLikeOnTop;
    private final CheckBox chkTopLikeOnTop;

    public OverlaysTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);

        // Header Section
        HBox header = DashboardLayout.createPageHeader(
            I18n.get("nav.overlays"),
            I18n.get("nav.overlays.desc")
        );
        setTop(header);

        // 2x2 Grid of overlays
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setMaxHeight(Double.MAX_VALUE);

        // Column Constraints to make it 50% split
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Row Constraints for equal sizing
        RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.ALWAYS);
        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().addAll(row1, row2);

        // 1. Gift Leaderboard
        swToggleOverlay = DashboardLayout.newToggleSwitch();
        swToggleOverlay.setOnToggle(parent::toggleOverlayWindow);
        chkLeaderboardOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayLeaderboardOnTop());
        chkLeaderboardOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayLeaderboardOnTop(chkLeaderboardOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        VBox cardLeaderboard = createWidgetCard(
            I18n.get("overview.widget.leaderboard.title"),
            I18n.get("overview.widget.leaderboard.desc"),
            "360 x 600 px",
            "#818cf8",
            Feather.BAR_CHART_2,
            swToggleOverlay,
            chkLeaderboardOnTop
        );

        // 2. Chat Overlay
        swToggleChatOverlay = DashboardLayout.newToggleSwitch();
        swToggleChatOverlay.setOnToggle(parent::toggleChatOverlayWindow);
        chkChatOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayChatOnTop());
        chkChatOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayChatOnTop(chkChatOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        VBox cardChat = createWidgetCard(
            I18n.get("overview.widget.chat.title"),
            I18n.get("overview.widget.chat.desc"),
            "400 x 800 px",
            "#fdba74",
            Feather.MESSAGE_SQUARE,
            swToggleChatOverlay,
            chkChatOnTop
        );

        // 3. Like Goal
        swToggleLikeOverlay = DashboardLayout.newToggleSwitch();
        swToggleLikeOverlay.setOnToggle(parent::toggleLikeOverlayWindow);
        chkLikeOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayLikeOnTop());
        chkLikeOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayLikeOnTop(chkLikeOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        VBox cardLike = createWidgetCard(
            I18n.get("overview.widget.likes.title"),
            I18n.get("overview.widget.likes.desc"),
            "600 x 120 px",
            "#f87171",
            Feather.HEART,
            swToggleLikeOverlay,
            chkLikeOnTop
        );

        // 4. Top Like Overlay
        swToggleTopLikeOverlay = DashboardLayout.newToggleSwitch();
        swToggleTopLikeOverlay.setOnToggle(parent::toggleTopLikeOverlayWindow);
        chkTopLikeOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayTopLikeOnTop());
        chkTopLikeOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayTopLikeOnTop(chkTopLikeOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        VBox cardTopLike = createWidgetCard(
            I18n.get("overview.widget.toplike.title"),
            I18n.get("overview.widget.toplike.desc"),
            "360 x 500 px",
            "#4ade80",
            Feather.AWARD,
            swToggleTopLikeOverlay,
            chkTopLikeOnTop
        );

        grid.add(cardLeaderboard, 0, 0);
        grid.add(cardChat, 1, 0);
        grid.add(cardLike, 0, 1);
        grid.add(cardTopLike, 1, 1);

        DashboardLayout.fillGridCell(cardLeaderboard);
        DashboardLayout.fillGridCell(cardChat);
        DashboardLayout.fillGridCell(cardLike);
        DashboardLayout.fillGridCell(cardTopLike);

        setCenter(grid);
        BorderPane.setMargin(grid, new Insets(15, 0, 0, 0));
    }

    private CheckBox createOnTopCheckbox(boolean initialState) {
        CheckBox chk = new CheckBox(I18n.get("overview.widget.ontop"));
        chk.setSelected(initialState);
        chk.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");
        return chk;
    }

    private VBox createWidgetCard(String title, String desc, String resolution, String iconColorHex, Feather icon, ToggleSwitch sw, CheckBox onTopChk) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: #121214;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.05);" +
            "-fx-border-radius: 12px;" +
            "-fx-border-width: 1px;"
        );

        // Header Row: Icon, Title on Left, Toggle Switch on Right
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(42, 42);
        iconBox.setMinSize(42, 42);
        iconBox.setMaxSize(42, 42);
        iconBox.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.02);" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;"
        );

        Region leftBar = new Region();
        leftBar.setPrefWidth(3);
        leftBar.setMaxHeight(Double.MAX_VALUE);
        leftBar.setStyle("-fx-background-color: " + iconColorHex + "; -fx-background-radius: 3px 0 0 3px;");
        StackPane.setAlignment(leftBar, Pos.CENTER_LEFT);

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(18);
        fontIcon.setIconColor(Color.web(iconColorHex));
        iconBox.getChildren().addAll(leftBar, fontIcon);

        VBox titleGroup = new VBox(2);
        HBox.setHgrow(titleGroup, Priority.ALWAYS);
        
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #f4f4f5; -fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label lblGuide = new Label("Độ phân giải đề xuất: " + resolution);
        lblGuide.setStyle("-fx-text-fill: #71717a; -fx-font-size: 10px; -fx-font-style: italic;");
        titleGroup.getChildren().addAll(lblTitle, lblGuide);

        headerRow.getChildren().addAll(iconBox, titleGroup, sw);

        // Description
        Label lblDesc = new Label(desc);
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px;");
        lblDesc.setMinHeight(36);
        VBox.setVgrow(lblDesc, Priority.ALWAYS);

        // Separator
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.06; -fx-padding: 0;");

        // Footer Row: OnTop checkbox & Active badge
        HBox footerRow = new HBox(10);
        footerRow.setAlignment(Pos.CENTER_LEFT);
        onTopChk.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");
        HBox.setHgrow(onTopChk, Priority.ALWAYS);
        
        Label lblStateBadge = new Label("OFFLINE");
        lblStateBadge.setAlignment(Pos.CENTER);
        lblStateBadge.setPrefWidth(65);
        lblStateBadge.setPadding(new Insets(3, 8, 3, 8));
        lblStateBadge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.03);" +
            "-fx-background-radius: 4px;" +
            "-fx-text-fill: #71717a;" +
            "-fx-font-size: 9px;" +
            "-fx-font-weight: bold;"
        );

        sw.selectedProperty().addListener((obs, wasActive, isActive) -> {
            if (isActive) {
                lblStateBadge.setText("ACTIVE");
                lblStateBadge.setStyle(
                    "-fx-background-color: rgba(34, 197, 94, 0.1);" +
                    "-fx-background-radius: 4px;" +
                    "-fx-text-fill: #4ade80;" +
                    "-fx-font-size: 9px;" +
                    "-fx-font-weight: bold;"
                );
            } else {
                lblStateBadge.setText("OFFLINE");
                lblStateBadge.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.03);" +
                    "-fx-background-radius: 4px;" +
                    "-fx-text-fill: #71717a;" +
                    "-fx-font-size: 9px;" +
                    "-fx-font-weight: bold;"
                );
            }
        });

        // Initialize state
        if (sw.isSelected()) {
            lblStateBadge.setText("ACTIVE");
            lblStateBadge.setStyle(
                "-fx-background-color: rgba(34, 197, 94, 0.1);" +
                "-fx-background-radius: 4px;" +
                "-fx-text-fill: #4ade80;" +
                "-fx-font-size: 9px;" +
                "-fx-font-weight: bold;"
            );
        }

        footerRow.getChildren().addAll(onTopChk, lblStateBadge);
        card.getChildren().addAll(headerRow, lblDesc, sep, footerRow);
        return card;
    }

    public void updateOverlayButtonStates(boolean isLeaderboardOpen, boolean isChatOpen, boolean isLikeOpen, boolean isTopLikeOpen) {
        swToggleOverlay.setSelected(isLeaderboardOpen);
        swToggleChatOverlay.setSelected(isChatOpen);
        swToggleLikeOverlay.setSelected(isLikeOpen);
        swToggleTopLikeOverlay.setSelected(isTopLikeOpen);
    }

    public CheckBox getChkLeaderboardOnTop() { return chkLeaderboardOnTop; }
    public CheckBox getChkChatOnTop() { return chkChatOnTop; }
    public CheckBox getChkLikeOnTop() { return chkLikeOnTop; }
    public CheckBox getChkTopLikeOnTop() { return chkTopLikeOnTop; }
}
