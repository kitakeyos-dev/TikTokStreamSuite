package com.leaderboard.ui.tab;

import com.leaderboard.model.Gifter;
import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.I18n;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;
import java.util.ArrayList;
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
    private final ObservableList<Gifter> gifterList = FXCollections.observableArrayList();
    private FilteredList<Gifter> filteredList;

    private TextField txtSearch;
    private Label lblTotalDiamondsVal;
    private Label lblActiveDonorsVal;
    
    private Button btnDeleteSelected;
    private Button btnResetAll;
    private Button btnAddManual;

    // Throttle: limit refreshes to once per 500ms to avoid flicker from rapid events
    private final PauseTransition refreshThrottle = new PauseTransition(Duration.millis(500));
    private boolean pendingRefresh = false;

    public LeaderboardTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);
        // Setup throttle: when it fires, do the actual refresh
        refreshThrottle.setOnFinished(e -> doRefreshTableData());
        initComponents();
        refreshTableData();
    }

    private void initComponents() {
        VBox cardLeaderboard = DashboardLayout.createPageContainer();

        lblTotalDiamondsVal = new Label("0");
        lblActiveDonorsVal = new Label("0");

        HBox headerRight = DashboardLayout.createHeaderActions(
                DashboardLayout.createMiniStatCard(I18n.get("leaderboard.stat.diamonds"), lblTotalDiamondsVal, "#818cf8"),
                DashboardLayout.createMiniStatCard(I18n.get("leaderboard.stat.donors"), lblActiveDonorsVal, "#e4e4e7")
        );

        cardLeaderboard.getChildren().add(DashboardLayout.createPageHeader(
                I18n.get("leaderboard.title"),
                I18n.get("leaderboard.subtitle"),
                headerRight
        ));

        txtSearch = DashboardLayout.newSearchField();
        cardLeaderboard.getChildren().add(DashboardLayout.createSearchBox(
                txtSearch, I18n.get("leaderboard.prompt.search")));

        tblGifters = DashboardLayout.createTable();

        TableColumn<Gifter, Integer> colRank = new TableColumn<>(I18n.get("leaderboard.col.rank"));
        colRank.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRank()).asObject());
        colRank.setPrefWidth(60);
        colRank.setStyle("-fx-alignment: CENTER; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        TableColumn<Gifter, String> colId = new TableColumn<>(I18n.get("leaderboard.col.id"));
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(140);

        TableColumn<Gifter, String> colNick = new TableColumn<>(I18n.get("leaderboard.col.nick"));
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(200);

        TableColumn<Gifter, Integer> colPoints = new TableColumn<>(I18n.get("leaderboard.col.points"));
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

        cardLeaderboard.getChildren().add(tblGifters);

        btnDeleteSelected = DashboardLayout.newButton(I18n.get("leaderboard.btn.delete"));
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#a1a1aa"));
        btnDeleteSelected.setGraphic(trashIcon);
        DashboardLayout.applySecondaryButton(btnDeleteSelected);
        btnDeleteSelected.setOnAction(e -> deleteSelectedGifter());

        btnResetAll = DashboardLayout.newButton(I18n.get("leaderboard.btn.reset"));
        FontIcon refreshIcon = new FontIcon(Feather.REFRESH_CW);
        refreshIcon.setIconColor(Color.web("#f87171"));
        btnResetAll.setGraphic(refreshIcon);
        DashboardLayout.applyDangerButton(btnResetAll);
        btnResetAll.setOnAction(e -> resetLeaderboard());

        btnAddManual = DashboardLayout.newButton(I18n.get("leaderboard.btn.add"));
        FontIcon plusIcon = new FontIcon(Feather.PLUS_CIRCLE);
        plusIcon.setIconColor(Color.web("#818cf8"));
        btnAddManual.setGraphic(plusIcon);
        DashboardLayout.applyPrimaryButton(btnAddManual);
        btnAddManual.setOnAction(e -> addManualPoints());

        cardLeaderboard.getChildren().add(DashboardLayout.createActionsRow(
                btnDeleteSelected, btnResetAll, btnAddManual));

        setCenter(cardLeaderboard);
    }

    public void refreshTableData() {
        // Throttle rapid calls — restart the 500ms timer each time
        pendingRefresh = true;
        refreshThrottle.playFromStart();
    }

    private void doRefreshTableData() {
        pendingRefresh = false;
        List<Gifter> source;
        synchronized (DataManager.class) {
            source = new ArrayList<>(DataManager.getGifters());
        }
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
            Dialogs.warning(parent, I18n.get("dialog.warning"), I18n.get("leaderboard.warn.select"));
            return;
        }

        if (Dialogs.confirm(parent, I18n.get("leaderboard.confirm.delete.title"), I18n.get("leaderboard.confirm.delete.msg", selected.getUniqueId()), I18n.get("leaderboard.confirm.delete.btn"))) {
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
        if (Dialogs.confirm(parent, I18n.get("leaderboard.confirm.reset.title"), I18n.get("leaderboard.confirm.reset.msg"), I18n.get("leaderboard.confirm.reset.btn"))) {
            synchronized (DataManager.class) {
                DataManager.getGifters().clear();
                DataManager.save();
            }
            refreshTableData();
            parent.updateLeaderboardOverlay();
        }
    }

    private void addManualPoints() {
        Optional<String> idResult = Dialogs.input(parent, I18n.get("leaderboard.manual.title"), I18n.get("leaderboard.manual.id.prompt"), I18n.get("leaderboard.manual.id.label"), "");
        if (idResult.isEmpty() || idResult.get().trim().isEmpty()) return;
        String uniqueId = idResult.get().trim();

        Optional<String> nickResult = Dialogs.input(parent, I18n.get("leaderboard.manual.title"), I18n.get("leaderboard.manual.nick.prompt"), I18n.get("leaderboard.manual.nick.label"), uniqueId);
        String nickname = nickResult.orElse("").trim();
        if (nickname.isEmpty()) nickname = uniqueId;

        Optional<String> pointsResult = Dialogs.input(parent, I18n.get("leaderboard.manual.title"), I18n.get("leaderboard.manual.points.prompt"), I18n.get("leaderboard.manual.points.label"), "100");
        if (pointsResult.isEmpty() || pointsResult.get().trim().isEmpty()) return;

        int points;
        try {
            points = Integer.parseInt(pointsResult.get().trim());
        } catch (NumberFormatException e) {
            Dialogs.error(parent, I18n.get("dialog.error"), I18n.get("leaderboard.manual.points.invalid"));
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
}
