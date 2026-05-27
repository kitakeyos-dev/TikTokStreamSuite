package com.leaderboard.ui.tab;

import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;
import com.leaderboard.util.I18n;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Cleaned up Live Chat Tab extending BaseDataTab.
 * Focuses strictly on chat row formatting and clearing logic.
 */
public class ChatTab extends BaseDataTab<ChatTab.ChatRow> {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public record ChatRow(String time, String uniqueId, String nickname, String comment, String avatarUrl) {
    }

    public ChatTab(DashboardStage parent) {
        super(parent);
        setupTableColumns();
        buildLayout("chat.title", "chat.subtitle", "chat.prompt.search");
    }

    @Override
    protected void setupTableColumns() {
        TableColumn<ChatRow, String> colTime = new TableColumn<>(I18n.get("chat.col.time"));
        colTime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().time()));
        colTime.setPrefWidth(90);
        colTime.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        TableColumn<ChatRow, String> colId = new TableColumn<>(I18n.get("chat.col.id"));
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().uniqueId()));
        colId.setPrefWidth(120);
        colId.setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #818cf8;");

        TableColumn<ChatRow, String> colNick = new TableColumn<>(I18n.get("chat.col.nick"));
        colNick.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().nickname()));
        colNick.setPrefWidth(150);

        TableColumn<ChatRow, String> colComment = new TableColumn<>(I18n.get("chat.col.comment"));
        colComment.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().comment()));
        colComment.setPrefWidth(430);

        tableView.getColumns().addAll(colTime, colId, colNick, colComment);
    }

    @Override
    protected boolean matchesSearch(ChatRow row, String query) {
        return row.uniqueId().toLowerCase().contains(query) ||
               row.nickname().toLowerCase().contains(query) ||
               row.comment().toLowerCase().contains(query);
    }

    @Override
    protected Pane buildFooterActions() {
        Button btnClearChat = DashboardLayout.newButton(I18n.get("chat.btn.clear"));
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#f87171"));
        btnClearChat.setGraphic(trashIcon);
        DashboardLayout.applyDangerButton(btnClearChat);
        btnClearChat.setOnAction(e -> masterList.clear());

        return DashboardLayout.createActionsRow(btnClearChat);
    }

    public void addChatRow(String uniqueId, String nickname, String comment, String avatarUrl) {
        String time = LocalTime.now().format(TIME_FORMATTER);
        ChatRow row = new ChatRow(time, uniqueId, nickname, comment, avatarUrl);
        masterList.add(0, row); // Insert at top like in Swing
        if (masterList.size() > 100) {
            masterList.remove(100, masterList.size());
        }
    }
}
