package com.leaderboard.ui.tab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leaderboard.model.TeamMember;
import com.leaderboard.ui.DashboardLayout;
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
import java.util.Optional;

public class TeamTab extends BorderPane {
    private final DashboardStage parent;
    private TableView<TeamMember> tblMembers;
    private ObservableList<TeamMember> memberList = FXCollections.observableArrayList();
    private FilteredList<TeamMember> filteredList;

    private TextField txtSearch;
    private ComboBox<String> cbFilter;

    private Label lblTotalMembersVal;
    private Label lblTotalSubsVal;
    private Label lblTotalFanClubVal;

    private Button btnDeleteSelected;
    private Button btnResetAll;
    private Button btnExportJson;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public TeamTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);
        initComponents();
        refreshTableData();
    }

    private void initComponents() {
        VBox cardMain = DashboardLayout.createPageContainer();

        lblTotalMembersVal = new Label("0");
        lblTotalSubsVal = new Label("0");
        lblTotalFanClubVal = new Label("0");

        HBox headerRight = DashboardLayout.createHeaderActions(
                DashboardLayout.createMiniStatCard("THÀNH VIÊN", lblTotalMembersVal, "#e4e4e7"),
                DashboardLayout.createMiniStatCard("SUBSCRIBERS", lblTotalSubsVal, "#818cf8"),
                DashboardLayout.createMiniStatCard("TIM ĐỘI", lblTotalFanClubVal, "#a1a1aa")
        );

        cardMain.getChildren().add(DashboardLayout.createPageHeader(
                "QUẢN LÝ THÀNH VIÊN TIM ĐỘI / SUBSCRIBER",
                "Danh sách người xem có vai trò đặc biệt tương tác trực tiếp trong phiên live.",
                headerRight
        ));

        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = DashboardLayout.newSearchField();
        HBox searchBox = DashboardLayout.createSearchBox(
                txtSearch, "Tìm kiếm TikTok ID hoặc Tên hiển thị...");
        HBox.setHgrow(searchBox, Priority.ALWAYS);

        cbFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Lọc: Tất cả", "Lọc: Chỉ Subscriber", "Lọc: Chỉ Fan Club"));
        cbFilter.getSelectionModel().select(0);
        cbFilter.setPrefWidth(180);
        DashboardLayout.styleComboBox(cbFilter);

        filterBar.getChildren().addAll(searchBox, cbFilter);
        cardMain.getChildren().add(filterBar);

        tblMembers = DashboardLayout.createTable();

        TableColumn<TeamMember, Integer> colStt = new TableColumn<>("STT");
        colStt.setCellValueFactory(cell -> new SimpleIntegerProperty(memberList.indexOf(cell.getValue()) + 1).asObject());
        colStt.setPrefWidth(50);
        colStt.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        TableColumn<TeamMember, String> colId = new TableColumn<>("TikTok ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(120);

        TableColumn<TeamMember, String> colNick = new TableColumn<>("Tên Hiển Thị");
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(160);

        TableColumn<TeamMember, String> colRole = new TableColumn<>("Vai Trò");
        colRole.setCellValueFactory(cell -> {
            TeamMember m = cell.getValue();
            String role = "Người dùng";
            if (m.isSubscriber() && m.getTeamName() != null) {
                role = "Sub & Fan Club";
            } else if (m.isSubscriber()) {
                role = "Subscriber";
            } else if (m.getTeamName() != null) {
                role = m.getTeamName();
            }
            return new SimpleStringProperty(role);
        });
        colRole.setPrefWidth(140);
        colRole.setStyle("-fx-alignment: CENTER; -fx-text-fill: #818cf8; -fx-font-weight: bold;");

        TableColumn<TeamMember, String> colTeamLvl = new TableColumn<>("Cấp Tim");
        colTeamLvl.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getTeamLevel() > 0 ? "Cấp " + cell.getValue().getTeamLevel() : "--"
        ));
        colTeamLvl.setPrefWidth(80);
        colTeamLvl.setStyle("-fx-alignment: CENTER; -fx-text-fill: #a1a1aa; -fx-font-weight: bold;");

        TableColumn<TeamMember, String> colGiftLvl = new TableColumn<>("Cấp Xanh");
        colGiftLvl.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getGiftGiverLevel() > 0 ? "Cấp " + cell.getValue().getGiftGiverLevel() : "--"
        ));
        colGiftLvl.setPrefWidth(90);
        colGiftLvl.setStyle("-fx-alignment: CENTER; -fx-text-fill: #e4e4e7; -fx-font-weight: bold;");

        TableColumn<TeamMember, String> colLastActive = new TableColumn<>("Tương Tác Cuối");
        colLastActive.setCellValueFactory(cell -> new SimpleStringProperty(
            TIME_FORMAT.format(new Date(cell.getValue().getLastActive()))
        ));
        colLastActive.setPrefWidth(110);
        colLastActive.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        tblMembers.getColumns().addAll(colStt, colId, colNick, colRole, colTeamLvl, colGiftLvl, colLastActive);

        // Advanced Live Filtering Logic
        filteredList = new FilteredList<>(memberList, p -> true);
        tblMembers.setItems(filteredList);

        // Combined filter listener
        Runnable applyFilter = () -> filteredList.setPredicate(m -> {
            // Text Search filter
            String text = txtSearch.getText().toLowerCase().trim();
            boolean textMatches = text.isEmpty() ||
                                  m.getUniqueId().toLowerCase().contains(text) ||
                                  m.getNickname().toLowerCase().contains(text);

            if (!textMatches) return false;

            // Category filter
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

        cardMain.getChildren().add(tblMembers);

        btnDeleteSelected = DashboardLayout.newButton("Xoá Thành Viên Chọn");
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#a1a1aa"));
        btnDeleteSelected.setGraphic(trashIcon);
        DashboardLayout.applySecondaryButton(btnDeleteSelected);
        btnDeleteSelected.setOnAction(e -> deleteSelectedMember());

        btnResetAll = DashboardLayout.newButton("Xoá Hết Bảng");
        FontIcon refreshIcon = new FontIcon(Feather.REFRESH_CW);
        refreshIcon.setIconColor(Color.web("#f87171"));
        btnResetAll.setGraphic(refreshIcon);
        DashboardLayout.applyDangerButton(btnResetAll);
        btnResetAll.setOnAction(e -> resetAllMembers());

        btnExportJson = DashboardLayout.newButton("Xuất File JSON");
        FontIcon exportIcon = new FontIcon(Feather.DOWNLOAD);
        exportIcon.setIconColor(Color.web("#818cf8"));
        btnExportJson.setGraphic(exportIcon);
        DashboardLayout.applyPrimaryButton(btnExportJson);
        btnExportJson.setOnAction(e -> exportToJson());

        cardMain.getChildren().add(DashboardLayout.createActionsRow(
                btnDeleteSelected, btnResetAll, btnExportJson));

        setCenter(cardMain);
    }

    public void refreshTableData() {
        memberList.clear();
        List<TeamMember> list = DataManager.getTeamMembers();
        int subsCount = 0;
        int fanClubCount = 0;

        for (TeamMember m : list) {
            memberList.add(m);
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
        TeamMember selected = tblMembers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.warning(parent, "Cảnh báo", "Vui lòng chọn thành viên cần xoá!");
            return;
        }

        if (Dialogs.confirm(parent, "Xác nhận", "Bạn có chắc muốn xoá thành viên @" + selected.getUniqueId() + " khỏi danh sách?", "Xoá")) {
            synchronized (DataManager.class) {
                List<TeamMember> list = DataManager.getTeamMembers();
                list.removeIf(m -> m.getUniqueId().equalsIgnoreCase(selected.getUniqueId()));
                DataManager.save();
            }
            refreshTableData();
        }
    }

    private void resetAllMembers() {
        if (Dialogs.confirm(parent, "Xác nhận xoá sạch", "Hành động này sẽ xoá sạch danh sách thành viên hiện tại. Bạn có muốn tiếp tục?", "Xoá sạch")) {
            synchronized (DataManager.class) {
                DataManager.getTeamMembers().clear();
                DataManager.save();
            }
            refreshTableData();
        }
    }

    private void exportToJson() {
        ObservableList<TeamMember> items = tblMembers.getItems();
        List<TeamMember> exportList = new ArrayList<>(items);

        if (exportList.isEmpty()) {
            Dialogs.info(parent, "Thông báo", "Không có dữ liệu nào để xuất!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file JSON danh sách thành viên");
        fileChooser.setInitialFileName("danh_sach_thanh_vien.json");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON File (*.json)", "*.json"));

        File fileToSave = fileChooser.showSaveDialog(parent);
        if (fileToSave != null) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(fileToSave), StandardCharsets.UTF_8)) {
                GSON.toJson(exportList, writer);
                Dialogs.info(parent, "Thành công", "Xuất file thành công!\nTổng số thành viên: " + exportList.size() + "\nĐường dẫn: " + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                Dialogs.error(parent, "Lỗi", "Lỗi khi lưu file: " + ex.getMessage());
            }
        }
    }
}
