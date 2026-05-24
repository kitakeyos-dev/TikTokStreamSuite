package com.leaderboard.ui.tab;

import com.leaderboard.model.Gifter;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.util.DataManager;
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
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class LeaderboardTab extends BorderPane {
    private final DashboardStage parent;
    private TableView<Gifter> tblGifters;
    private ObservableList<Gifter> gifterList = FXCollections.observableArrayList();
    private FilteredList<Gifter> filteredList;

    private TextField txtSearch;
    private Label lblTotalDiamondsVal;
    private Label lblActiveDonorsVal;
    
    private Button btnDeleteSelected;
    private Button btnResetAll;
    private Button btnAddManual;
    private Button btnToggleOverlayTab2;

    // Throttle: limit refreshes to once per 500ms to avoid flicker from rapid events
    private final PauseTransition refreshThrottle = new PauseTransition(Duration.millis(500));
    private boolean pendingRefresh = false;

    public LeaderboardTab(DashboardStage parent) {
        this.parent = parent;
        setPadding(new Insets(15, 5, 15, 5));
        setStyle("-fx-background-color: transparent;");
        // Setup throttle: when it fires, do the actual refresh
        refreshThrottle.setOnFinished(e -> doRefreshTableData());
        initComponents();
        refreshTableData();
    }

    private void initComponents() {
        VBox cardLeaderboard = new VBox(15);
        cardLeaderboard.setPadding(new Insets(15, 20, 15, 20));
        cardLeaderboard.setStyle(
            "-fx-background-color: #121214;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.05);" +
            "-fx-border-radius: 12px;" +
            "-fx-border-width: 1px;"
        );

        // --- SUBHEADER PANEL ---
        HBox cardHeader = new HBox(15);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(0, 0, 10, 0));

        // Left Side: Title & Subtitle
        VBox titleArea = new VBox(2);
        HBox.setHgrow(titleArea, Priority.ALWAYS);
        
        Label lblTitle = new Label("BẢNG XẾP HẠNG DONATE TÍCH LŨY");
        lblTitle.setStyle("-fx-text-fill: #f4f4f5; -fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label lblSubtitle = new Label("Quản lý danh sách người ủng hộ tích lũy và tổng số kim cương nhận được.");
        lblSubtitle.setStyle("-fx-text-fill: #71717a; -fx-font-size: 10px;");
        
        titleArea.getChildren().addAll(lblTitle, lblSubtitle);
        cardHeader.getChildren().add(titleArea);

        // Right Side: Dynamic Stats Summary & OBS Button
        HBox headerRight = new HBox(12);
        headerRight.setAlignment(Pos.CENTER_RIGHT);

        lblTotalDiamondsVal = new Label("0");
        VBox pnlStatDiamonds = createMiniStatCard("TỔNG KIM CƯƠNG", lblTotalDiamondsVal, "#818cf8");

        lblActiveDonorsVal = new Label("0");
        VBox pnlStatDonors = createMiniStatCard("NHÀ TÀI TRỢ", lblActiveDonorsVal, "#e4e4e7");

        btnToggleOverlayTab2 = new Button("BẬT BẢNG XẾP HẠNG");
        btnToggleOverlayTab2.setPrefHeight(32);
        
        // Style initial state
        btnToggleOverlayTab2.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #a1a1aa;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        btnToggleOverlayTab2.setOnAction(e -> parent.toggleOverlayWindow());

        headerRight.getChildren().addAll(pnlStatDiamonds, pnlStatDonors, btnToggleOverlayTab2);
        cardHeader.getChildren().add(headerRight);
        
        cardLeaderboard.getChildren().add(cardHeader);

        // --- ACTION FILTER BAR (Vercel styled Search) ---
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 10, 0, 10));
        searchBox.setStyle(
            "-fx-background-color: #18181b;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
            "-fx-border-width: 1px;"
        );
        
        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconColor(Color.web("#71717a"));
        
        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm TikTok ID hoặc Tên hiển thị...");
        txtSearch.setPrefHeight(34);
        txtSearch.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #f4f4f5;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        searchBox.getChildren().addAll(searchIcon, txtSearch);

        // --- TABLE CONTAINER ---
        tblGifters = new TableView<>();
        tblGifters.setPrefHeight(380);
        tblGifters.setStyle(
            "-fx-background-color: #121214;" +
            "-fx-control-inner-background: #121214;" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );

        TableColumn<Gifter, Integer> colRank = new TableColumn<>("Hạng");
        colRank.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRank()).asObject());
        colRank.setPrefWidth(60);
        colRank.setStyle("-fx-alignment: CENTER; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        TableColumn<Gifter, String> colId = new TableColumn<>("TikTok ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(140);

        TableColumn<Gifter, String> colNick = new TableColumn<>("Tên Hiển Thị");
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(200);

        TableColumn<Gifter, Integer> colPoints = new TableColumn<>("Điểm (Kim cương)");
        colPoints.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getPoints()).asObject());
        colPoints.setPrefWidth(140);
        colPoints.setStyle("-fx-alignment: CENTER; -fx-text-fill: #e4e4e7; -fx-font-weight: bold;");

        tblGifters.getColumns().addAll(colRank, colId, colNick, colPoints);

        // Live Filtering Setup
        filteredList = new FilteredList<>(gifterList, p -> true);
        tblGifters.setItems(filteredList);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(g -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase().trim();
                return g.getUniqueId().toLowerCase().contains(lower) || 
                       g.getNickname().toLowerCase().contains(lower);
            });
        });

        cardLeaderboard.getChildren().addAll(searchBox, tblGifters);

        // --- ACTIONS TOOLBAR ---
        HBox actionsRow = new HBox(12);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(10, 0, 0, 0));

        btnDeleteSelected = new Button("Xoá Người Chọn");
        btnDeleteSelected.setPrefHeight(32);
        
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#a1a1aa"));
        btnDeleteSelected.setGraphic(trashIcon);
        
        btnDeleteSelected.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #a1a1aa;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        btnDeleteSelected.setOnAction(e -> deleteSelectedGifter());

        btnResetAll = new Button("Xoá Hết Bảng");
        btnResetAll.setPrefHeight(32);
        
        FontIcon refreshIcon = new FontIcon(Feather.REFRESH_CW);
        refreshIcon.setIconColor(Color.web("#f87171"));
        btnResetAll.setGraphic(refreshIcon);
        
        btnResetAll.setStyle(
            "-fx-background-color: rgba(239, 68, 68, 0.08);" +
            "-fx-text-fill: #f87171;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(239, 68, 68, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        btnResetAll.setOnAction(e -> resetLeaderboard());

        btnAddManual = new Button("Cộng Điểm Thủ Công");
        btnAddManual.setPrefHeight(32);
        
        FontIcon plusIcon = new FontIcon(Feather.PLUS_CIRCLE);
        plusIcon.setIconColor(Color.web("#818cf8"));
        btnAddManual.setGraphic(plusIcon);
        
        btnAddManual.setStyle(
            "-fx-background-color: rgba(99, 102, 241, 0.08);" +
            "-fx-text-fill: #818cf8;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(99, 102, 241, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-width: 1px;"
        );
        btnAddManual.setOnAction(e -> addManualPoints());

        actionsRow.getChildren().addAll(btnDeleteSelected, btnResetAll, btnAddManual);
        cardLeaderboard.getChildren().add(actionsRow);

        setCenter(cardLeaderboard);
    }

    private VBox createMiniStatCard(String label, Label valueLabel, String accentHex) {
        VBox pnl = new VBox(2);
        pnl.setPadding(new Insets(4, 12, 4, 12));
        pnl.setAlignment(Pos.CENTER_LEFT);
        pnl.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.02);" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.06);" +
            "-fx-border-radius: 10px;" +
            "-fx-border-width: 1px;"
        );

        Label lblTitle = new Label(label);
        lblTitle.setStyle("-fx-font-size: 8.5px; -fx-font-weight: bold; -fx-text-fill: #71717a;");

        valueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + accentHex + ";");

        pnl.getChildren().addAll(lblTitle, valueLabel);
        return pnl;
    }

    public void refreshTableData() {
        // Throttle rapid calls — restart the 500ms timer each time
        pendingRefresh = true;
        refreshThrottle.playFromStart();
    }

    private void doRefreshTableData() {
        pendingRefresh = false;
        List<Gifter> source = DataManager.getGifters();
        int totalDiamonds = 0;

        // --- Incremental update: avoid clear() to prevent TableView flicker ---
        Map<String, Gifter> currentMap = new HashMap<>();
        for (Gifter g : gifterList) {
            currentMap.put(g.getUniqueId().toLowerCase(), g);
        }

        // Pass 1: update existing or add new items
        int rank = 1;
        for (Gifter fresh : source) {
            fresh.setRank(rank++);
            totalDiamonds += fresh.getPoints();
            String key = fresh.getUniqueId().toLowerCase();
            Gifter existing = currentMap.get(key);
            if (existing != null) {
                // Update in-place
                existing.setRank(fresh.getRank());
                existing.setPoints(fresh.getPoints());
                existing.setNickname(fresh.getNickname());
                existing.setAvatarUrl(fresh.getAvatarUrl());
                currentMap.remove(key);
            } else {
                gifterList.add(fresh);
            }
        }

        // Pass 2: remove items no longer in source
        if (!currentMap.isEmpty()) {
            gifterList.removeIf(g -> currentMap.containsKey(g.getUniqueId().toLowerCase()));
        }

        // Pass 3: re-sort to match points desc order
        FXCollections.sort(gifterList);

        // Pass 4: force TableView redraw (plain fields, not JavaFX Properties)
        tblGifters.refresh();

        // Update statistics cards
        if (lblTotalDiamondsVal != null) {
            lblTotalDiamondsVal.setText(String.format("%,d", totalDiamonds));
        }
        if (lblActiveDonorsVal != null) {
            lblActiveDonorsVal.setText(String.format("%,d", source.size()));
        }
    }

    private void deleteSelectedGifter() {
        Gifter selected = tblGifters.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.warning(parent, "Cảnh báo", "Vui lòng chọn người cần xoá!");
            return;
        }

        if (Dialogs.confirm(parent, "Xác nhận xoá", "Bạn có chắc muốn xoá người dùng @" + selected.getUniqueId() + " khỏi bảng xếp hạng?", "Xoá")) {
            synchronized (DataManager.class) {
                List<Gifter> list = DataManager.getGifters();
                list.removeIf(g -> g.getUniqueId().equalsIgnoreCase(selected.getUniqueId()));
                DataManager.save();
            }
            refreshTableData();
            parent.updateLeaderboardOverlay();
        }
    }

    private void resetLeaderboard() {
        if (Dialogs.confirm(parent, "Xác nhận xoá sạch", "Hành động này sẽ xoá sạch bảng xếp hạng hiện tại. Bạn có muốn tiếp tục?", "Xoá sạch")) {
            synchronized (DataManager.class) {
                DataManager.getGifters().clear();
                DataManager.save();
            }
            refreshTableData();
            parent.updateLeaderboardOverlay();
        }
    }

    private void addManualPoints() {
        Optional<String> idResult = Dialogs.input(parent, "Cộng điểm thủ công", "Nhập TikTok ID (ví dụ: user123):", "TikTok ID:", "");
        if (idResult.isEmpty() || idResult.get().trim().isEmpty()) return;
        String uniqueId = idResult.get().trim();

        Optional<String> nickResult = Dialogs.input(parent, "Cộng điểm thủ công", "Nhập Tên Hiển Thị (không bắt buộc):", "Tên:", uniqueId);
        String nickname = nickResult.orElse("").trim();
        if (nickname.isEmpty()) nickname = uniqueId;

        Optional<String> pointsResult = Dialogs.input(parent, "Cộng điểm thủ công", "Nhập số Kim cương cần cộng (hoặc trừ nếu nhập số âm):", "Kim cương:", "100");
        if (pointsResult.isEmpty() || pointsResult.get().trim().isEmpty()) return;

        int points;
        try {
            points = Integer.parseInt(pointsResult.get().trim());
        } catch (NumberFormatException e) {
            Dialogs.error(parent, "Lỗi", "Số kim cương không hợp lệ!");
            return;
        }

        final String finalUniqueId = uniqueId;
        final String finalNickname = nickname;
        final int finalPoints = points;

        synchronized (DataManager.class) {
            List<Gifter> list = DataManager.getGifters();
            Optional<Gifter> existing = list.stream()
                    .filter(g -> g.getUniqueId().equalsIgnoreCase(finalUniqueId))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().addPoints(finalPoints);
                existing.get().setNickname(finalNickname);
            } else {
                list.add(new Gifter(finalUniqueId, finalNickname, null, Math.max(0, finalPoints)));
            }

            Collections.sort(list);
            DataManager.save();
        }

        refreshTableData();
        parent.updateLeaderboardOverlay();
    }

    public void updateOverlayButtonState(boolean isOpen) {
        if (isOpen) {
            btnToggleOverlayTab2.setText("TẮT BẢNG XẾP HẠNG");
            btnToggleOverlayTab2.setStyle(
                "-fx-background-color: rgba(99, 102, 241, 0.12);" +
                "-fx-text-fill: #818cf8;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: rgba(99, 102, 241, 0.4);" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-width: 1px;"
            );
        } else {
            btnToggleOverlayTab2.setText("BẬT BẢNG XẾP HẠNG");
            btnToggleOverlayTab2.setStyle(
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
