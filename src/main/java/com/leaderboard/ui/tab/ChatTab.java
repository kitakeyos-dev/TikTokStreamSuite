package com.leaderboard.ui.tab;

import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatTab extends BorderPane {
    private final DashboardStage parent;
    private TableView<ChatRow> tblChatLog;
    private final ObservableList<ChatRow> chatList = FXCollections.observableArrayList();
    private FilteredList<ChatRow> filteredList;

    private TextField txtSearch;
    private Button btnClearChat;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public record ChatRow(String time, String uniqueId, String nickname, String comment, String avatarUrl) {
    }

    public ChatTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);
        initComponents();
    }

    private void initComponents() {
        VBox cardChat = DashboardLayout.createPageContainer();

        cardChat.getChildren().add(DashboardLayout.createPageHeader(
                "LỊCH SỬ TRÒ CHUYỆN TRỰC TIẾP",
                "Xem tin nhắn thời gian thực truyền trực tiếp từ cổng kết nối Live stream."
        ));

        txtSearch = DashboardLayout.newSearchField();
        cardChat.getChildren().add(DashboardLayout.createSearchBox(
                txtSearch, "Tìm kiếm theo TikTok ID, Tên hiển thị hoặc Nội dung bình luận..."));

        tblChatLog = DashboardLayout.createTable();

        TableColumn<ChatRow, String> colTime = new TableColumn<>("Thời Gian");
        colTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().time()));
        colTime.setPrefWidth(90);
        colTime.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        TableColumn<ChatRow, String> colId = new TableColumn<>("TikTok ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().uniqueId()));
        colId.setPrefWidth(120);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #818cf8;");

        TableColumn<ChatRow, String> colNick = new TableColumn<>("Tên Hiển Thị");
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nickname()));
        colNick.setPrefWidth(150);

        TableColumn<ChatRow, String> colComment = new TableColumn<>("Nội Dung Bình Luận");
        colComment.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().comment()));
        colComment.setPrefWidth(430);

        tblChatLog.getColumns().addAll(colTime, colId, colNick, colComment);

        // Live Filtering Setup
        filteredList = new FilteredList<>(chatList, p -> true);
        tblChatLog.setItems(filteredList);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(row -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase().trim();
                return row.uniqueId().toLowerCase().contains(lower) ||
                       row.nickname().toLowerCase().contains(lower) ||
                       row.comment().toLowerCase().contains(lower);
            });
        });

        cardChat.getChildren().add(tblChatLog);

        btnClearChat = DashboardLayout.newButton("Xoá Lịch Sử Chat");
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#f87171"));
        btnClearChat.setGraphic(trashIcon);
        DashboardLayout.applyDangerButton(btnClearChat);
        btnClearChat.setOnAction(e -> chatList.clear());

        cardChat.getChildren().add(DashboardLayout.createActionsRow(btnClearChat));

        setCenter(cardChat);
    }

    public void addChatRow(String uniqueId, String nickname, String comment, String avatarUrl) {
        String time = LocalTime.now().format(TIME_FORMATTER);
        ChatRow row = new ChatRow(time, uniqueId, nickname, comment, avatarUrl);
        chatList.add(0, row); // Insert at top like in Swing
        if (chatList.size() > 100) {
            chatList.remove(100, chatList.size());
        }
    }
}
