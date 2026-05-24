package com.leaderboard.ui.tab;

import com.leaderboard.model.BankDeposit;
import com.leaderboard.model.BankTransaction;
import com.leaderboard.service.AcbClient;
import com.leaderboard.util.AcbTransactionParser;
import com.leaderboard.util.BankConfigManager;
import com.leaderboard.util.BankDataManager;
import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BankTab extends BorderPane {
    private final DashboardStage parent;

    private TextField txtUsername;
    private PasswordField txtPassword;
    private TextField txtAccount;
    private TextField txtApiUrl;
    private PasswordField txtApiKey;
    private Spinner<Integer> spinnerInterval;
    private Button btnStartStop;
    private Label lblStatusBadge;
    private Label lblBalance;

    private TableView<BankTransaction> tblTransactions;
    private TableView<BankDeposit> tblDeposits;
    private TextArea txtLog;
    private Tab tabDep;

    private final ObservableList<BankTransaction> transactionList = FXCollections.observableArrayList();
    private final ObservableList<BankDeposit> depositList = FXCollections.observableArrayList();

    private final AcbClient client = new AcbClient();
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,##0");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ScheduledExecutorService scheduler;
    private boolean running = false;

    public BankTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);

        initComponents();

        // Connect log callback
        BankDataManager.setLogCallback(this::appendLog);

        // Load saved config
        loadConfig();
    }

    private void initComponents() {
        GridPane grid = DashboardLayout.createTwoColumnGrid();

        VBox cardConfig = DashboardLayout.createCard("CẤU HÌNH ACB BANKPUSHER");
        VBox leftContent = DashboardLayout.createSectionContent();

        lblStatusBadge = DashboardLayout.createStatusBadge("ĐÃ DỪNG");
        btnStartStop = DashboardLayout.newButton("Bắt đầu");
        DashboardLayout.applySuccessButton(btnStartStop);
        btnStartStop.setOnAction(e -> toggleRunning());
        HBox statusRow = DashboardLayout.createStatusRow(lblStatusBadge, btnStartStop);

        VBox form = new VBox(DashboardLayout.FORM_GAP);
        form.setMaxWidth(Double.MAX_VALUE);

        txtUsername = DashboardLayout.newTextField();
        txtPassword = DashboardLayout.newPasswordField();
        txtAccount = DashboardLayout.newTextField();
        txtApiUrl = DashboardLayout.newTextField();
        txtApiKey = DashboardLayout.newPasswordField();

        FontIcon iconUser = new FontIcon(Feather.USER);
        iconUser.setIconColor(Color.web("#71717a"));
        FontIcon iconPass = new FontIcon(Feather.LOCK);
        iconPass.setIconColor(Color.web("#71717a"));
        FontIcon iconAcct = new FontIcon(Feather.CREDIT_CARD);
        iconAcct.setIconColor(Color.web("#71717a"));
        FontIcon iconUrl = new FontIcon(Feather.LINK);
        iconUrl.setIconColor(Color.web("#71717a"));
        FontIcon iconKey = new FontIcon(Feather.KEY);
        iconKey.setIconColor(Color.web("#71717a"));

        form.getChildren().addAll(
                DashboardLayout.createFieldLabel("TÊN ĐĂNG NHẬP BANK:"),
                DashboardLayout.wrapTextField(txtUsername, "Nhập tên đăng nhập ACB...", iconUser),
                DashboardLayout.createFieldLabel("MẬT KHẨU BANK:"),
                DashboardLayout.wrapPasswordField(txtPassword, "Nhập mật khẩu...", iconPass),
                DashboardLayout.createFieldLabel("SỐ TÀI KHOẢN:"),
                DashboardLayout.wrapTextField(txtAccount, "Nhập số tài khoản...", iconAcct),
                DashboardLayout.createFieldLabel("API URL:"),
                DashboardLayout.wrapTextField(txtApiUrl, "https://donate.nhimnhor.com", iconUrl),
                DashboardLayout.createFieldLabel("API KEY:"),
                DashboardLayout.wrapPasswordField(txtApiKey, "Nhập API Key...", iconKey),
                DashboardLayout.createFieldLabel("CHU KỲ CẬP NHẬT:")
        );

        HBox intervalBox = new HBox(8);
        intervalBox.setAlignment(Pos.CENTER_LEFT);
        spinnerInterval = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 300, 30));
        spinnerInterval.setPrefWidth(80);
        spinnerInterval.setPrefHeight(DashboardLayout.FIELD_HEIGHT);
        spinnerInterval.getStyleClass().add("tss-spinner");
        spinnerInterval.setStyle(
                "-fx-background-color: #18181b;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-text-fill: #f4f4f5;");
        spinnerInterval.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (running)
                startScheduler();
        });
        Label lblMin = new Label("giây");
        lblMin.setStyle("-fx-text-fill: #71717a; -fx-font-size: 12px;");
        intervalBox.getChildren().addAll(spinnerInterval, lblMin);
        form.getChildren().add(intervalBox);

        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.08; -fx-padding: 8 0 8 0;");

        Label lblBalanceTitle = DashboardLayout.createFieldLabel("SỐ DƯ TÀI KHOẢN:");

        lblBalance = new Label("-- VND");
        lblBalance.setStyle(
                "-fx-font-size: 26px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #25f4ee;" // Neon cyan/teal matching design aesthetics
        );

        leftContent.getChildren().addAll(statusRow, form, sep, lblBalanceTitle, lblBalance);
        cardConfig.getChildren().add(leftContent);
        grid.add(cardConfig, 0, 0);
        DashboardLayout.fillGridCell(cardConfig);

        // Column 2: Data Tabs & Logs
        VBox cardDisplay = DashboardLayout.createCard("DÒNG SỰ KIỆN GIAO DỊCH");

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setMinHeight(DashboardLayout.TABLE_PREF_HEIGHT);
        tabs.setPrefHeight(DashboardLayout.TABLE_PREF_HEIGHT);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        tabs.setStyle("-fx-background-color: transparent;");

        tblTransactions = DashboardLayout.createTable();

        TableColumn<BankTransaction, String> colType = new TableColumn<>("Loại");
        colType.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getType() == BankTransaction.Type.IN ? "▲ Vào" : "▼ Ra"));
        colType.setPrefWidth(60);
        colType.setStyle("-fx-alignment: CENTER;");

        TableColumn<BankTransaction, String> colAmount = new TableColumn<>("Số tiền");
        colAmount.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue().getType() == BankTransaction.Type.IN ? "+" : "-")
                        + MONEY_FMT.format((long) c.getValue().getAmount())));
        colAmount.setPrefWidth(110);
        colAmount.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        TableColumn<BankTransaction, String> colMessage = new TableColumn<>("Nội dung");
        colMessage.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMessage()));
        colMessage.setPrefWidth(200);

        TableColumn<BankTransaction, String> colTime = new TableColumn<>("Thời gian");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTime()));
        colTime.setPrefWidth(120);
        colTime.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        TableColumn<BankTransaction, String> colBal = new TableColumn<>("Số dư");
        colBal.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getBalance() != null ? MONEY_FMT.format(c.getValue().getBalance().longValue()) : ""));
        colBal.setPrefWidth(100);
        colBal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #25f4ee;");

        tblTransactions.getColumns().addAll(colType, colAmount, colMessage, colTime, colBal);
        tblTransactions.setItems(transactionList);

        // Customize Type and Amount colors in CellFactory
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Vào")) {
                        setTextFill(Color.web("#4ade80"));
                        setStyle("-fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setTextFill(Color.web("#f87171"));
                        setStyle("-fx-font-weight: bold; -fx-alignment: CENTER;");
                    }
                }
            }
        });
        colAmount.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.startsWith("+")) {
                        setTextFill(Color.web("#4ade80"));
                        setStyle("-fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                    } else {
                        setTextFill(Color.web("#f87171"));
                        setStyle("-fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
                    }
                }
            }
        });

        Tab tabTx = new Tab("Giao dịch", tblTransactions);

        // Tab 2: Pushed History (DepositRecord)
        tblDeposits = DashboardLayout.createTable();

        TableColumn<BankDeposit, String> colDepAmount = new TableColumn<>("Số tiền");
        colDepAmount.setCellValueFactory(
                c -> new SimpleStringProperty("+" + MONEY_FMT.format((long) c.getValue().getAmount())));
        colDepAmount.setPrefWidth(110);
        colDepAmount.setStyle("-fx-alignment: CENTER-RIGHT; -fx-text-fill: #4ade80; -fx-font-weight: bold;");

        TableColumn<BankDeposit, String> colDepMsg = new TableColumn<>("Nội dung CK");
        colDepMsg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMessage()));
        colDepMsg.setPrefWidth(220);

        TableColumn<BankDeposit, String> colDepTime = new TableColumn<>("Thời gian GD");
        colDepTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionTime()));
        colDepTime.setPrefWidth(120);
        colDepTime.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        TableColumn<BankDeposit, String> colDepPushed = new TableColumn<>("Đẩy lúc");
        colDepPushed.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRecordedAt()));
        colDepPushed.setPrefWidth(120);
        colDepPushed.setStyle("-fx-alignment: CENTER; -fx-text-fill: #71717a;");

        tblDeposits.getColumns().addAll(colDepAmount, colDepMsg, colDepTime, colDepPushed);
        tblDeposits.setItems(depositList);
        depositList.addAll(BankDataManager.getDeposits());

        tabDep = new Tab("Lịch sử đẩy (" + depositList.size() + ")", tblDeposits);

        // Tab 3: System Log Terminal
        txtLog = new TextArea();
        txtLog.setEditable(false);
        txtLog.setStyle(
                "-fx-control-inner-background: #1e1e1e;" +
                        "-fx-text-fill: #c8c8c8;" +
                        "-fx-font-family: 'Consolas', monospace;" +
                        "-fx-font-size: 11px;" +
                        "-fx-border-color: rgba(255,255,255,0.05);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;");
        Tab tabLog = new Tab("Logs hệ thống", txtLog);

        tabs.getTabs().addAll(tabTx, tabDep, tabLog);

        // Clear local logs button
        Button btnClearLog = DashboardLayout.newButton("Xoá Lịch Sử");
        FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
        trashIcon.setIconColor(Color.web("#f87171"));
        btnClearLog.setGraphic(trashIcon);
        DashboardLayout.applyDangerButton(btnClearLog);
        btnClearLog.setOnAction(e -> {
            txtLog.clear();
            appendLog("Lịch sử log đã được làm sạch.");
        });

        cardDisplay.getChildren().addAll(tabs, DashboardLayout.createActionsRow(btnClearLog));
        grid.add(cardDisplay, 1, 0);
        DashboardLayout.fillGridCell(cardDisplay);

        setCenter(grid);
    }

    private void appendLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String line = "[" + timestamp + "] " + message + "\n";
        Platform.runLater(() -> {
            if (txtLog != null) {
                txtLog.appendText(line);
            }
        });
    }

    private void loadConfig() {
        BankConfigManager.BankSettings cfg = BankConfigManager.getSettings();
        txtUsername.setText(cfg.getUsername());
        txtPassword.setText(cfg.getPassword());
        txtAccount.setText(cfg.getAccountNumber());
        int sec = cfg.getPollIntervalSeconds();
        if (sec < 5)
            sec = 30;
        spinnerInterval.getValueFactory().setValue(sec);
        txtApiUrl.setText(cfg.getApiUrl());
        txtApiKey.setText(cfg.getApiKey());
    }

    private void saveConfig() {
        BankConfigManager.BankSettings cfg = BankConfigManager.getSettings();
        cfg.setUsername(txtUsername.getText().trim());
        cfg.setPassword(txtPassword.getText().trim());
        cfg.setAccountNumber(txtAccount.getText().trim());
        cfg.setPollIntervalSeconds(spinnerInterval.getValue());
        cfg.setApiUrl(txtApiUrl.getText().trim());
        cfg.setApiKey(txtApiKey.getText().trim());
        BankConfigManager.save();
    }

    private void toggleRunning() {
        if (running) {
            stop();
        } else {
            start();
        }
    }

    private void start() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String account = txtAccount.getText().trim();

        if (username.isEmpty() || password.isEmpty() || account.isEmpty()) {
            Dialogs.warning(parent, "Thiếu thông tin", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        saveConfig();
        BankDataManager.setApiConfig(txtApiUrl.getText().trim(), txtApiKey.getText().trim());
        setFormEnabled(false);
        lblStatusBadge.setText("ĐANG ĐĂNG NHẬP...");
        lblStatusBadge.setStyle(
                "-fx-background-color: rgba(168, 85, 247, 0.08);" +
                        "-fx-background-radius: 8px;" +
                        "-fx-text-fill: #c084fc;" +
                        "-fx-border-color: rgba(168, 85, 247, 0.4);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;");
        lblBalance.setText("-- VND");
        transactionList.clear();
        client.setCredentials(username, password, account);

        new Thread(() -> {
            boolean success = false;
            String errorMsg = null;
            try {
                success = client.login();
                if (!success) {
                    errorMsg = "Đăng nhập thất bại. Kiểm tra lại thông tin.";
                }
            } catch (Exception e) {
                errorMsg = "Lỗi kết nối: " + e.getMessage();
            }

            final boolean finalSuccess = success;
            final String finalError = errorMsg;

            Platform.runLater(() -> {
                if (!finalSuccess) {
                    lblStatusBadge.setText("LỖI KẾT NỐI");
                    lblStatusBadge.setStyle(
                            "-fx-background-color: rgba(239, 68, 68, 0.08);" +
                                    "-fx-background-radius: 8px;" +
                                    "-fx-text-fill: #f87171;" +
                                    "-fx-border-color: rgba(239, 68, 68, 0.4);" +
                                    "-fx-border-radius: 8px;" +
                                    "-fx-border-width: 1px;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-font-size: 11px;");
                    appendLog("[LOGIN] " + finalError);
                    setFormEnabled(true);
                    return;
                }

                running = true;
                btnStartStop.setText("Dừng lại");
                DashboardLayout.applyDangerButton(btnStartStop);
                btnStartStop.setDisable(false);
                lblStatusBadge.setText("ĐANG CHẠY");
                lblStatusBadge.setStyle(
                        "-fx-background-color: rgba(34, 197, 94, 0.08);" +
                                "-fx-background-radius: 8px;" +
                                "-fx-text-fill: #4ade80;" +
                                "-fx-border-color: rgba(34, 197, 94, 0.4);" +
                                "-fx-border-radius: 8px;" +
                                "-fx-border-width: 1px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 11px;");
                appendLog("[LOGIN] Đăng nhập thành công, bắt đầu poll...");

                startScheduler();
            });
        }).start();
    }

    private void stop() {
        stopScheduler();
        running = false;
        btnStartStop.setText("Bắt đầu");
        DashboardLayout.applySuccessButton(btnStartStop);
        setFormEnabled(true);
        lblStatusBadge.setText("ĐÃ DỪNG");
        lblStatusBadge.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.03);" +
                        "-fx-background-radius: 8px;" +
                        "-fx-text-fill: #a1a1aa;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;");
        appendLog("[SERVICE] Đã dừng dịch vụ BankPusher.");
    }

    private void setFormEnabled(boolean enabled) {
        txtUsername.setDisable(!enabled);
        txtPassword.setDisable(!enabled);
        txtAccount.setDisable(!enabled);
        txtApiUrl.setDisable(!enabled);
        txtApiKey.setDisable(!enabled);
        spinnerInterval.setDisable(!enabled);
    }

    private void startScheduler() {
        stopScheduler();
        int delaySeconds = spinnerInterval.getValue();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Run immediately, then with fixed delay
        scheduler.scheduleWithFixedDelay(this::fetchTransactions, 0, delaySeconds, TimeUnit.SECONDS);
    }

    private void stopScheduler() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void fetchTransactions() {
        Platform.runLater(() -> {
            lblStatusBadge.setText("ĐANG TẢI...");
            lblStatusBadge.setStyle(
                    "-fx-background-color: rgba(251, 146, 60, 0.08);" +
                            "-fx-background-radius: 8px;" +
                            "-fx-text-fill: #fdba74;" +
                            "-fx-border-color: rgba(251, 146, 60, 0.4);" +
                            "-fx-border-radius: 8px;" +
                            "-fx-border-width: 1px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 11px;");
        });

        try {
            // 1. Get Account Balance
            Map<String, Object> detail = client.getAccountDetail();
            String balanceText = null;
            if (detail != null && detail.containsKey("data")) {
                var dataList = (List<Map<String, Object>>) detail.get("data");
                if (dataList != null && !dataList.isEmpty()) {
                    Object bal = dataList.get(0).get("balance");
                    if (bal instanceof Number) {
                        balanceText = MONEY_FMT.format(((Number) bal).longValue()) + " VND";
                    }
                }
            }

            // 2. Get Transaction lists
            Map<String, Object> tx = client.getTransactions(50);
            List<BankTransaction> transactions = AcbTransactionParser.parse(tx, null);
            int newPushed = BankDataManager.pushTransactions(transactions);
            int apiMatched = BankDataManager.getLastMatchedCount();

            // Refresh UI components
            final String finalBalance = balanceText;
            Platform.runLater(() -> {
                if (finalBalance != null) {
                    lblBalance.setText(finalBalance);
                }

                transactionList.clear();
                transactionList.addAll(transactions);

                depositList.clear();
                depositList.addAll(BankDataManager.getDeposits());

                // Update Deposit Tab Title with count
                tabDep.setText("Lịch sử đẩy (" + depositList.size() + ")");

                lblStatusBadge.setText("ĐANG CHẠY");
                lblStatusBadge.setStyle(
                        "-fx-background-color: rgba(34, 197, 94, 0.08);" +
                                "-fx-background-radius: 8px;" +
                                "-fx-text-fill: #4ade80;" +
                                "-fx-border-color: rgba(34, 197, 94, 0.4);" +
                                "-fx-border-radius: 8px;" +
                                "-fx-border-width: 1px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 11px;");

                if (apiMatched > 0) {
                    appendLog("[POLL] Khớp " + apiMatched + " đơn đẩy thành công lên API!");
                }
                if (newPushed > 0) {
                    appendLog("[POLL] Lưu " + newPushed + " giao dịch mới vào lịch sử local.");
                }
            });
        } catch (Exception ex) {
            String errorMsg = "Lỗi nạp giao dịch: " + ex.getMessage();
            Platform.runLater(() -> {
                lblStatusBadge.setText("LỖI PHIÊN");
                lblStatusBadge.setStyle(
                        "-fx-background-color: rgba(239, 68, 68, 0.08);" +
                                "-fx-background-radius: 8px;" +
                                "-fx-text-fill: #f87171;" +
                                "-fx-border-color: rgba(239, 68, 68, 0.4);" +
                                "-fx-border-radius: 8px;" +
                                "-fx-border-width: 1px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-size: 11px;");
                appendLog("[POLL] " + errorMsg);
            });
        }
    }

    public void destroy() {
        stopScheduler();
    }
}
