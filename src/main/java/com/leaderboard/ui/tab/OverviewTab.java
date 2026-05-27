package com.leaderboard.ui.tab;

import com.leaderboard.service.UpdateService;
import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.I18n;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.feather.Feather;

/**
 * Main overview dashboard tab. Focuses on TikTok connection configuration
 * and live system diagnostics.
 */
public class OverviewTab extends BorderPane {
    private final DashboardStage parent;

    private final TextField txtUsername;
    private final PasswordField txtApiKey;
    private final Label lblStatusBadge;
    private final Button btnConnect;

    private final Label lblWebSocketDiag;
    private final Label lblLatencyDiag;
    private final Label lblSyncDiag;

    public OverviewTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);

        GridPane grid = DashboardLayout.createTwoColumnGrid();

        // Left Column: Connection Configuration Card
        VBox cardConfig = DashboardLayout.createCard(I18n.get("overview.card.config"));
        VBox leftContent = DashboardLayout.createSectionContent();

        // TikTok Username
        Label lblUser = DashboardLayout.createFieldLabel(I18n.get("overview.label.username"));
        txtUsername = DashboardLayout.newTextField();
        txtUsername.setText(ConfigManager.getConfig().getStreamerUsername());
        Label lblAt = new Label("@");
        lblAt.setStyle("-fx-text-fill: #71717a; -fx-font-size: 14px; -fx-font-weight: bold;");
        HBox userFieldBox = DashboardLayout.wrapTextField(txtUsername, I18n.get("overview.prompt.username"), lblAt);

        // API Key
        Label lblKey = DashboardLayout.createFieldLabel(I18n.get("overview.label.apikey"));
        txtApiKey = DashboardLayout.newPasswordField();
        txtApiKey.setText(ConfigManager.getConfig().getEulerstreamKey());
        FontIcon keyIcon = new FontIcon(Feather.KEY);
        keyIcon.setIconColor(Color.web("#71717a"));
        HBox keyFieldBox = DashboardLayout.wrapPasswordField(txtApiKey, I18n.get("overview.prompt.apikey"), keyIcon);

        // Language settings
        Label lblLang = DashboardLayout.createFieldLabel(I18n.get("overview.language.label"));
        ComboBox<String> cbLanguage = new ComboBox<>();
        cbLanguage.getItems().addAll("Tiếng Việt", "English");
        cbLanguage.setValue(ConfigManager.getConfig().getLanguage().equals("vi") ? "Tiếng Việt" : "English");
        DashboardLayout.styleComboBox(cbLanguage);
        cbLanguage.setMaxWidth(Double.MAX_VALUE);
        cbLanguage.setOnAction(e -> {
            String selected = cbLanguage.getValue();
            String langCode = selected.equals("Tiếng Việt") ? "vi" : "en";
            if (!langCode.equals(ConfigManager.getConfig().getLanguage())) {
                ConfigManager.getConfig().setLanguage(langCode);
                ConfigManager.save();
                I18n.loadBundle();
                
                String title = I18n.get("overview.language.restart.title");
                String msg = I18n.get("overview.language.restart.msg");
                Dialogs.info(parent.getScene().getWindow(), title, msg);
            }
        });

        // Connection Action Row
        lblStatusBadge = DashboardLayout.createStatusBadge(I18n.get("overview.status.offline"));
        btnConnect = DashboardLayout.newButton(I18n.get("overview.btn.connect"));
        DashboardLayout.applyPrimaryButton(btnConnect);
        btnConnect.setOnAction(e -> parent.toggleConnection());
        HBox statusRow = DashboardLayout.createStatusRow(lblStatusBadge, btnConnect);

        leftContent.getChildren().addAll(lblUser, userFieldBox, lblKey, keyFieldBox, lblLang, cbLanguage, statusRow);
        cardConfig.getChildren().add(leftContent);
        grid.add(cardConfig, 0, 0);
        DashboardLayout.fillGridCell(cardConfig);

        // Right Column: System Diagnostics Card & Auto Updater
        VBox cardDiag = DashboardLayout.createCard(I18n.get("overview.label.diag"));
        VBox rightContent = DashboardLayout.createSectionContent();

        VBox diagBox = new VBox(14);
        diagBox.setPadding(new Insets(10, 10, 15, 10));

        lblWebSocketDiag = createDiagRow(diagBox, "WebSocket Connection", I18n.get("overview.status.offline"), "#71717a");
        lblLatencyDiag = createDiagRow(diagBox, I18n.get("overview.diag.latency"), "--", "#f4f4f5");
        lblSyncDiag = createDiagRow(diagBox, I18n.get("overview.diag.sync"), I18n.get("overview.diag.sync.inactive"), "#71717a");
        createDiagRow(diagBox, I18n.get("overview.diag.version"), "v" + UpdateService.CURRENT_VERSION, "#818cf8");

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.08; -fx-padding: 5 0 5 0;");

        // Action Check Update
        Button btnCheckUpdate = DashboardLayout.newButton(I18n.get("overview.btn.checkupdate"));
        FontIcon refreshIcon = new FontIcon(Feather.REFRESH_CW);
        refreshIcon.setIconColor(Color.web("#818cf8"));
        btnCheckUpdate.setGraphic(refreshIcon);
        DashboardLayout.applySecondaryButton(btnCheckUpdate);
        btnCheckUpdate.setMaxWidth(Double.MAX_VALUE);
        btnCheckUpdate.setOnAction(e -> UpdateService.checkForUpdates(parent.getScene().getWindow(), false));

        rightContent.getChildren().addAll(diagBox, sep, btnCheckUpdate);
        cardDiag.getChildren().add(rightContent);
        grid.add(cardDiag, 1, 0);
        DashboardLayout.fillGridCell(cardDiag);

        setCenter(grid);
        updateDiagnostics(false, "--");
    }

    private Label createDiagRow(VBox parentContainer, String label, String value, String valueColorHex) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px;");
        HBox.setHgrow(lblLabel, Priority.ALWAYS);
        lblLabel.setMaxWidth(Double.MAX_VALUE);

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-text-fill: " + valueColorHex + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        row.getChildren().addAll(lblLabel, lblValue);
        parentContainer.getChildren().add(row);

        return lblValue;
    }

    public void updateDiagnostics(boolean isConnected, String latency) {
        if (isConnected) {
            lblWebSocketDiag.setText(I18n.get("overview.diag.ws.online"));
            lblWebSocketDiag.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblLatencyDiag.setText(latency);
            lblLatencyDiag.setStyle("-fx-text-fill: #f4f4f5; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblSyncDiag.setText(I18n.get("overview.diag.sync.active"));
            lblSyncDiag.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            lblWebSocketDiag.setText(I18n.get("overview.diag.ws.offline"));
            lblWebSocketDiag.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblLatencyDiag.setText("--");
            lblLatencyDiag.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblSyncDiag.setText(I18n.get("overview.diag.sync.inactive"));
            lblSyncDiag.setStyle("-fx-text-fill: #71717a; -fx-font-size: 11px; -fx-font-weight: bold;");
        }
    }

    public void setConnectionState(boolean isConnected) {
        if (isConnected) {
            lblStatusBadge.setText(I18n.get("overview.status.connected"));
            lblStatusBadge.setStyle(
                "-fx-background-color: rgba(99, 102, 241, 0.08);" +
                "-fx-background-radius: 8px;" +
                "-fx-text-fill: #818cf8;" +
                "-fx-border-color: rgba(99, 102, 241, 0.4);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;"
            );
            btnConnect.setText(I18n.get("overview.btn.disconnect"));
            DashboardLayout.applyDangerButton(btnConnect);
            btnConnect.setDisable(false);
            txtUsername.setDisable(true);
            txtApiKey.setDisable(true);
        } else {
            lblStatusBadge.setText(I18n.get("overview.status.offline"));
            lblStatusBadge.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.03);" +
                "-fx-background-radius: 8px;" +
                "-fx-text-fill: #a1a1aa;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;"
            );
            btnConnect.setText(I18n.get("overview.btn.connect"));
            DashboardLayout.applyPrimaryButton(btnConnect);
            btnConnect.setDisable(false);
            txtUsername.setDisable(false);
            txtApiKey.setDisable(false);
        }
    }

    public void setConnectingState() {
        lblStatusBadge.setText(I18n.get("overview.status.connecting"));
        lblStatusBadge.setStyle(
            "-fx-background-color: rgba(168, 85, 247, 0.08);" +
            "-fx-background-radius: 8px;" +
            "-fx-text-fill: #c084fc;" +
            "-fx-border-color: rgba(168, 85, 247, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11px;"
        );
        btnConnect.setText(I18n.get("overview.btn.connecting"));
        DashboardLayout.applyButtonStyle(btnConnect,
                "-fx-background-color: rgba(168, 85, 247, 0.04);" +
                        "-fx-text-fill: #c084fc;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(168, 85, 247, 0.2);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 1px;");
        btnConnect.setDisable(true);
        txtUsername.setDisable(true);
        txtApiKey.setDisable(true);
    }

    public void setDisconnectingState() {
        lblStatusBadge.setText(I18n.get("overview.status.disconnecting"));
        lblStatusBadge.setStyle(
            "-fx-background-color: rgba(251, 146, 60, 0.08);" +
            "-fx-background-radius: 8px;" +
            "-fx-text-fill: #fdba74;" +
            "-fx-border-color: rgba(251, 146, 60, 0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 11px;"
        );
        btnConnect.setText(I18n.get("overview.btn.disconnecting"));
        DashboardLayout.applyButtonStyle(btnConnect,
                "-fx-background-color: rgba(251, 146, 60, 0.04);" +
                        "-fx-text-fill: #fdba74;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(251, 146, 60, 0.2);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 1px;");
        btnConnect.setDisable(true);
        txtUsername.setDisable(true);
        txtApiKey.setDisable(true);
    }

    public String getUsername() { return txtUsername.getText().trim(); }
    public String getApiKey() { return txtApiKey.getText().trim(); }
    public Label getLblSyncDiag() { return lblSyncDiag; }
}
