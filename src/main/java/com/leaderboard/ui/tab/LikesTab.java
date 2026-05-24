package com.leaderboard.ui.tab;

import com.leaderboard.ui.DashboardStage;
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
        setPadding(new Insets(15, 5, 15, 5));
        setStyle("-fx-background-color: transparent;");
        initComponents();
        updateProgress(0);
    }

    private void initComponents() {
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(0);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        // Column 1: Config, Stats and Progress Card
        VBox cardStats = createCard("MỤC TIÊU THẢ TIM & THỐNG KÊ");
        VBox leftContent = new VBox(12);
        leftContent.setPadding(new Insets(10, 0, 10, 0));

        Label lblLikesTitle = new Label("TỔNG SỐ TIM ĐÃ NHẬN TỪ LIVE CLIENT:");
        lblLikesTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #71717a;");

        lblTotalLikes = new Label("0");
        lblTotalLikes.setStyle(
            "-fx-font-size: 48px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #f4f4f5;"
        );

        Label lblTargetTitle = new Label("MỤC TIÊU THẢ TIM TIẾP THEO (LIKE GOAL):");
        lblTargetTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #71717a;");

        txtLikeTarget = new TextField(String.valueOf(ConfigManager.getConfig().getLikeTarget()));
        txtLikeTarget.setPrefHeight(36);
        txtLikeTarget.setStyle(
            "-fx-background-color: #18181b;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
            "-fx-border-width: 1px;" +
            "-fx-text-fill: #f4f4f5;" +
            "-fx-padding: 0 10 0 10;"
        );

        Label lblProgressTitle = new Label("TIẾN TRÌNH HIỆN TẠI:");
        lblProgressTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #71717a;");

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

        // Buttons row
        HBox targetControls = new HBox(10);
        targetControls.setAlignment(Pos.CENTER_LEFT);
        targetControls.setPadding(new Insets(10, 0, 0, 0));

        btnUpdateTarget = new Button("Cập Nhật");
        btnUpdateTarget.setPrefHeight(32);
        
        FontIcon checkIcon = new FontIcon(Feather.CHECK);
        checkIcon.setIconColor(Color.web("#818cf8"));
        btnUpdateTarget.setGraphic(checkIcon);
        
        btnUpdateTarget.setStyle(
            "-fx-background-color: rgba(99, 102, 241, 0.08);" +
            "-fx-text-fill: #818cf8;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(99, 102, 241, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        btnUpdateTarget.setOnAction(e -> updateLikeTarget());

        btnToggleLikeOverlayTab4 = new Button("BẬT MỤC TIÊU TIM");
        btnToggleLikeOverlayTab4.setPrefHeight(32);
        btnToggleLikeOverlayTab4.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #a1a1aa;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        btnToggleLikeOverlayTab4.setOnAction(e -> parent.toggleLikeOverlayWindow());

        btnToggleTopLikeOverlayTab4 = new Button("BẬT TOP TIM");
        btnToggleTopLikeOverlayTab4.setPrefHeight(32);
        btnToggleTopLikeOverlayTab4.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #a1a1aa;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        btnToggleTopLikeOverlayTab4.setOnAction(e -> parent.toggleTopLikeOverlayWindow());

        targetControls.getChildren().addAll(btnUpdateTarget, btnToggleLikeOverlayTab4, btnToggleTopLikeOverlayTab4);

        leftContent.getChildren().addAll(lblLikesTitle, lblTotalLikes, lblTargetTitle, txtLikeTarget, lblProgressTitle, progressBar, progressLabels, targetControls);
        cardStats.getChildren().add(leftContent);
        grid.add(cardStats, 0, 0);

        // Column 2: Real-time Like Activity Log Card
        VBox cardLikesLog = createCard("DÒNG SỰ KIỆN THẢ TIM THỰC TẾ");
        
        tblLikeLog = new TableView<>();
        tblLikeLog.setPrefHeight(360);
        tblLikeLog.setStyle(
            "-fx-background-color: #121214;" +
            "-fx-control-inner-background: #121214;" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );

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

        HBox logActions = new HBox(12);
        logActions.setAlignment(Pos.CENTER_RIGHT);
        logActions.setPadding(new Insets(10, 0, 0, 0));

        btnResetLikes = new Button("Xoá Lịch Sử");
        btnResetLikes.setPrefHeight(32);
        
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#f87171"));
        btnResetLikes.setGraphic(trashIcon);
        
        btnResetLikes.setStyle(
            "-fx-background-color: rgba(239, 68, 68, 0.08);" +
            "-fx-text-fill: #f87171;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(239, 68, 68, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        btnResetLikes.setOnAction(e -> {
            likeList.clear();
            lblTotalLikes.setText("0");
            parent.resetLikesOverlay();
            updateProgress(0);
        });
        logActions.getChildren().add(btnResetLikes);

        cardLikesLog.getChildren().addAll(tblLikeLog, logActions);
        grid.add(cardLikesLog, 1, 0);

        setCenter(grid);
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

    private void updateLikeTarget() {
        String targetStr = txtLikeTarget.getText().trim();
        if (targetStr.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng nhập mục tiêu tim!", ButtonType.OK);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        int target;
        try {
            target = Integer.parseInt(targetStr.replace(",", ""));
            if (target <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Mục tiêu tim phải là một số nguyên dương!", ButtonType.OK);
            err.setTitle("Lỗi");
            err.setHeaderText(null);
            err.showAndWait();
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

        Alert success = new Alert(Alert.AlertType.INFORMATION, "Đã cập nhật mục tiêu tim thành " + String.format("%,d", target) + "!", ButtonType.OK);
        success.setTitle("Thành công");
        success.setHeaderText(null);
        success.showAndWait();
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
        styleToggleButton(btnToggleLikeOverlayTab4, "Mục Tiêu Tim", isLikeOpen);
        styleToggleButton(btnToggleTopLikeOverlayTab4, "Top Tim", isTopLikeOpen);
    }

    private void styleToggleButton(Button btn, String title, boolean isActive) {
        if (isActive) {
            btn.setText("TẮT " + title.toUpperCase());
            btn.setStyle(
                "-fx-background-color: rgba(99, 102, 241, 0.12);" +
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
}
