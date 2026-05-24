package com.leaderboard.ui.tab;

import com.leaderboard.model.Gifter;
import com.leaderboard.ui.DashboardStage;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public LeaderboardTab(DashboardStage parent) {
        this.parent = parent;
        setPadding(new Insets(15, 5, 15, 5));
        setStyle("-fx-background-color: transparent;");
        initComponents();
        refreshTableData();
    }

    private void initComponents() {
        VBox cardLeaderboard = new VBox(15);
        cardLeaderboard.setPadding(new Insets(15, 20, 15, 20));
        cardLeaderboard.setStyle(
            "-fx-background-color: #1e1e1e;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
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
        lblTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label lblSubtitle = new Label("Quản lý danh sách người ủng hộ tích lũy và tổng số kim cương nhận được.");
        lblSubtitle.setStyle("-fx-text-fill: #71717a; -fx-font-size: 10px;");
        
        titleArea.getChildren().addAll(lblTitle, lblSubtitle);
        cardHeader.getChildren().add(titleArea);

        // Right Side: Dynamic Stats Summary & OBS Button
        HBox headerRight = new HBox(15);
        headerRight.setAlignment(Pos.CENTER_RIGHT);

        lblTotalDiamondsVal = new Label("0");
        VBox pnlStatDiamonds = createMiniStatCard("TỔNG KIM CƯƠNG", lblTotalDiamondsVal, "#25f4ee");

        lblActiveDonorsVal = new Label("0");
        VBox pnlStatDonors = createMiniStatCard("NHÀ TÀI TRỢ", lblActiveDonorsVal, "#fe2c55");

        btnToggleOverlayTab2 = new Button("BẬT BẢNG XẾP HẠNG");
        btnToggleOverlayTab2.setPrefHeight(32);
        btnToggleOverlayTab2.setStyle(
            "-fx-background-color: rgba(37, 244, 238, 0.1);" +
            "-fx-text-fill: #25f4ee;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(37, 244, 238, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );
        btnToggleOverlayTab2.setOnAction(e -> parent.toggleOverlayWindow());

        headerRight.getChildren().addAll(pnlStatDiamonds, pnlStatDonors, btnToggleOverlayTab2);
        cardHeader.getChildren().add(headerRight);
        
        cardLeaderboard.getChildren().add(cardHeader);

        // --- ACTION FILTER BAR ---
        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm TikTok ID hoặc Tên hiển thị...");
        txtSearch.setPrefHeight(36);
        txtSearch.setStyle("-fx-background-radius: 8px;");

        // --- TABLE CONTAINER ---
        tblGifters = new TableView<>();
        tblGifters.setPrefHeight(380);
        tblGifters.setStyle(
            "-fx-background-color: #1e1e1e;" +
            "-fx-control-inner-background: #1e1e1e;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );

        TableColumn<Gifter, Integer> colRank = new TableColumn<>("Hạng");
        colRank.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRank()).asObject());
        colRank.setPrefWidth(60);
        colRank.setStyle("-fx-alignment: CENTER; -fx-text-fill: #25f4ee; -fx-font-weight: bold;");

        TableColumn<Gifter, String> colId = new TableColumn<>("TikTok ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(140);

        TableColumn<Gifter, String> colNick = new TableColumn<>("Tên Hiển Thị");
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(200);

        TableColumn<Gifter, Integer> colPoints = new TableColumn<>("Điểm (Kim cương)");
        colPoints.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getPoints()).asObject());
        colPoints.setPrefWidth(140);
        colPoints.setStyle("-fx-alignment: CENTER; -fx-text-fill: #fe2c55; -fx-font-weight: bold;");

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

        cardLeaderboard.getChildren().addAll(txtSearch, tblGifters);

        // --- ACTIONS TOOLBAR ---
        HBox actionsRow = new HBox(15);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(10, 0, 0, 0));

        btnDeleteSelected = new Button("Xoá Người Chọn");
        btnDeleteSelected.setPrefHeight(32);
        btnDeleteSelected.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.05);" +
            "-fx-text-fill: #e9e2e1;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;"
        );
        btnDeleteSelected.setOnAction(e -> deleteSelectedGifter());

        btnResetAll = new Button("Xoá Hết Bảng");
        btnResetAll.setPrefHeight(32);
        btnResetAll.setStyle(
            "-fx-background-color: #fe2c55;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;"
        );
        btnResetAll.setOnAction(e -> resetLeaderboard());

        btnAddManual = new Button("Cộng Điểm Thủ Công");
        btnAddManual.setPrefHeight(32);
        btnAddManual.setStyle(
            "-fx-background-color: #25f4ee;" +
            "-fx-text-fill: #131313;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;"
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
            "-fx-background-color: rgba(30, 30, 30, 0.8);" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.15);" +
            "-fx-border-radius: 10px;" +
            "-fx-border-width: 1px;"
        );

        Label lblTitle = new Label(label);
        lblTitle.setStyle("-fx-font-size: 8.5px; -fx-font-weight: bold; -fx-text-fill: #a1a1a8;");

        valueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + accentHex + ";");

        pnl.getChildren().addAll(lblTitle, valueLabel);
        return pnl;
    }

    public void refreshTableData() {
        gifterList.clear();
        List<Gifter> list = DataManager.getGifters();
        
        int rank = 1;
        int totalDiamonds = 0;
        
        for (Gifter g : list) {
            g.setRank(rank);
            gifterList.add(g);
            totalDiamonds += g.getPoints();
            rank++;
        }

        // Update statistics cards
        if (lblTotalDiamondsVal != null) {
            lblTotalDiamondsVal.setText(String.format("%,d", totalDiamonds));
        }
        if (lblActiveDonorsVal != null) {
            lblActiveDonorsVal.setText(String.format("%,d", list.size()));
        }
    }

    private void deleteSelectedGifter() {
        Gifter selected = tblGifters.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn người cần xoá!", ButtonType.OK);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Bạn có chắc muốn xoá người dùng @" + selected.getUniqueId() + " khỏi bảng xếp hạng?", 
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xoá");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
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
        Alert confirm = new Alert(Alert.AlertType.WARNING, 
            "CẢNH BÁO: Hành động này sẽ xoá SẠCH bảng xếp hạng hiện tại! Bạn có muốn tiếp tục?", 
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xoá sạch");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            synchronized (DataManager.class) {
                DataManager.getGifters().clear();
                DataManager.save();
            }
            refreshTableData();
            parent.updateLeaderboardOverlay();
        }
    }

    private void addManualPoints() {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Cộng điểm thủ công");
        idDialog.setHeaderText("Nhập TikTok ID (ví dụ: user123):");
        idDialog.setContentText("TikTok ID:");
        Optional<String> idResult = idDialog.showAndWait();
        if (idResult.isEmpty() || idResult.get().trim().isEmpty()) return;
        String uniqueId = idResult.get().trim();

        TextInputDialog nickDialog = new TextInputDialog(uniqueId);
        nickDialog.setTitle("Cộng điểm thủ công");
        nickDialog.setHeaderText("Nhập Tên Hiển Thị (không bắt buộc):");
        nickDialog.setContentText("Tên:");
        Optional<String> nickResult = nickDialog.showAndWait();
        String nickname = nickResult.orElse("").trim();
        if (nickname.isEmpty()) nickname = uniqueId;

        TextInputDialog pointsDialog = new TextInputDialog("100");
        pointsDialog.setTitle("Cộng điểm thủ công");
        pointsDialog.setHeaderText("Nhập số Kim cương cần cộng (hoặc trừ nếu nhập số âm):");
        pointsDialog.setContentText("Kim cương:");
        Optional<String> pointsResult = pointsDialog.showAndWait();
        if (pointsResult.isEmpty() || pointsResult.get().trim().isEmpty()) return;

        int points;
        try {
            points = Integer.parseInt(pointsResult.get().trim());
        } catch (NumberFormatException e) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Số kim cương không hợp lệ!", ButtonType.OK);
            err.setTitle("Lỗi");
            err.setHeaderText(null);
            err.showAndWait();
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
                "-fx-background-color: #fe2c55;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;"
            );
        } else {
            btnToggleOverlayTab2.setText("BẬT BẢNG XẾP HẠNG");
            btnToggleOverlayTab2.setStyle(
                "-fx-background-color: rgba(37, 244, 238, 0.1);" +
                "-fx-text-fill: #25f4ee;" +
                "-fx-font-weight: bold;" +
                "-fx-border-color: rgba(37, 244, 238, 0.4);" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;"
            );
        }
    }
}
