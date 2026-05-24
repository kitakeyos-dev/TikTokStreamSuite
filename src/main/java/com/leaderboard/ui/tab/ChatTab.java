package com.leaderboard.ui.tab;

import com.leaderboard.ui.DashboardStage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatTab extends BorderPane {
    private final DashboardStage parent;
    private TableView<ChatRow> tblChatLog;
    private ObservableList<ChatRow> chatList = FXCollections.observableArrayList();
    private FilteredList<ChatRow> filteredList;

    private TextField txtSearch;
    private Button btnClearChat;
    private Button btnToggleChatOverlayTab3;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static class ChatRow {
        private final String time;
        private final String uniqueId;
        private final String nickname;
        private final String comment;
        private final String avatarUrl;

        public ChatRow(String time, String uniqueId, String nickname, String comment, String avatarUrl) {
            this.time = time;
            this.uniqueId = uniqueId;
            this.nickname = nickname;
            this.comment = comment;
            this.avatarUrl = avatarUrl;
        }

        public String getTime() { return time; }
        public String getUniqueId() { return uniqueId; }
        public String getNickname() { return nickname; }
        public String getComment() { return comment; }
        public String getAvatarUrl() { return avatarUrl; }
    }

    public ChatTab(DashboardStage parent) {
        this.parent = parent;
        setPadding(new Insets(15, 5, 15, 5));
        setStyle("-fx-background-color: transparent;");
        initComponents();
    }

    private void initComponents() {
        VBox cardChat = new VBox(15);
        cardChat.setPadding(new Insets(15, 20, 15, 20));
        cardChat.setStyle(
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

        VBox titleArea = new VBox(2);
        HBox.setHgrow(titleArea, Priority.ALWAYS);

        Label lblTitle = new Label("LỊCH SỬ TRÒ CHUYỆN TRỰC TIẾP");
        lblTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label lblSubtitle = new Label("Xem tin nhắn thời gian thực truyền trực tiếp từ cổng kết nối Live stream.");
        lblSubtitle.setStyle("-fx-text-fill: #71717a; -fx-font-size: 10px;");

        titleArea.getChildren().addAll(lblTitle, lblSubtitle);
        cardHeader.getChildren().add(titleArea);

        btnToggleChatOverlayTab3 = new Button("BẬT KHUNG CHAT");
        btnToggleChatOverlayTab3.setPrefHeight(32);
        btnToggleChatOverlayTab3.setStyle(
            "-fx-background-color: rgba(37, 244, 238, 0.1);" +
            "-fx-text-fill: #25f4ee;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(37, 244, 238, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );
        btnToggleChatOverlayTab3.setOnAction(e -> parent.toggleChatOverlayWindow());
        cardHeader.getChildren().add(btnToggleChatOverlayTab3);

        cardChat.getChildren().add(cardHeader);

        // --- ACTION SEARCH BAR ---
        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm kiếm theo TikTok ID, Tên hiển thị hoặc Nội dung bình luận...");
        txtSearch.setPrefHeight(36);
        txtSearch.setStyle("-fx-background-radius: 8px;");
        cardChat.getChildren().add(txtSearch);

        // --- CHAT TABLE CONTAINER ---
        tblChatLog = new TableView<>();
        tblChatLog.setPrefHeight(380);
        tblChatLog.setStyle(
            "-fx-background-color: #1e1e1e;" +
            "-fx-control-inner-background: #1e1e1e;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );

        TableColumn<ChatRow, String> colTime = new TableColumn<>("Thời Gian");
        colTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTime()));
        colTime.setPrefWidth(90);
        colTime.setStyle("-fx-alignment: CENTER; -fx-text-fill: #8f8f98;");

        TableColumn<ChatRow, String> colId = new TableColumn<>("TikTok ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUniqueId()));
        colId.setPrefWidth(120);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #a1a1a8;");

        TableColumn<ChatRow, String> colNick = new TableColumn<>("Tên Hiển Thị");
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNickname()));
        colNick.setPrefWidth(150);

        TableColumn<ChatRow, String> colComment = new TableColumn<>("Nội Dung Bình Luận");
        colComment.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getComment()));
        colComment.setPrefWidth(430);

        tblChatLog.getColumns().addAll(colTime, colId, colNick, colComment);

        // Live Filtering Setup
        filteredList = new FilteredList<>(chatList, p -> true);
        tblChatLog.setItems(filteredList);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(row -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase().trim();
                return row.getUniqueId().toLowerCase().contains(lower) || 
                       row.getNickname().toLowerCase().contains(lower) || 
                       row.getComment().toLowerCase().contains(lower);
            });
        });

        cardChat.getChildren().add(tblChatLog);

        // --- ACTIONS TOOLBAR ---
        HBox actionsRow = new HBox(15);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        actionsRow.setPadding(new Insets(10, 0, 0, 0));

        btnClearChat = new Button("Xoá Lịch Sử Chat");
        btnClearChat.setPrefHeight(32);
        btnClearChat.setStyle(
            "-fx-background-color: #fe2c55;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;"
        );
        btnClearChat.setOnAction(e -> chatList.clear());
        actionsRow.getChildren().add(btnClearChat);

        cardChat.getChildren().add(actionsRow);

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

    public void updateOverlayButtonState(boolean isOpen) {
        if (isOpen) {
            btnToggleChatOverlayTab3.setText("TẮT KHUNG CHAT");
            btnToggleChatOverlayTab3.setStyle(
                "-fx-background-color: #fe2c55;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8px;"
            );
        } else {
            btnToggleChatOverlayTab3.setText("BẬT KHUNG CHAT");
            btnToggleChatOverlayTab3.setStyle(
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
