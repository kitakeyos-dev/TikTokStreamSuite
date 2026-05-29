package com.leaderboard.ui.tab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leaderboard.model.TikTokUser;
import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.I18n;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TeamTab displaying fan club members and subscribers.
 * Extends BaseDataTab utilizing the unified TikTokUser domain entity.
 */
public class TeamTab extends BaseDataTab<TikTokUser> {
    private ComboBox<String> cbFilter;

    private final Label lblTotalMembersVal;
    private final Label lblTotalSubsVal;
    private final Label lblTotalFanClubVal;

    private Button btnDeleteSelected;
    private Button btnResetAll;
    private Button btnExportJson;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public TeamTab(DashboardStage parent) {
        super(parent);
        
        lblTotalMembersVal = new Label("0");
        lblTotalSubsVal = new Label("0");
        lblTotalFanClubVal = new Label("0");

        HBox headerRight = DashboardLayout.createHeaderActions(
                DashboardLayout.createMiniStatCard(I18n.get("team.stat.members"), lblTotalMembersVal, "#e4e4e7"),
                DashboardLayout.createMiniStatCard(I18n.get("team.stat.subs"), lblTotalSubsVal, "#818cf8"),
                DashboardLayout.createMiniStatCard(I18n.get("team.stat.fancollective"), lblTotalFanClubVal, "#a1a1aa")
        );

        setupTableColumns();
        buildLayout("team.title", "team.subtitle", "team.prompt.search", headerRight);

        // Combined filter listener
        Runnable applyFilter = () -> filteredList.setPredicate(m -> {
            String text = txtSearch.getText().toLowerCase().trim();
            boolean textMatches = text.isEmpty() ||
                                  m.getUniqueId().toLowerCase().contains(text) ||
                                  m.getNickname().toLowerCase().contains(text);

            if (!textMatches) return false;

            int filterIdx = cbFilter.getSelectionModel().getSelectedIndex();
            if (filterIdx == 1) { // Only Subscribers
                return m.isSubscriber();
            } else if (filterIdx == 2) { // Only Fan Club
                return m.getTeamName() != null;
            }

            return true;
        });

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter.run());
        cbFilter.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> applyFilter.run());

        refreshTableData();
    }

    @Override
    protected Node buildSearchBar(String searchPromptKey) {
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        HBox searchBox = DashboardLayout.createSearchBox(txtSearch, I18n.get(searchPromptKey));
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        cbFilter = new ComboBox<>(FXCollections.observableArrayList(
                I18n.get("team.filter.all"), I18n.get("team.filter.subs"), I18n.get("team.filter.fanclub")));
        cbFilter.getSelectionModel().select(0);
        cbFilter.setPrefWidth(180);
        DashboardLayout.styleComboBox(cbFilter);

        filterBar.getChildren().addAll(searchBox, cbFilter);
        return filterBar;
    }

    @Override
    protected void setupTableColumns() {
        TableColumn<TikTokUser, Integer> colStt = new TableColumn<>(I18n.get("team.col.stt"));
        colStt.setCellValueFactory(cell -> new SimpleIntegerProperty(masterList.indexOf(cell.getValue()) + 1).asObject());
        colStt.setPrefWidth(50);
        colStt.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        TableColumn<TikTokUser, String> colId = new TableColumn<>(I18n.get("team.col.id"));
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(120);

        TableColumn<TikTokUser, String> colNick = new TableColumn<>(I18n.get("team.col.nick"));
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(160);

        TableColumn<TikTokUser, String> colRole = new TableColumn<>(I18n.get("team.col.role"));
        colRole.setCellValueFactory(cell -> {
            TikTokUser m = cell.getValue();
            String role = I18n.get("team.role.user");
            if (m.isSubscriber() && m.getTeamName() != null) {
                role = I18n.get("team.role.subfan");
            } else if (m.isSubscriber()) {
                role = I18n.get("team.role.sub");
            } else if (m.getTeamName() != null) {
                role = m.getTeamName();
            }
            return new SimpleStringProperty(role);
        });
        colRole.setPrefWidth(140);
        colRole.setStyle("-fx-alignment: CENTER; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        TableColumn<TikTokUser, String> colTeamLvl = new TableColumn<>(I18n.get("team.col.teamlvl"));
        colTeamLvl.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getTeamLevel() > 0 ? I18n.get("team.level.prefix", cell.getValue().getTeamLevel()) : "--"
        ));
        colTeamLvl.setPrefWidth(80);
        colTeamLvl.setStyle("-fx-alignment: CENTER; -fx-text-fill: #a1a1aa; -fx-font-weight: bold;");

        TableColumn<TikTokUser, String> colGiftLvl = new TableColumn<>(I18n.get("team.col.giftlvl"));
        colGiftLvl.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getGiftGiverLevel() > 0 ? I18n.get("team.level.prefix", cell.getValue().getGiftGiverLevel()) : "--"
        ));
        colGiftLvl.setPrefWidth(90);
        colGiftLvl.setStyle("-fx-alignment: CENTER; -fx-text-fill: #e4e4e7; -fx-font-weight: bold;");

        TableColumn<TikTokUser, String> colLastActive = new TableColumn<>(I18n.get("team.col.lastactive"));
        colLastActive.setCellValueFactory(cell -> new SimpleStringProperty(
            TIME_FORMAT.format(new Date(cell.getValue().getLastActive()))
        ));
        colLastActive.setPrefWidth(110);
        colLastActive.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        tableView.getColumns().addAll(colStt, colId, colNick, colRole, colTeamLvl, colGiftLvl, colLastActive);
    }

    @Override
    protected boolean matchesSearch(TikTokUser item, String query) {
        return true;
    }

    @Override
    protected Pane buildFooterActions() {
        btnDeleteSelected = DashboardLayout.newButton(I18n.get("team.btn.delete"));
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#a1a1aa"));
        btnDeleteSelected.setGraphic(trashIcon);
        DashboardLayout.applySecondaryButton(btnDeleteSelected);
        btnDeleteSelected.setOnAction(e -> deleteSelectedMember());

        btnResetAll = DashboardLayout.newButton(I18n.get("team.btn.reset"));
        FontIcon refreshIcon = new FontIcon(Feather.REFRESH_CW);
        refreshIcon.setIconColor(Color.web("#f87171"));
        btnResetAll.setGraphic(refreshIcon);
        DashboardLayout.applyDangerButton(btnResetAll);
        btnResetAll.setOnAction(e -> resetAllMembers());

        btnExportJson = DashboardLayout.newButton(I18n.get("team.btn.export"));
        FontIcon exportIcon = new FontIcon(Feather.DOWNLOAD);
        exportIcon.setIconColor(Color.web("#818cf8"));
        btnExportJson.setGraphic(exportIcon);
        DashboardLayout.applyPrimaryButton(btnExportJson);
        btnExportJson.setOnAction(e -> exportToJson());

        return DashboardLayout.createActionsRow(btnDeleteSelected, btnResetAll, btnExportJson);
    }

    public void refreshTableData() {
        masterList.clear();
        List<TikTokUser> list;
        synchronized (DataManager.class) {
            list = new ArrayList<>(DataManager.getTeamMembers());
        }
        int subsCount = 0;
        int fanClubCount = 0;

        for (TikTokUser m : list) {
            masterList.add(m);
            if (m.isSubscriber()) subsCount++;
            if (m.getTeamName() != null) fanClubCount++;
        }

        if (lblTotalMembersVal != null) {
            lblTotalMembersVal.setText(String.format("%,d", list.size()));
        }
        if (lblTotalSubsVal != null) {
            lblTotalSubsVal.setText(String.format("%,d", subsCount));
        }
        if (lblTotalFanClubVal != null) {
            lblTotalFanClubVal.setText(String.format("%,d", fanClubCount));
        }
    }

    private void deleteSelectedMember() {
        TikTokUser selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.warning(parent, I18n.get("dialog.warning"), I18n.get("team.warn.select"));
            return;
        }

        if (Dialogs.confirm(parent, I18n.get("dialog.confirm"), I18n.get("team.confirm.delete.msg", selected.getUniqueId()), I18n.get("team.confirm.delete.btn"))) {
            synchronized (DataManager.class) {
                TikTokUser user = DataManager.findOrCreateUser(selected);
                if (user != null) {
                    user.setTeamName(null);
                    user.setTeamLevel(0);
                    user.setSubscriber(false);
                    DataManager.getUsers().removeIf(u -> u.getGiftPoints() <= 0 && u.getLikesSent() <= 0 && u.getTeamName() == null && !u.isSubscriber());
                    DataManager.save();
                }
            }
            refreshTableData();
        }
    }

    private void resetAllMembers() {
        if (Dialogs.confirm(parent, I18n.get("team.confirm.reset.title"), I18n.get("team.confirm.reset.msg"), I18n.get("team.confirm.reset.btn"))) {
            synchronized (DataManager.class) {
                for (TikTokUser u : DataManager.getUsers()) {
                    u.setTeamName(null);
                    u.setTeamLevel(0);
                    u.setSubscriber(false);
                }
                DataManager.getUsers().removeIf(u -> u.getGiftPoints() <= 0 && u.getLikesSent() <= 0 && u.getTeamName() == null && !u.isSubscriber());
                DataManager.save();
            }
            refreshTableData();
        }
    }

    private void exportToJson() {
        List<TikTokUser> exportList = new ArrayList<>(tableView.getItems());

        if (exportList.isEmpty()) {
            Dialogs.info(parent, I18n.get("dialog.success"), I18n.get("team.export.empty"));
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.get("team.export.title"));
        fileChooser.setInitialFileName(I18n.get("team.export.filename"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON File (*.json)", "*.json"));

        File fileToSave = fileChooser.showSaveDialog(parent);
        if (fileToSave != null) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileToSave), StandardCharsets.UTF_8)) {
                GSON.toJson(exportList, writer);
                Dialogs.info(parent, I18n.get("dialog.success"), I18n.get("team.export.success", exportList.size(), fileToSave.getAbsolutePath()));
            } catch (Exception ex) {
                Dialogs.error(parent, I18n.get("dialog.error"), I18n.get("dialog.error") + ": " + ex.getMessage());
            }
        }
    }
}
