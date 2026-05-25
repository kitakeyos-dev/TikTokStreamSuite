package com.leaderboard.ui.tab;

import com.leaderboard.model.Liker;
import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.DataManager;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LikesTab extends BorderPane {
    private final DashboardStage parent;
    private TableView<Liker> tblLikers;
    private ObservableList<Liker> likerList = FXCollections.observableArrayList();

    private TextField txtLikeTarget;
    private Label lblTotalLikes;
    private Label lblPercent;
    private Label lblRemaining;
    private ProgressBar progressBar;

    private Button btnUpdateTarget;
    private Button btnDeleteSelected;
    private Button btnResetAll;
    private Button btnAddManual;

    private final PauseTransition refreshThrottle = new PauseTransition(Duration.millis(500));
    private boolean pendingRefresh = false;

    public LikesTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);
        refreshThrottle.setOnFinished(e -> doRefreshLikerTableData());
        initComponents();
        updateProgress(0);
        refreshLikerTableData();
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

        targetControls.getChildren().addAll(btnUpdateTarget);

        leftContent.getChildren().addAll(lblLikesTitle, lblTotalLikes, lblTargetTitle, txtLikeTargetBox, lblProgressTitle, progressBar, progressLabels, targetControls);
        cardStats.getChildren().add(leftContent);
        grid.add(cardStats, 0, 0);
        DashboardLayout.fillGridCell(cardStats);

        // Column 2: Persistent Top Likers Leaderboard Card
        VBox cardLikers = DashboardLayout.createCard("BẢNG XẾP HẠNG THẢ TIM TÍCH LŨY");

        tblLikers = DashboardLayout.createTable();

        TableColumn<Liker, Integer> colRank = new TableColumn<>("Hạng");
        colRank.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRank()).asObject());
        colRank.setPrefWidth(60);
        colRank.setStyle("-fx-alignment: CENTER; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        TableColumn<Liker, String> colId = new TableColumn<>("TikTok ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(140);

        TableColumn<Liker, String> colNick = new TableColumn<>("Tên Hiển Thị");
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(180);

        TableColumn<Liker, Integer> colLikesCount = new TableColumn<>("Số Tim Thả");
        colLikesCount.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getLikes()).asObject());
        colLikesCount.setPrefWidth(120);
        colLikesCount.setStyle("-fx-alignment: CENTER; -fx-text-fill: #f43f5e; -fx-font-weight: bold;");

        tblLikers.getColumns().addAll(colRank, colId, colNick, colLikesCount);
        tblLikers.setItems(likerList);

        btnDeleteSelected = DashboardLayout.newButton("Xoá Người Chọn");
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#a1a1aa"));
        btnDeleteSelected.setGraphic(trashIcon);
        DashboardLayout.applySecondaryButton(btnDeleteSelected);
        btnDeleteSelected.setOnAction(e -> deleteSelectedLiker());

        btnResetAll = DashboardLayout.newButton("Xoá Hết Bảng");
        FontIcon refreshIcon = new FontIcon(Feather.REFRESH_CW);
        refreshIcon.setIconColor(Color.web("#f87171"));
        btnResetAll.setGraphic(refreshIcon);
        DashboardLayout.applyDangerButton(btnResetAll);
        btnResetAll.setOnAction(e -> resetLikers());

        btnAddManual = DashboardLayout.newButton("Cộng Tim Thủ Công");
        FontIcon plusIcon = new FontIcon(Feather.PLUS_CIRCLE);
        plusIcon.setIconColor(Color.web("#f43f5e"));
        btnAddManual.setGraphic(plusIcon);
        DashboardLayout.applyPrimaryButton(btnAddManual);
        btnAddManual.setOnAction(e -> addManualLikes());

        cardLikers.getChildren().addAll(tblLikers, DashboardLayout.createActionsRow(
                btnDeleteSelected, btnResetAll, btnAddManual));
        grid.add(cardLikers, 1, 0);
        DashboardLayout.fillGridCell(cardLikers);

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
        // Obsolete event log handler, kept for compatibility with connector callback
        refreshLikerTableData();
    }

    public void refreshLikerTableData() {
        pendingRefresh = true;
        refreshThrottle.playFromStart();
    }

    private void doRefreshLikerTableData() {
        pendingRefresh = false;
        List<Liker> source;
        synchronized (DataManager.class) {
            source = new ArrayList<>(DataManager.getLikers());
        }

        // --- Incremental update to prevent flicker ---
        java.util.Map<String, Liker> currentMap = new java.util.HashMap<>();
        for (Liker l : likerList) {
            currentMap.put(l.getUniqueId().toLowerCase(), l);
        }

        int rank = 1;
        for (Liker fresh : source) {
            fresh.setRank(rank++);
            String key = fresh.getUniqueId().toLowerCase();
            Liker existing = currentMap.get(key);
            if (existing != null) {
                existing.setRank(fresh.getRank());
                existing.setLikes(fresh.getLikes());
                existing.setNickname(fresh.getNickname());
                existing.setAvatarUrl(fresh.getAvatarUrl());
                currentMap.remove(key);
            } else {
                likerList.add(fresh);
            }
        }

        if (!currentMap.isEmpty()) {
            likerList.removeIf(l -> currentMap.containsKey(l.getUniqueId().toLowerCase()));
        }

        FXCollections.sort(likerList);
        tblLikers.refresh();
    }

    private void deleteSelectedLiker() {
        Liker selected = tblLikers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.warning(parent, "Cảnh báo", "Vui lòng chọn người cần xoá!");
            return;
        }

        if (Dialogs.confirm(parent, "Xác nhận xoá", "Bạn có chắc muốn xoá người dùng @" + selected.getUniqueId() + " khỏi bảng xếp hạng thả tim?", "Xoá")) {
            synchronized (DataManager.class) {
                List<Liker> list = DataManager.getLikers();
                list.removeIf(l -> l.getUniqueId().equalsIgnoreCase(selected.getUniqueId()));
                DataManager.save();
            }
            refreshLikerTableData();
            parent.updateTopLikeOverlay();
        }
    }

    private void resetLikers() {
        if (Dialogs.confirm(parent, "Xác nhận xoá sạch", "Hành động này sẽ xoá sạch bảng xếp hạng thả tim hiện tại. Bạn có muốn tiếp tục?", "Xoá sạch")) {
            synchronized (DataManager.class) {
                DataManager.getLikers().clear();
                DataManager.save();
            }
            refreshLikerTableData();
            parent.updateTopLikeOverlay();
        }
    }

    private void addManualLikes() {
        java.util.Optional<String> idResult = Dialogs.input(parent, "Cộng tim thủ công", "Nhập TikTok ID (ví dụ: user123):", "TikTok ID:", "");
        if (idResult.isEmpty() || idResult.get().trim().isEmpty()) return;
        String uniqueId = idResult.get().trim();

        java.util.Optional<String> nickResult = Dialogs.input(parent, "Cộng tim thủ công", "Nhập Tên Hiển Thị (không bắt buộc):", "Tên:", uniqueId);
        String nickname = nickResult.orElse("").trim();
        if (nickname.isEmpty()) nickname = uniqueId;

        java.util.Optional<String> likesResult = Dialogs.input(parent, "Cộng tim thủ công", "Nhập số tim cần cộng (hoặc trừ nếu nhập số âm):", "Số tim:", "100");
        if (likesResult.isEmpty() || likesResult.get().trim().isEmpty()) return;

        int likes;
        try {
            likes = Integer.parseInt(likesResult.get().trim());
        } catch (NumberFormatException e) {
            Dialogs.error(parent, "Lỗi", "Số tim không hợp lệ!");
            return;
        }

        final String finalUniqueId = uniqueId;
        final String finalNickname = nickname;
        final int finalLikes = likes;

        synchronized (DataManager.class) {
            List<Liker> list = DataManager.getLikers();
            java.util.Optional<Liker> existing = list.stream()
                    .filter(l -> l.getUniqueId().equalsIgnoreCase(finalUniqueId))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().addLikes(finalLikes);
                existing.get().setNickname(finalNickname);
            } else {
                list.add(new Liker(finalUniqueId, finalNickname, null, Math.max(0, finalLikes)));
            }

            Collections.sort(list);
            DataManager.save();
        }

        refreshLikerTableData();
        parent.updateTopLikeOverlay();
    }
}
