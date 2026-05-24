package com.leaderboard.ui.tab;

import com.leaderboard.ui.DashboardStage;
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

    private TextField txtUsername;
    private PasswordField txtApiKey;
    private Label lblStatusBadge;
    private Button btnConnect;

    private Button btnToggleOverlay;
    private Button btnToggleChatOverlay;
    private Button btnToggleLikeOverlay;
    private Button btnToggleTopLikeOverlay;

    private CheckBox chkLeaderboardOnTop;
    private CheckBox chkChatOnTop;
    private CheckBox chkLikeOnTop;
    private CheckBox chkTopLikeOnTop;

    private Label lblWebSocketDiag;
    private Label lblLatencyDiag;
    private Label lblSyncDiag;

    public OverviewTab(DashboardStage parent) {
        this.parent = parent;
        setPadding(new Insets(15, 5, 15, 5));
        setStyle("-fx-background-color: transparent;");

        // Two Column Layout
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(0);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Column 1: Connection & Config
        VBox cardConfig = createCard("CẤU HÌNH KẾT NỐI");
        VBox leftContent = new VBox(12);
        leftContent.setPadding(new Insets(10, 0, 10, 0));

        // TikTok Username
        Label lblUser = new Label("TIKTOK USERNAME (TÊN KÊNH LIVE):");
        lblUser.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #71717a;");
        
        HBox userFieldBox = new HBox(8);
        userFieldBox.setAlignment(Pos.CENTER_LEFT);
        userFieldBox.setPadding(new Insets(0, 10, 0, 10));
        userFieldBox.setStyle(
            "-fx-background-color: #18181b;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
            "-fx-border-width: 1px;"
        );
        Label lblAt = new Label("@");
        lblAt.setStyle("-fx-text-fill: #71717a; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        txtUsername = new TextField(ConfigManager.getConfig().getStreamerUsername());
        txtUsername.setPromptText("Ví dụ: streamer_live");
        txtUsername.setPrefHeight(34);
        txtUsername.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #f4f4f5;");
        HBox.setHgrow(txtUsername, Priority.ALWAYS);
        userFieldBox.getChildren().addAll(lblAt, txtUsername);

        // API Key
        Label lblKey = new Label("EULERSTREAM API KEY (TÙY CHỌN):");
        lblKey.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #71717a;");
        
        HBox keyFieldBox = new HBox(8);
        keyFieldBox.setAlignment(Pos.CENTER_LEFT);
        keyFieldBox.setPadding(new Insets(0, 10, 0, 10));
        keyFieldBox.setStyle(
            "-fx-background-color: #18181b;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
            "-fx-border-width: 1px;"
        );
        FontIcon keyIcon = new FontIcon(Feather.KEY);
        keyIcon.setIconColor(Color.web("#71717a"));
        
        txtApiKey = new PasswordField();
        txtApiKey.setText(ConfigManager.getConfig().getEulerstreamKey());
        txtApiKey.setPromptText("Nhập API Key để livestream ổn định...");
        txtApiKey.setPrefHeight(34);
        txtApiKey.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #f4f4f5;");
        HBox.setHgrow(txtApiKey, Priority.ALWAYS);
        keyFieldBox.getChildren().addAll(keyIcon, txtApiKey);

        // Status Row (Badge + Connect Button)
        HBox statusRow = new HBox(15);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.setPadding(new Insets(10, 0, 10, 0));

        lblStatusBadge = new Label("CHƯA KẾT NỐI");
        lblStatusBadge.setAlignment(Pos.CENTER);
        lblStatusBadge.setPrefSize(110, 32);
        lblStatusBadge.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.03);" +
            "-fx-background-radius: 8px;" +
            "-fx-text-fill: #a1a1aa;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;" +
            "-fx-font-size: 11px;"
        );

        btnConnect = new Button("Kết nối LIVE");
        btnConnect.setPrefHeight(32);
        btnConnect.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnConnect, Priority.ALWAYS);
        btnConnect.setStyle(
            "-fx-background-color: rgba(99, 102, 241, 0.08);" + // Soft indigo tint
            "-fx-text-fill: #818cf8;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(99, 102, 241, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        btnConnect.setOnAction(e -> parent.toggleConnection());

        statusRow.getChildren().addAll(lblStatusBadge, btnConnect);

        // Divider
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.08; -fx-padding: 5 0 5 0;");

        // System Diagnostics Section
        Label lblDiagTitle = new Label("TRẠNG THÁI HỆ THỐNG / DIAGNOSTICS:");
        lblDiagTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #71717a;");

        VBox diagBox = new VBox(10);
        diagBox.setPadding(new Insets(5, 10, 5, 10));
        
        lblWebSocketDiag = createDiagRow(diagBox, "WebSocket", "CHƯA KẾT NỐI", "#71717a");
        lblLatencyDiag = createDiagRow(diagBox, "Độ trễ kết nối (Latency)", "--", "#f4f4f5");
        lblSyncDiag = createDiagRow(diagBox, "Đồng bộ mắt xem (Viewer Sync)", "INACTIVE", "#71717a");

        leftContent.getChildren().addAll(lblUser, userFieldBox, lblKey, keyFieldBox, statusRow, sep, lblDiagTitle, diagBox);
        cardConfig.getChildren().add(leftContent);
        grid.add(cardConfig, 0, 0);

        // Column 2: Quick OBS Overlays controls (Bento)
        VBox cardWidgets = createCard("ĐIỀU KHIỂN WIDGETS OBS");
        VBox widgetsBox = new VBox(12);
        widgetsBox.setPadding(new Insets(10, 5, 10, 5));

        // Bento 1: Rankings Overlay
        btnToggleOverlay = new Button("BẬT BẢNG XẾP HẠNG");
        btnToggleOverlay.setOnAction(e -> parent.toggleOverlayWindow());
        chkLeaderboardOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayLeaderboardOnTop());
        chkLeaderboardOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayLeaderboardOnTop(chkLeaderboardOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        widgetsBox.getChildren().add(createWidgetBento("BẢNG XẾP HẠNG LIVE", "Hiển thị Top nhà tài trợ và quà tặng.", "#818cf8", Feather.BAR_CHART_2, btnToggleOverlay, chkLeaderboardOnTop));

        // Bento 2: Chat Overlay
        btnToggleChatOverlay = new Button("BẬT KHUNG CHAT");
        btnToggleChatOverlay.setOnAction(e -> parent.toggleChatOverlayWindow());
        chkChatOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayChatOnTop());
        chkChatOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayChatOnTop(chkChatOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        widgetsBox.getChildren().add(createWidgetBento("KHUNG CHAT TRONG SUỐT", "Hiển thị dòng chat game capture trực tiếp.", "#818cf8", Feather.MESSAGE_SQUARE, btnToggleChatOverlay, chkChatOnTop));

        // Bento 3: Like Goal Overlay
        btnToggleLikeOverlay = new Button("BẬT MỤC TIÊU TIM");
        btnToggleLikeOverlay.setOnAction(e -> parent.toggleLikeOverlayWindow());
        chkLikeOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayLikeOnTop());
        chkLikeOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayLikeOnTop(chkLikeOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        widgetsBox.getChildren().add(createWidgetBento("MỤC TIÊU THẢ TIM", "Thanh tim bay lơ lửng và đếm tim.", "#818cf8", Feather.HEART, btnToggleLikeOverlay, chkLikeOnTop));

        // Bento 4: Top Like Overlay
        btnToggleTopLikeOverlay = new Button("BẬT TOP THẢ TIM");
        btnToggleTopLikeOverlay.setOnAction(e -> parent.toggleTopLikeOverlayWindow());
        chkTopLikeOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayTopLikeOnTop());
        chkTopLikeOnTop.setOnAction(e -> {
            ConfigManager.getConfig().setOverlayTopLikeOnTop(chkTopLikeOnTop.isSelected());
            ConfigManager.save();
            parent.updateOverlayAlwaysOnTop();
        });
        widgetsBox.getChildren().add(createWidgetBento("TOP THẢ TIM", "Bảng xếp hạng thả tim thời gian thực.", "#818cf8", Feather.AWARD, btnToggleTopLikeOverlay, chkTopLikeOnTop));

        cardWidgets.getChildren().add(widgetsBox);
        grid.add(cardWidgets, 1, 0);

        setCenter(grid);
        updateDiagnostics(false, "--");
    }

    private VBox createCard(String title) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setStyle(
            "-fx-background-color: #121214;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.05);" +
            "-fx-border-radius: 12px;" +
            "-fx-border-width: 1px;"
        );

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #f4f4f5; -fx-font-weight: bold; -fx-font-size: 13px;");
        
        Separator titleSep = new Separator();
        titleSep.setStyle("-fx-opacity: 0.08; -fx-padding: 2 0 5 0;");

        card.getChildren().addAll(lblTitle, titleSep);
        return card;
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

    private HBox createWidgetBento(String title, String desc, String iconColorHex, Feather icon, Button toggleBtn, CheckBox onTopChk) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(5, 5, 8, 5));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
            "-fx-border-color: rgba(255, 255, 255, 0.05);" +
            "-fx-border-width: 0 0 1px 0;"
        );

        // Tech Visual Icon block using Ikonli FontIcon
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(42, 38);
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
        leftBar.setStyle("-fx-background-color: " + iconColorHex + "66; -fx-background-radius: 3px 0 0 3px;"); // Muted icon accent bar
        StackPane.setAlignment(leftBar, Pos.CENTER_LEFT);
        
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(Color.web(iconColorHex, 0.8)); // slightly translucent icon

        iconBox.getChildren().addAll(leftBar, fontIcon);

        // Text Grouping
        VBox textGroup = new VBox(2);
        HBox.setHgrow(textGroup, Priority.ALWAYS);
        
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: bold; -fx-font-size: 11px;");
        
        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: #71717a; -fx-font-size: 9.5px;");
        
        textGroup.getChildren().addAll(lblTitle, lblDesc, onTopChk);
        
        // Button
        toggleBtn.setPrefWidth(140);
        toggleBtn.setPrefHeight(28);
        
        row.getChildren().addAll(iconBox, textGroup, toggleBtn);
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
            btnConnect.setStyle(
                "-fx-background-color: rgba(239, 68, 68, 0.08);" + // Outlined red disconnect button
                "-fx-text-fill: #f87171;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: rgba(239, 68, 68, 0.4);" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-width: 1px;"
            );
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
            btnConnect.setStyle(
                "-fx-background-color: rgba(99, 102, 241, 0.08);" +
                "-fx-text-fill: #818cf8;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: rgba(99, 102, 241, 0.4);" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-width: 1px;"
            );
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
    }

    public void updateOverlayButtonStates(boolean isLeaderboardOpen, boolean isChatOpen, boolean isLikeOpen, boolean isTopLikeOpen) {
        styleToggleButton(btnToggleOverlay, "Bảng Xếp Hạng", isLeaderboardOpen);
        styleToggleButton(btnToggleChatOverlay, "Khung Chat", isChatOpen);
        styleToggleButton(btnToggleLikeOverlay, "Mục Tiêu Tim", isLikeOpen);
        styleToggleButton(btnToggleTopLikeOverlay, "Top Thả Tim", isTopLikeOpen);
    }

    private void styleToggleButton(Button btn, String title, boolean isActive) {
        if (isActive) {
            btn.setText("TẮT " + title.toUpperCase());
            btn.setStyle(
                "-fx-background-color: rgba(99, 102, 241, 0.12);" + // Premium low-saturation active state
                "-fx-text-fill: #818cf8;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: rgba(99, 102, 241, 0.4);" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-width: 1px;"
            );
        } else {
            btn.setText("BẬT " + title.toUpperCase());
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #a1a1aa;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-width: 1px;"
            );
        }
    }

    public String getUsername() {
        return txtUsername.getText().trim();
    }

    public String getApiKey() {
        return txtApiKey.getText().trim();
    }

    public CheckBox getChkLeaderboardOnTop() {
        return chkLeaderboardOnTop;
    }

    public CheckBox getChkChatOnTop() {
        return chkChatOnTop;
    }

    public CheckBox getChkLikeOnTop() {
        return chkLikeOnTop;
    }

    public CheckBox getChkTopLikeOnTop() {
        return chkTopLikeOnTop;
    }

    public Label getLblSyncDiag() {
        return lblSyncDiag;
    }
}
