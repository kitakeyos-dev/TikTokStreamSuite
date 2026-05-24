package com.leaderboard.ui.tab;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leaderboard.model.TeamMember;
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
import javafx.stage.FileChooser;
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
        setPadding(new Insets(15, 5, 15, 5));
        setStyle("-fx-background-color: transparent;");
        initComponents();
        refreshTableData();
    }

    private void initComponents() {
        VBox cardMain = new VBox(15);
        cardMain.setPadding(new Insets(15, 20, 15, 20));
        cardMain.setStyle(
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

        // Left Area: Title
        VBox titleArea = new VBox(2);
        HBox.setHgrow(titleArea, Priority.ALWAYS);

        Label lblTitle = new Label("QUẢN LÝ THÀNH VIÊN TIM ĐỘI / SUBSCRIBER");
        lblTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label lblSubtitle = new Label("Danh sách người xem có vai trò đặc biệt tương tác trực tiếp trong phiên live.");
        lblSubtitle.setStyle("-fx-text-fill: #71717a; -fx-font-size: 10px;");

        titleArea.getChildren().addAll(lblTitle, lblSubtitle);
        cardHeader.getChildren().add(titleArea);

        // Right Area: Stats
        HBox headerRight = new HBox(15);
        headerRight.setAlignment(Pos.CENTER_RIGHT);

        lblTotalMembersVal = new Label("0");
        VBox pnlStatMembers = createMiniStatCard("THÀNH VIÊN", lblTotalMembersVal, "#ffffff");

        lblTotalSubsVal = new Label("0");
        VBox pnlStatSubs = createMiniStatCard("SUBSCRIBERS", lblTotalSubsVal, "#fe2c55");

        lblTotalFanClubVal = new Label("0");
        VBox pnlStatFanClub = createMiniStatCard("TIM ĐỘI", lblTotalFanClubVal, "#25f4ee");

        headerRight.getChildren().addAll(pnlStatMembers, pnlStatSubs, pnlStatFanClub);
        cardHeader.getChildren().add(headerRight);

        cardMain.getChildren().add(cardHeader);

        // --- FILTER ACTION BAR ---
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm TikTok ID hoặc Tên hiển thị...");
        txtSearch.setPrefHeight(36);
        txtSearch.setStyle("-fx-background-radius: 8px;");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        cbFilter = new ComboBox<>(FXCollections.observableArrayList("Lọc: Tất cả", "Lọc: Chỉ Subscriber", "Lọc: Chỉ Fan Club"));
        cbFilter.getSelectionModel().select(0);
        cbFilter.setPrefHeight(36);
        cbFilter.setPrefWidth(180);
        cbFilter.setStyle("-fx-background-radius: 8px;");

        filterBar.getChildren().addAll(txtSearch, cbFilter);
        cardMain.getChildren().add(filterBar);

        // --- TABLE CONTAINER ---
        tblMembers = new TableView<>();
        tblMembers.setPrefHeight(380);
        tblMembers.setStyle(
            "-fx-background-color: #1e1e1e;" +
            "-fx-control-inner-background: #1e1e1e;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );

        TableColumn<TeamMember, Integer> colStt = new TableColumn<>("STT");
        colStt.setCellValueFactory(cell -> new SimpleIntegerProperty(memberList.indexOf(cell.getValue()) + 1).asObject());
        colStt.setPrefWidth(50);
        colStt.setStyle("-fx-alignment: CENTER; -fx-text-fill: #a1a1a8;");

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
        colRole.setStyle("-fx-alignment: CENTER; -fx-text-fill: #fe2c55; -fx-font-weight: bold;");

        TableColumn<TeamMember, String> colTeamLvl = new TableColumn<>("Cấp Tim");
        colTeamLvl.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getTeamLevel() > 0 ? "Cấp " + cell.getValue().getTeamLevel() : "--"
        ));
        colTeamLvl.setPrefWidth(80);
        colTeamLvl.setStyle("-fx-alignment: CENTER; -fx-text-fill: #fb923c; -fx-font-weight: bold;");

        TableColumn<TeamMember, String> colGiftLvl = new TableColumn<>("Cấp Xanh");
        colGiftLvl.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getGiftGiverLevel() > 0 ? "Cấp " + cell.getValue().getGiftGiverLevel() : "--"
        ));
        colGiftLvl.setPrefWidth(80);
        colGiftLvl.setStyle("-fx-alignment: CENTER; -fx-text-fill: #25f4ee; -fx-font-weight: bold;");

        TableColumn<TeamMember, String> colLastActive = new TableColumn<>("Tương Tác Cuối");
        colLastActive.setCellValueFactory(cell -> new SimpleStringProperty(
            TIME_FORMAT.format(new Date(cell.getValue().getLastActive()))
        ));
        colLastActive.setPrefWidth(110);
        colLastActive.setStyle("-fx-alignment: CENTER; -fx-text-fill: #a1a1a8;");

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

        // --- BOTTOM ACTIONS TOOLBAR ---
        HBox actionsRow = new HBox(15);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(10, 0, 0, 0));

        btnDeleteSelected = new Button("Xoá Thành Viên Chọn");
        btnDeleteSelected.setPrefHeight(32);
        btnDeleteSelected.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.05);" +
            "-fx-text-fill: #e9e2e1;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;"
        );
        btnDeleteSelected.setOnAction(e -> deleteSelectedMember());

        btnResetAll = new Button("Xoá Hết Bảng");
        btnResetAll.setPrefHeight(32);
        btnResetAll.setStyle(
            "-fx-background-color: #fe2c55;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;"
        );
        btnResetAll.setOnAction(e -> resetAllMembers());

        btnExportJson = new Button("Xuất File JSON");
        btnExportJson.setPrefHeight(32);
        btnExportJson.setStyle(
            "-fx-background-color: #25f4ee;" +
            "-fx-text-fill: #131313;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;"
        );
        btnExportJson.setOnAction(e -> exportToJson());

        actionsRow.getChildren().addAll(btnDeleteSelected, btnResetAll, btnExportJson);
        cardMain.getChildren().add(actionsRow);

        setCenter(cardMain);
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
            Alert alert = new Alert(Alert.AlertType.WARNING, "Vui lòng chọn thành viên cần xoá!", ButtonType.OK);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Bạn có chắc muốn xoá thành viên @" + selected.getUniqueId() + " khỏi danh sách?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            synchronized (DataManager.class) {
                List<TeamMember> list = DataManager.getTeamMembers();
                list.removeIf(m -> m.getUniqueId().equalsIgnoreCase(selected.getUniqueId()));
                DataManager.save();
            }
            refreshTableData();
        }
    }

    private void resetAllMembers() {
        Alert confirm = new Alert(Alert.AlertType.WARNING,
            "CẢNH BÁO: Hành động này sẽ xoá SẠCH danh sách thành viên hiện tại! Bạn có muốn tiếp tục?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xoá sạch");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Không có dữ liệu nào để xuất!", ButtonType.OK);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.showAndWait();
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
                Alert success = new Alert(Alert.AlertType.INFORMATION, 
                    "Xuất file thành công!\nTổng số thành viên: " + exportList.size() + "\nĐường dẫn: " + fileToSave.getAbsolutePath(), 
                    ButtonType.OK);
                success.setTitle("Thành công");
                success.setHeaderText(null);
                success.showAndWait();
            } catch (Exception ex) {
                Alert error = new Alert(Alert.AlertType.ERROR, "Lỗi khi lưu file: " + ex.getMessage(), ButtonType.OK);
                error.setTitle("Lỗi");
                error.setHeaderText(null);
                error.showAndWait();
            }
        }
    }
}
