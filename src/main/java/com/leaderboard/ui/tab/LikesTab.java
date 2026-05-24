package com.leaderboard.ui.tab;

import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.util.ConfigManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LikesTab extends BorderPane {
    private final DashboardStage parent;
    private TableView<LikeRow> tblLikeLog;
    private ObservableList<LikeRow> likeList = FXCollections.observableArrayList();

    private TextField txtLikeTarget;
    private Label lblTotalLikes;
    private Label lblPercent;
    private Label lblRemaining;
    private ProgressBar progressBar;

    private Button btnUpdateTarget;
    private Button btnToggleLikeOverlayTab4;
    private Button btnToggleTopLikeOverlayTab4;
    private Button btnResetLikes;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static class LikeRow {
        private final String time;
        private final String uniqueId;
        private final String nickname;
        private final int likesSent;

        public LikeRow(String time, String uniqueId, String nickname, int likesSent) {
            this.time = time;
            this.uniqueId = uniqueId;
            this.nickname = nickname;
            this.likesSent = likesSent;
        }

        public String getTime() { return time; }
        public String getUniqueId() { return uniqueId; }
        public String getNickname() { return nickname; }
        public int getLikesSent() { return likesSent; }
    }

    public LikesTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);
        initComponents();
        updateProgress(0);
    }

    private void initComponents() {
        GridPane grid = DashboardLayout.createTwoColumnGrid();

        VBox cardStats = DashboardLayout.createCard("MỤC TIÊU THẢ TIM & THỐNG KÊ");
        VBox leftContent = DashboardLayout.createSectionContent();

        Label lblLikesTitle = DashboardLayout.createFieldLabel("TỔNG SỐ TIM ĐÃ NHẬN TỪ LIVE CLIENT:");

        lblTotalLikes = new Label("0");
        lblTotalLikes.setStyle(
            "-fx-font-size: 48px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #f4f4f5;"
        );

        Label lblTargetTitle = DashboardLayout.createFieldLabel("MỤC TIÊU THẢ TIM TIẾP THEO (LIKE GOAL):");

        txtLikeTarget = DashboardLayout.newTextField();
        txtLikeTarget.setText(String.valueOf(ConfigManager.getConfig().getLikeTarget()));
        FontIcon iconTarget = new FontIcon(Feather.TARGET);
        iconTarget.setIconColor(Color.web("#71717a"));
        HBox txtLikeTargetBox = DashboardLayout.wrapTextField(txtLikeTarget, "Nhập số tim mục tiêu...", iconTarget);

        Label lblProgressTitle = DashboardLayout.createFieldLabel("TIẾN TRÌNH HIỆN TẠI:");

        progressBar = new ProgressBar(0.0);
        progressBar.setPrefHeight(16);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle(
            "-fx-box-border: transparent;" +
            "-fx-control-inner-background: #18181b;" +
            "-fx-background-color: transparent;"
        );
        progressBar.getStylesheets().add(getClass().getResource("/css/progressbar.css") != null ? 
            getClass().getResource("/css/progressbar.css").toExternalForm() : "");

        HBox progressLabels = new HBox();
        progressLabels.setAlignment(Pos.CENTER_LEFT);

        lblPercent = new Label("0.0%");
        lblPercent.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: bold;");
        HBox.setHgrow(lblPercent, Priority.ALWAYS);
        lblPercent.setMaxWidth(Double.MAX_VALUE);

        lblRemaining = new Label("Đang chờ kết nối...");
        lblRemaining.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");

        progressLabels.getChildren().addAll(lblPercent, lblRemaining);

        // Buttons — full width stack in narrow config column
        VBox targetControls = new VBox(8);
        targetControls.setPadding(new Insets(10, 0, 0, 0));
        targetControls.setMaxWidth(Double.MAX_VALUE);

        btnUpdateTarget = DashboardLayout.newButton("Cập nhật mục tiêu");
        FontIcon checkIcon = new FontIcon(Feather.CHECK);
        checkIcon.setIconColor(Color.web("#818cf8"));
        btnUpdateTarget.setGraphic(checkIcon);
        DashboardLayout.applyPrimaryButton(btnUpdateTarget);
        DashboardLayout.allowButtonGrow(btnUpdateTarget);
        btnUpdateTarget.setOnAction(e -> updateLikeTarget());

        btnToggleLikeOverlayTab4 = DashboardLayout.newButton("Bật mục tiêu tim");
        DashboardLayout.applySecondaryButton(btnToggleLikeOverlayTab4);
        DashboardLayout.allowButtonGrow(btnToggleLikeOverlayTab4);
        btnToggleLikeOverlayTab4.setOnAction(e -> parent.toggleLikeOverlayWindow());

        btnToggleTopLikeOverlayTab4 = DashboardLayout.newButton("Bật top tim");
        DashboardLayout.applySecondaryButton(btnToggleTopLikeOverlayTab4);
        DashboardLayout.allowButtonGrow(btnToggleTopLikeOverlayTab4);
        btnToggleTopLikeOverlayTab4.setOnAction(e -> parent.toggleTopLikeOverlayWindow());

        targetControls.getChildren().addAll(btnUpdateTarget, btnToggleLikeOverlayTab4, btnToggleTopLikeOverlayTab4);

        leftContent.getChildren().addAll(lblLikesTitle, lblTotalLikes, lblTargetTitle, txtLikeTargetBox, lblProgressTitle, progressBar, progressLabels, targetControls);
        cardStats.getChildren().add(leftContent);
        grid.add(cardStats, 0, 0);
        DashboardLayout.fillGridCell(cardStats);

        // Column 2: Real-time Like Activity Log Card
        VBox cardLikesLog = DashboardLayout.createCard("DÒNG SỰ KIỆN THẢ TIM THỰC TẾ");

        tblLikeLog = DashboardLayout.createTable();

        TableColumn<LikeRow, String> colTime = new TableColumn<>("Thời Gian");
        colTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTime()));
        colTime.setPrefWidth(90);
        colTime.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        TableColumn<LikeRow, String> colId = new TableColumn<>("TikTok ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(120);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #818cf8;");

        TableColumn<LikeRow, String> colNick = new TableColumn<>("Tên Hiển Thị");
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(140);

        TableColumn<LikeRow, Integer> colLikes = new TableColumn<>("Số Tim Thả");
        colLikes.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getLikesSent()).asObject());
        colLikes.setPrefWidth(90);
        colLikes.setStyle("-fx-alignment: CENTER; -fx-text-fill: #e4e4e7;");

        tblLikeLog.getColumns().addAll(colTime, colId, colNick, colLikes);
        tblLikeLog.setItems(likeList);

        btnResetLikes = DashboardLayout.newButton("Xoá Lịch Sử");
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#f87171"));
        btnResetLikes.setGraphic(trashIcon);
        DashboardLayout.applyDangerButton(btnResetLikes);
        btnResetLikes.setOnAction(e -> {
            likeList.clear();
            lblTotalLikes.setText("0");
            parent.resetLikesOverlay();
            updateProgress(0);
        });

        cardLikesLog.getChildren().addAll(tblLikeLog, DashboardLayout.createActionsRow(btnResetLikes));
        grid.add(cardLikesLog, 1, 0);
        DashboardLayout.fillGridCell(cardLikesLog);

        setCenter(grid);
    }

    private void updateLikeTarget() {
        String targetStr = txtLikeTarget.getText().trim();
        if (targetStr.isEmpty()) {
            Dialogs.warning(parent, "Cảnh báo", "Vui lòng nhập mục tiêu tim!");
            return;
        }

        int target;
        try {
            target = Integer.parseInt(targetStr.replace(",", ""));
            if (target <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Dialogs.error(parent, "Lỗi", "Mục tiêu tim phải là một số nguyên dương!");
            return;
        }

        ConfigManager.getConfig().setLikeTarget(target);
        ConfigManager.save();

        parent.updateLikeTargetOverlay(target);

        // Refresh progress visuals
        try {
            int current = Integer.parseInt(lblTotalLikes.getText().replace(",", ""));
            updateProgress(current);
        } catch (Exception ex) {
            updateProgress(0);
        }

        Dialogs.info(parent, "Thành công", "Đã cập nhật mục tiêu tim thành " + String.format("%,d", target) + "!");
    }

    public void updateProgress(int totalLikes) {
        int target = ConfigManager.getConfig().getLikeTarget();
        double percent = Math.min(100.0, ((double) totalLikes / Math.max(1, target)) * 100);
        
        Platform.runLater(() -> {
            lblTotalLikes.setText(String.format("%,d", totalLikes));
            progressBar.setProgress(Math.min(1.0, (double) totalLikes / Math.max(1, target)));
            lblPercent.setText(String.format("%.1f%%", percent));
            
            if (totalLikes >= target) {
                lblRemaining.setText("Mục tiêu đã hoàn thành!");
                lblRemaining.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else {
                int remaining = Math.max(0, target - totalLikes);
                lblRemaining.setText(String.format("Còn thiếu %,d tim", remaining));
                lblRemaining.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");
            }
        });
    }

    public void addLikeRow(String uniqueId, String nickname, int likesSent) {
        String time = LocalTime.now().format(TIME_FORMATTER);
        LikeRow row = new LikeRow(time, uniqueId, nickname, likesSent);
        likeList.add(0, row); // Insert at top like in Swing
        if (likeList.size() > 100) {
            likeList.remove(100, likeList.size());
        }
    }

    public void updateOverlayButtonStates(boolean isLikeOpen, boolean isTopLikeOpen) {
        styleToggleButton(btnToggleLikeOverlayTab4, "mục tiêu tim", isLikeOpen);
        styleToggleButton(btnToggleTopLikeOverlayTab4, "top tim", isTopLikeOpen);
    }

    private void styleToggleButton(Button btn, String title, boolean isActive) {
        DashboardLayout.applyToggleButton(btn, title, isActive);
    }
}
