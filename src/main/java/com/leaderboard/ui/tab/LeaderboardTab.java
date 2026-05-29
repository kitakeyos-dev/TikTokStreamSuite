package com.leaderboard.ui.tab;

import com.leaderboard.model.TikTokUser;
import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.I18n;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * LeaderboardTab displaying top gifters.
 * Extends BaseDataTab utilizing the unified TikTokUser domain entity.
 */
public class LeaderboardTab extends BaseDataTab<TikTokUser> {
    private final Label lblTotalDiamondsVal;
    private final Label lblActiveDonorsVal;
    
    private Button btnDeleteSelected;
    private Button btnResetAll;
    private Button btnAddManual;

    // Throttle: limit refreshes to once per 500ms to avoid flicker
    private final PauseTransition refreshThrottle = new PauseTransition(Duration.millis(500));
    private boolean pendingRefresh = false;

    public LeaderboardTab(DashboardStage parent) {
        super(parent);
        
        refreshThrottle.setOnFinished(e -> doRefreshTableData());

        lblTotalDiamondsVal = new Label("0");
        lblActiveDonorsVal = new Label("0");

        HBox headerRight = DashboardLayout.createHeaderActions(
                DashboardLayout.createMiniStatCard(I18n.get("leaderboard.stat.diamonds"), lblTotalDiamondsVal, "#818cf8"),
                DashboardLayout.createMiniStatCard(I18n.get("leaderboard.stat.donors"), lblActiveDonorsVal, "#e4e4e7")
        );

        setupTableColumns();
        buildLayout("leaderboard.title", "leaderboard.subtitle", "leaderboard.prompt.search", headerRight);
        refreshTableData();
    }

    @Override
    protected void setupTableColumns() {
        TableColumn<TikTokUser, Integer> colRank = new TableColumn<>(I18n.get("leaderboard.col.rank"));
        colRank.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getRank()).asObject());
        colRank.setPrefWidth(60);
        colRank.setStyle("-fx-alignment: CENTER; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        TableColumn<TikTokUser, String> colId = new TableColumn<>(I18n.get("leaderboard.col.id"));
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(140);

        TableColumn<TikTokUser, String> colNick = new TableColumn<>(I18n.get("leaderboard.col.nick"));
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(200);

        TableColumn<TikTokUser, Integer> colPoints = new TableColumn<>(I18n.get("leaderboard.col.points"));
        colPoints.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getGiftPoints()).asObject());
        colPoints.setPrefWidth(140);
        colPoints.setStyle("-fx-alignment: CENTER; -fx-text-fill: #e4e4e7; -fx-font-weight: bold;");

        tableView.getColumns().addAll(colRank, colId, colNick, colPoints);
    }

    @Override
    protected boolean matchesSearch(TikTokUser g, String query) {
        return g.getUniqueId().toLowerCase().contains(query) || 
               g.getNickname().toLowerCase().contains(query);
    }

    @Override
    protected Pane buildFooterActions() {
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

        return DashboardLayout.createActionsRow(btnDeleteSelected, btnResetAll, btnAddManual);
    }

    public void refreshTableData() {
        pendingRefresh = true;
        refreshThrottle.playFromStart();
    }

    private void doRefreshTableData() {
        pendingRefresh = false;
        List<TikTokUser> source;
        synchronized (DataManager.class) {
            source = new ArrayList<>(DataManager.getGifters());
        }
        int totalDiamonds = 0;

        // Incremental update to prevent TableView flicker
        Map<String, TikTokUser> currentMap = new HashMap<>();
        for (TikTokUser g : masterList) {
            currentMap.put(g.getUniqueId().toLowerCase(), g);
        }

        int rank = 1;
        for (TikTokUser fresh : source) {
            fresh.setRank(rank++);
            totalDiamonds += fresh.getGiftPoints();
            String key = fresh.getUniqueId().toLowerCase();
            TikTokUser existing = currentMap.get(key);
            if (existing != null) {
                existing.setRank(fresh.getRank());
                existing.setGiftPoints(fresh.getGiftPoints());
                existing.setNickname(fresh.getNickname());
                existing.setAvatarUrl(fresh.getAvatarUrl());
                existing.setBadgeUrls(fresh.getBadgeUrls());
                currentMap.remove(key);
            } else {
                masterList.add(fresh);
            }
        }

        if (!currentMap.isEmpty()) {
            masterList.removeIf(g -> currentMap.containsKey(g.getUniqueId().toLowerCase()));
        }

        // Re-sort descending by gift points
        masterList.sort((u1, u2) -> {
            int diff = Integer.compare(u2.getGiftPoints(), u1.getGiftPoints());
            if (diff != 0) return diff;
            return u1.getNickname().compareToIgnoreCase(u2.getNickname());
        });

        tableView.refresh();

        if (lblTotalDiamondsVal != null) {
            lblTotalDiamondsVal.setText(String.format("%,d", totalDiamonds));
        }
        if (lblActiveDonorsVal != null) {
            lblActiveDonorsVal.setText(String.format("%,d", source.size()));
        }
    }

    private void deleteSelectedGifter() {
        TikTokUser selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.warning(parent, I18n.get("dialog.warning"), I18n.get("leaderboard.warn.select"));
            return;
        }

        if (Dialogs.confirm(parent, I18n.get("leaderboard.confirm.delete.title"), I18n.get("leaderboard.confirm.delete.msg", selected.getUniqueId()), I18n.get("leaderboard.confirm.delete.btn"))) {
            synchronized (DataManager.class) {
                // To keep database clean, if they delete it, we reset giftPoints to 0.
                // If the user has no other stats left, we can fully delete them.
                TikTokUser user = DataManager.findOrCreateUser(selected);
                if (user != null) {
                    user.setGiftPoints(0);
                    DataManager.getUsers().removeIf(u -> u.getGiftPoints() <= 0 && u.getLikesSent() <= 0 && u.getTeamName() == null && !u.isSubscriber());
                    DataManager.save();
                }
            }
            refreshTableData();
            parent.updateLeaderboardOverlay();
        }
    }

    private void resetLeaderboard() {
        if (Dialogs.confirm(parent, I18n.get("leaderboard.confirm.reset.title"), I18n.get("leaderboard.confirm.reset.msg"), I18n.get("leaderboard.confirm.reset.btn"))) {
            synchronized (DataManager.class) {
                for (TikTokUser u : DataManager.getUsers()) {
                    u.setGiftPoints(0);
                }
                DataManager.getUsers().removeIf(u -> u.getGiftPoints() <= 0 && u.getLikesSent() <= 0 && u.getTeamName() == null && !u.isSubscriber());
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
            TikTokUser incoming = new TikTokUser(finalUniqueId, finalNickname, null);
            TikTokUser user = DataManager.findOrCreateUser(incoming);
            user.addGiftPoints(finalPoints);
            DataManager.save();
        }

        refreshTableData();
        parent.updateLeaderboardOverlay();
    }
}
