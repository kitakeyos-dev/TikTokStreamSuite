package com.leaderboard.ui.tab;

import com.leaderboard.service.UpdateService;
import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.ToggleSwitch;
import com.leaderboard.util.ConfigManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

public class OverviewTab extends BorderPane {
    private final DashboardStage parent;

    private final TextField txtUsername;
    private final PasswordField txtApiKey;
    private final Label lblStatusBadge;
    private final Button btnConnect;

    private final ToggleSwitch swToggleOverlay;
    private final ToggleSwitch swToggleChatOverlay;
    private final ToggleSwitch swToggleLikeOverlay;
    private final ToggleSwitch swToggleTopLikeOverlay;

    private final CheckBox chkLeaderboardOnTop;
    private final CheckBox chkChatOnTop;
    private final CheckBox chkLikeOnTop;
    private final CheckBox chkTopLikeOnTop;

    private final Label lblWebSocketDiag;
    private final Label lblLatencyDiag;
    private final Label lblSyncDiag;

    public OverviewTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);

        GridPane grid = DashboardLayout.createTwoColumnGrid();

        VBox cardConfig = DashboardLayout.createCard("CẤU HÌNH KẾT NỐI");
        VBox leftContent = DashboardLayout.createSectionContent();

        // TikTok Username
        Label lblUser = DashboardLayout.createFieldLabel("TIKTOK USERNAME (TÊN KÊNH LIVE):");
        txtUsername = DashboardLayout.newTextField();
        txtUsername.setText(ConfigManager.getConfig().getStreamerUsername());
        Label lblAt = new Label("@");
        lblAt.setStyle("-fx-text-fill: #71717a; -fx-font-size: 14px; -fx-font-weight: bold;");
        HBox userFieldBox = DashboardLayout.wrapTextField(txtUsername, "Ví dụ: streamer_live", lblAt);

        // API Key
        Label lblKey = DashboardLayout.createFieldLabel("EULERSTREAM API KEY (TÙY CHỌN):");
        txtApiKey = DashboardLayout.newPasswordField();
        txtApiKey.setText(ConfigManager.getConfig().getEulerstreamKey());
        FontIcon keyIcon = new FontIcon(Feather.KEY);
        keyIcon.setIconColor(Color.web("#71717a"));
        HBox keyFieldBox = DashboardLayout.wrapPasswordField(txtApiKey, "Nhập API Key để livestream ổn định...", keyIcon);

        // Status Row
        lblStatusBadge = DashboardLayout.createStatusBadge("CHƯA KẾT NỐI");
        btnConnect = DashboardLayout.newButton("Kết nối LIVE");
        DashboardLayout.applyPrimaryButton(btnConnect);
        btnConnect.setOnAction(e -> parent.toggleConnection());
        HBox statusRow = DashboardLayout.createStatusRow(lblStatusBadge, btnConnect);

        // Divider
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.08; -fx-padding: 5 0 5 0;");

        // System Diagnostics Section
        Label lblDiagTitle = DashboardLayout.createFieldLabel("TRẠNG THÁI HỆ THỐNG / DIAGNOSTICS:");

        VBox diagBox = new VBox(10);
        diagBox.setPadding(new Insets(5, 10, 5, 10));

        lblWebSocketDiag = createDiagRow(diagBox, "WebSocket", "CHƯA KẾT NỐI", "#71717a");
        lblLatencyDiag = createDiagRow(diagBox, "Độ trễ kết nối (Latency)", "--", "#f4f4f5");
        lblSyncDiag = createDiagRow(diagBox, "Đồng bộ mắt xem (Viewer Sync)", "INACTIVE", "#71717a");
        createDiagRow(diagBox, "Phiên bản hiện tại", "v" + UpdateService.CURRENT_VERSION, "#818cf8");

        Button btnCheckUpdate = DashboardLayout.newButton("Kiểm tra cập nhật");
        FontIcon refreshIcon = new FontIcon(Feather.REFRESH_CW);
        refreshIcon.setIconColor(Color.web("#818cf8"));
        btnCheckUpdate.setGraphic(refreshIcon);
        DashboardLayout.applySecondaryButton(btnCheckUpdate);
        btnCheckUpdate.setMaxWidth(Double.MAX_VALUE);
        btnCheckUpdate.setOnAction(e -> UpdateService.checkForUpdates(parent.getScene().getWindow(), false));

        leftContent.getChildren().addAll(lblUser, userFieldBox, lblKey, keyFieldBox, statusRow, sep, lblDiagTitle, diagBox, btnCheckUpdate);
        cardConfig.getChildren().add(leftContent);
        grid.add(cardConfig, 0, 0);
        DashboardLayout.fillGridCell(cardConfig);

        // Column 2: Quick OBS Overlays controls (Bento)
        VBox cardWidgets = DashboardLayout.createCard("ĐIỀU KHIỂN WIDGETS OBS");
        VBox widgetsBox = new VBox(12);
        widgetsBox.setPadding(new Insets(10, 5, 10, 5));

        swToggleOverlay = DashboardLayout.newToggleSwitch();
        swToggleOverlay.setOnToggle(parent::toggleOverlayWindow);
        chkLeaderboardOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayLeaderboardOnTop());
        chkLeaderboardOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayLeaderboardOnTop(chkLeaderboardOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        widgetsBox.getChildren().add(createWidgetBento("BẢNG XẾP HẠNG XU", "Hiển thị Top nhà tài trợ và quà tặng.", "#818cf8", Feather.BAR_CHART_2, swToggleOverlay, chkLeaderboardOnTop));

        swToggleChatOverlay = DashboardLayout.newToggleSwitch();
        swToggleChatOverlay.setOnToggle(parent::toggleChatOverlayWindow);
        chkChatOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayChatOnTop());
        chkChatOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayChatOnTop(chkChatOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        widgetsBox.getChildren().add(createWidgetBento("KHUNG CHAT", "Hiển thị dòng chat game capture trực tiếp.", "#818cf8", Feather.MESSAGE_SQUARE, swToggleChatOverlay, chkChatOnTop));

        swToggleLikeOverlay = DashboardLayout.newToggleSwitch();
        swToggleLikeOverlay.setOnToggle(parent::toggleLikeOverlayWindow);
        chkLikeOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayLikeOnTop());
        chkLikeOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayLikeOnTop(chkLikeOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        widgetsBox.getChildren().add(createWidgetBento("MỤC TIÊU THẢ TIM", "Thanh tim bay lơ lửng và đếm tim.", "#818cf8", Feather.HEART, swToggleLikeOverlay, chkLikeOnTop));

        swToggleTopLikeOverlay = DashboardLayout.newToggleSwitch();
        swToggleTopLikeOverlay.setOnToggle(parent::toggleTopLikeOverlayWindow);
        chkTopLikeOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayTopLikeOnTop());
        chkTopLikeOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayTopLikeOnTop(chkTopLikeOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        widgetsBox.getChildren().add(createWidgetBento("BẢNG XẾP HẠNG THẢ TIM", "Bảng xếp hạng thả tim thời gian thực.", "#818cf8", Feather.AWARD, swToggleTopLikeOverlay, chkTopLikeOnTop));

        cardWidgets.getChildren().add(widgetsBox);
        grid.add(cardWidgets, 1, 0);
        DashboardLayout.fillGridCell(cardWidgets);

        setCenter(grid);
        updateDiagnostics(false, "--");
    }

    private Label createDiagRow(VBox parentContainer, String label, String value, String valueColorHex) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");
        HBox.setHgrow(lblLabel, Priority.ALWAYS);
        lblLabel.setMaxWidth(Double.MAX_VALUE);

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-text-fill: " + valueColorHex + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        row.getChildren().addAll(lblLabel, lblValue);
        parentContainer.getChildren().add(row);

        return lblValue;
    }

    private CheckBox createOnTopCheckbox(boolean initialState) {
        CheckBox chk = new CheckBox("Luôn trên cùng");
        chk.setSelected(initialState);
        chk.setStyle("-fx-text-fill: #71717a; -fx-font-size: 10px;");
        return chk;
    }

    private HBox createWidgetBento(String title, String desc, String iconColorHex, Feather icon, ToggleSwitch sw, CheckBox onTopChk) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(5, 5, 8, 5));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
            "-fx-border-color: rgba(255, 255, 255, 0.05);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(38, 38);
        iconBox.setMinSize(38, 38);
        iconBox.setMaxSize(38, 38);
        iconBox.setStyle(
            "-fx-background-color: #121214;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-radius: 6px;" +
            "-fx-border-width: 1px;"
        );

        Region leftBar = new Region();
        leftBar.setPrefWidth(3);
        leftBar.setMaxHeight(Double.MAX_VALUE);
        leftBar.setStyle("-fx-background-color: " + iconColorHex + "66; -fx-background-radius: 3px 0 0 3px;");
        StackPane.setAlignment(leftBar, Pos.CENTER_LEFT);

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(Color.web(iconColorHex, 0.8));

        iconBox.getChildren().addAll(leftBar, fontIcon);

        VBox textGroup = new VBox(2);
        HBox.setHgrow(textGroup, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: #71717a; -fx-font-size: 9.5px;");

        textGroup.getChildren().addAll(lblTitle, lblDesc, onTopChk);

        row.getChildren().addAll(iconBox, textGroup, sw);
        return row;
    }

    public void updateDiagnostics(boolean isConnected, String latency) {
        if (isConnected) {
            lblWebSocketDiag.setText("ĐÃ KẾT NỐI (CONNECTED)");
            lblWebSocketDiag.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblLatencyDiag.setText(latency);
            lblLatencyDiag.setStyle("-fx-text-fill: #f4f4f5; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblSyncDiag.setText("HOẠT ĐỘNG (ACTIVE)");
            lblSyncDiag.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            lblWebSocketDiag.setText("CHƯA KẾT NỐI (OFFLINE)");
            lblWebSocketDiag.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblLatencyDiag.setText("--");
            lblLatencyDiag.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblSyncDiag.setText("TẮT (INACTIVE)");
            lblSyncDiag.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    public void setConnectionState(boolean isConnected) {
        if (isConnected) {
            lblStatusBadge.setText("ĐÃ KẾT NỐI");
            lblStatusBadge.setStyle(
                "-fx-background-color: rgba(99, 102, 241, 0.08);" +
                "-fx-background-radius: 8px;" +
                "-fx-text-fill: #818cf8;" +
                "-fx-border-color: rgba(99, 102, 241, 0.4);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;"
            );
            btnConnect.setText("Ngắt kết nối LIVE");
            DashboardLayout.applyDangerButton(btnConnect);
            btnConnect.setDisable(false);
            txtUsername.setDisable(true);
            txtApiKey.setDisable(true);
        } else {
            lblStatusBadge.setText("CHƯA KẾT NỐI");
            lblStatusBadge.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.03);" +
                "-fx-background-radius: 8px;" +
                "-fx-text-fill: #a1a1aa;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;"
            );
            btnConnect.setText("Kết nối LIVE");
            DashboardLayout.applyPrimaryButton(btnConnect);
            btnConnect.setDisable(false);
            txtUsername.setDisable(false);
            txtApiKey.setDisable(false);
        }
    }

    public void setConnectingState() {
        lblStatusBadge.setText("ĐANG KẾT NỐI...");
        lblStatusBadge.setStyle(
            "-fx-background-color: rgba(168, 85, 247, 0.08);" +
            "-fx-background-radius: 8px;" +
            "-fx-text-fill: #c084fc;" +
            "-fx-border-color: rgba(168, 85, 247, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11px;"
        );
        btnConnect.setText("Đang kết nối...");
        DashboardLayout.applyButtonStyle(btnConnect,
                "-fx-background-color: rgba(168, 85, 247, 0.04);" +
                        "-fx-text-fill: #c084fc;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(168, 85, 247, 0.2);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 1px;");
        btnConnect.setDisable(true);
        txtUsername.setDisable(true);
        txtApiKey.setDisable(true);
    }

    public void setDisconnectingState() {
        lblStatusBadge.setText("ĐANG NGẮT...");
        lblStatusBadge.setStyle(
            "-fx-background-color: rgba(251, 146, 60, 0.08);" +
            "-fx-background-radius: 8px;" +
            "-fx-text-fill: #fdba74;" +
            "-fx-border-color: rgba(251, 146, 60, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11px;"
        );
        btnConnect.setText("Đang ngắt...");
        DashboardLayout.applyButtonStyle(btnConnect,
                "-fx-background-color: rgba(251, 146, 60, 0.04);" +
                        "-fx-text-fill: #fdba74;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(251, 146, 60, 0.2);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 1px;");
        btnConnect.setDisable(true);
        txtUsername.setDisable(true);
        txtApiKey.setDisable(true);
    }

    public void updateOverlayButtonStates(boolean isLeaderboardOpen, boolean isChatOpen, boolean isLikeOpen, boolean isTopLikeOpen) {
        swToggleOverlay.setSelected(isLeaderboardOpen);
        swToggleChatOverlay.setSelected(isChatOpen);
        swToggleLikeOverlay.setSelected(isLikeOpen);
        swToggleTopLikeOverlay.setSelected(isTopLikeOpen);
    }

    public String getUsername() { return txtUsername.getText().trim(); }
    public String getApiKey() { return txtApiKey.getText().trim(); }
    public CheckBox getChkLeaderboardOnTop() { return chkLeaderboardOnTop; }
    public CheckBox getChkChatOnTop() { return chkChatOnTop; }
    public CheckBox getChkLikeOnTop() { return chkLikeOnTop; }
    public CheckBox getChkTopLikeOnTop() { return chkTopLikeOnTop; }
    public Label getLblSyncDiag() { return lblSyncDiag; }
}
