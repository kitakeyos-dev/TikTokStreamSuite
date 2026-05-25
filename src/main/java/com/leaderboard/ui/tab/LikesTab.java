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
import javafx.collections.transformation.FilteredList;
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
    private final ObservableList<Liker> likerList = FXCollections.observableArrayList();
    private FilteredList<Liker> filteredList;

    private TextField txtSearch;
    private TextField txtLikeTarget;
    private Label lblTotalLikes;
    private Label lblActiveLikersVal;
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
        VBox cardMain = DashboardLayout.createPageContainer();

        lblTotalLikes = new Label("0");
        lblActiveLikersVal = new Label("0");

        HBox headerRight = DashboardLayout.createHeaderActions(
                DashboardLayout.createMiniStatCard("TỔNG SỐ TIM", lblTotalLikes, "#f43f5e"),
                DashboardLayout.createMiniStatCard("NGƯỜI THẢ TIM", lblActiveLikersVal, "#e4e4e7")
        );

        cardMain.getChildren().add(DashboardLayout.createPageHeader(
                "MỤC TIÊU & BẢNG XẾP HẠNG THẢ TIM TÍCH LŨY",
                "Theo dõi mục tiêu thả tim và quản lý danh sách người ủng hộ thả tim cộng dồn.",
                headerRight
        ));

        // Single-Row Premium Bento Bar (All elements aligned horizontally in 1 line)
        HBox goalBar = new HBox(15);
        goalBar.setAlignment(Pos.CENTER_LEFT);
        goalBar.setPadding(new Insets(10, 16, 10, 16));
        goalBar.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.01);" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: rgba(255, 255, 255, 0.05);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;"
        );

        // 1. Goal Settings controls (Left-aligned)
        Label lblTargetTitle = new Label("LIKE GOAL:");
        lblTargetTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #71717a;");

        txtLikeTarget = DashboardLayout.newTextField();
        txtLikeTarget.setText(String.valueOf(ConfigManager.getConfig().getLikeTarget()));
        txtLikeTarget.setPrefWidth(90);
        FontIcon iconTarget = new FontIcon(Feather.TARGET);
        iconTarget.setIconColor(Color.web("#71717a"));
        HBox txtLikeTargetBox = DashboardLayout.wrapTextField(txtLikeTarget, "Mục tiêu...", iconTarget);
        txtLikeTargetBox.setPrefWidth(120);

        btnUpdateTarget = DashboardLayout.newButton("Cập nhật");
        FontIcon checkIcon = new FontIcon(Feather.CHECK);
        checkIcon.setIconColor(Color.web("#818cf8"));
        btnUpdateTarget.setGraphic(checkIcon);
        DashboardLayout.applyPrimaryButton(btnUpdateTarget);
        btnUpdateTarget.setOnAction(e -> updateLikeTarget());

        // 2. Sleek Progress Bar (Middle, auto-stretches to fill space)
        progressBar = new ProgressBar(0.0);
        progressBar.setPrefHeight(10);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.setStyle(
            "-fx-box-border: transparent;" +
            "-fx-control-inner-background: #18181b;" +
            "-fx-background-color: transparent;"
        );
        progressBar.getStylesheets().add(getClass().getResource("/css/progressbar.css") != null ? 
            getClass().getResource("/css/progressbar.css").toExternalForm() : "");

        // 3. Progress percentage & remaining status (Right-aligned)
        HBox progressLabelsBox = new HBox(8);
        progressLabelsBox.setAlignment(Pos.CENTER_LEFT);

        lblPercent = new Label("0.0%");
        lblPercent.setStyle("-fx-text-fill: #f43f5e; -fx-font-size: 12px; -fx-font-weight: bold;");

        lblRemaining = new Label("(Đang chờ kết nối...)");
        lblRemaining.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");

        progressLabelsBox.getChildren().addAll(lblPercent, lblRemaining);

        goalBar.getChildren().addAll(
                lblTargetTitle, txtLikeTargetBox, btnUpdateTarget, 
                progressBar, 
                progressLabelsBox
        );
        cardMain.getChildren().add(goalBar);

        // Search Box
        txtSearch = DashboardLayout.newSearchField();
        cardMain.getChildren().add(DashboardLayout.createSearchBox(
                txtSearch, "Tìm kiếm TikTok ID hoặc Tên hiển thị..."));

        // Table
        tblLikers = DashboardLayout.createTable();

        TableColumn<Liker, Integer> colRank = new TableColumn<>("Hạng");
        colRank.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRank()).asObject());
        colRank.setPrefWidth(60);
        colRank.setStyle("-fx-alignment: CENTER; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        TableColumn<Liker, String> colId = new TableColumn<>("TikTok ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(160);

        TableColumn<Liker, String> colNick = new TableColumn<>("Tên Hiển Thị");
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(220);

        TableColumn<Liker, Integer> colLikesCount = new TableColumn<>("Số Tim Thả");
        colLikesCount.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getLikes()).asObject());
        colLikesCount.setPrefWidth(160);
        colLikesCount.setStyle("-fx-alignment: CENTER; -fx-text-fill: #f43f5e; -fx-font-weight: bold;");

        tblLikers.getColumns().addAll(colRank, colId, colNick, colLikesCount);
        
        filteredList = new FilteredList<>(likerList, p -> true);
        tblLikers.setItems(filteredList);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(l -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase().trim();
                return l.getUniqueId().toLowerCase().contains(lower) || 
                       l.getNickname().toLowerCase().contains(lower);
            });
        });

        cardMain.getChildren().add(tblLikers);

        // Actions Row
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

        cardMain.getChildren().add(DashboardLayout.createActionsRow(
                btnDeleteSelected, btnResetAll, btnAddManual));

        setCenter(cardMain);
    }

    private void updateLikeTarget() {
        String targetStr = txtLikeTarget.getText().trim();
        if (targetStr.isEmpty()) {
            Dialogs.warning(parent, "Cảnh báo", "Vui lòng nhập mục tiêu tim!");
            return;
        }

        int target;
        try {
            target = Integer.parseInt(targetStr.replaceAll("[^\\d]", ""));
            if (target <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Dialogs.error(parent, "Lỗi", "Mục tiêu tim phải là một số nguyên dương!");
            return;
        }

        ConfigManager.getConfig().setLikeTarget(target);
        ConfigManager.save();

        parent.updateLikeTargetOverlay(target);

        try {
            int current = Integer.parseInt(lblTotalLikes.getText().replaceAll("[^\\d]", ""));
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
                lblRemaining.setText("(Mục tiêu đã hoàn thành!)");
                lblRemaining.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: bold;");
            } else {
                int remaining = Math.max(0, target - totalLikes);
                lblRemaining.setText(String.format("(Còn thiếu %,d tim)", remaining));
                lblRemaining.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");
            }
        });
    }

    public void addLikeRow(String uniqueId, String nickname, int likesSent) {
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

        if (lblActiveLikersVal != null) {
            lblActiveLikersVal.setText(String.format("%,d", source.size()));
        }
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
