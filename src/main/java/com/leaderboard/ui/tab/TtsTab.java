package com.leaderboard.ui.tab;

import com.leaderboard.service.TTSService;
import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.ui.ToggleSwitch;
import com.leaderboard.util.ConfigManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class TtsTab extends BorderPane {
    private final DashboardStage parent;

    private ToggleSwitch swTtsEnabled;
    private ToggleSwitch swTtsReadUsername;
    private ComboBox<String> comboAudioDevice;
    private Slider sliderVolume;
    private Label lblVolumeVal;
    private Slider sliderQueueLimit;
    private Label lblQueueLimitVal;
    
    private TextField txtTestTts;
    private Button btnPlayTest;
    private Button btnClearQueue;
    private TextArea taBlockedWords;
    private Button btnSaveConfig;

    public TtsTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);
        initComponents();
    }

    private void initComponents() {
        GridPane grid = DashboardLayout.createTwoColumnGrid();

        // --- COLUMN 1: CONFIG & TEST ---
        VBox cardConfig = DashboardLayout.createCard("CẤU HÌNH GIỌNG NÓI (TTS)");
        VBox leftContent = DashboardLayout.createSectionContent();

        // Setting 1: Enable/Disable TTS
        swTtsEnabled = DashboardLayout.newToggleSwitch();
        swTtsEnabled.setSelected(ConfigManager.getConfig().isTtsEnabled());
        swTtsEnabled.setOnToggle(() -> {
            ConfigManager.getConfig().setTtsEnabled(swTtsEnabled.isSelected());
            ConfigManager.save();
            if (!swTtsEnabled.isSelected()) {
                TTSService.stopAndClear();
            }
        });
        HBox rowTts = createSettingRow(
                "ĐỌC BÌNH LUẬN (TTS)",
                "Tự động phát giọng đọc tiếng Việt khi có người xem bình luận trên Live stream.",
                "#818cf8",
                Feather.VOLUME_2,
                swTtsEnabled
        );

        // Setting 2: Read Username Toggle
        swTtsReadUsername = DashboardLayout.newToggleSwitch();
        swTtsReadUsername.setSelected(ConfigManager.getConfig().isTtsReadUsername());
        swTtsReadUsername.setOnToggle(() -> {
            ConfigManager.getConfig().setTtsReadUsername(swTtsReadUsername.isSelected());
            ConfigManager.save();
        });
        HBox rowUsername = createSettingRow(
                "ĐỌC TÊN NGƯỜI CHAT",
                "Đọc kèm tên người dùng. Ví dụ: 'Nguyễn Văn A nói: Xin chào'. Nếu tắt, chỉ đọc 'Xin chào'.",
                "#818cf8",
                Feather.USER,
                swTtsReadUsername
        );

        // Setting 3: Audio Output Device Dropdown
        comboAudioDevice = new ComboBox<>();
        DashboardLayout.styleComboBox(comboAudioDevice);
        List<String> devices = TTSService.getAudioOutputDevices();
        comboAudioDevice.getItems().addAll(devices);
        
        String savedDevice = ConfigManager.getConfig().getTtsAudioDeviceName();
        if (devices.contains(savedDevice)) {
            comboAudioDevice.setValue(savedDevice);
        } else {
            comboAudioDevice.setValue("Default");
        }
        
        comboAudioDevice.setOnAction(e -> {
            String selected = comboAudioDevice.getValue();
            if (selected != null) {
                ConfigManager.getConfig().setTtsAudioDeviceName(selected);
                ConfigManager.save();
            }
        });
        HBox rowAudioDevice = createComboSettingRow(
                "THIẾT BỊ ĐẦU RA ÂM THANH",
                "Chọn loa, tai nghe hoặc cáp âm thanh ảo để phát riêng biệt tiếng đọc bình luận.",
                "#818cf8",
                Feather.HEADPHONES,
                comboAudioDevice
        );

        // Setting 4: TTS Volume Slider
        double initialVol = ConfigManager.getConfig().getTtsVolume() * 100.0;
        lblVolumeVal = new Label((int) initialVol + "%");
        sliderVolume = new Slider(0, 100, initialVol);
        sliderVolume.setBlockIncrement(5);
        sliderVolume.valueProperty().addListener((obs, oldVal, newVal) -> {
            double vol = newVal.doubleValue() / 100.0;
            ConfigManager.getConfig().setTtsVolume(vol);
            ConfigManager.save();
            lblVolumeVal.setText((int) newVal.doubleValue() + "%");
            TTSService.updateVolume(vol); // Cập nhật trực tiếp âm lượng đang phát nếu có
        });
        HBox rowVolume = createSliderSettingRow(
                "ÂM LƯỢNG GIỌNG ĐỌC",
                "Điều chỉnh độ to nhỏ của âm thanh TTS phát ra từ ứng dụng.",
                "#818cf8",
                Feather.SPEAKER,
                sliderVolume,
                lblVolumeVal
        );

        // Setting 5: Max Queue Limit Slider
        int initialQueueLimit = ConfigManager.getConfig().getTtsMaxQueue();
        lblQueueLimitVal = new Label(initialQueueLimit + " bình luận");
        sliderQueueLimit = new Slider(1, 25, initialQueueLimit);
        sliderQueueLimit.setBlockIncrement(1);
        sliderQueueLimit.setSnapToTicks(true);
        sliderQueueLimit.valueProperty().addListener((obs, oldVal, newVal) -> {
            int limit = newVal.intValue();
            ConfigManager.getConfig().setTtsMaxQueue(limit);
            ConfigManager.save();
            lblQueueLimitVal.setText(limit + " bình luận");
        });
        HBox rowQueueLimit = createSliderSettingRow(
                "GIỚI HẠN HÀNG ĐỢI (CHỐNG NGHẼN)",
                "Số lượng bình luận chờ đọc tối đa. Vượt quá mức này sẽ bỏ qua các bình luận cũ nhất.",
                "#818cf8",
                Feather.SLIDERS,
                sliderQueueLimit,
                lblQueueLimitVal
        );

        // Divider
        Separator sep = new Separator();
        sep.setStyle("-fx-opacity: 0.08; -fx-padding: 10 0 10 0;");

        // Section Test TTS
        Label lblTestTitle = DashboardLayout.createFieldLabel("PHÁT THỬ GIỌNG NÓI / HÀNG ĐỢI:");
        txtTestTts = DashboardLayout.newTextField();
        HBox testFieldBox = DashboardLayout.wrapTextField(txtTestTts, "Nhập nội dung cần đọc thử vào đây...");

        // Play and Clear Queue buttons
        btnPlayTest = DashboardLayout.newButton("Đọc Thử");
        FontIcon playIcon = new FontIcon(Feather.PLAY);
        playIcon.setIconColor(Color.web("#4ade80"));
        btnPlayTest.setGraphic(playIcon);
        DashboardLayout.applyPrimaryButton(btnPlayTest);
        btnPlayTest.setOnAction(e -> {
            String text = txtTestTts.getText().trim();
            if (!text.isEmpty()) {
                // Temporarily force enable TTS for manual testing even if globally disabled
                boolean prevVal = ConfigManager.getConfig().isTtsEnabled();
                ConfigManager.getConfig().setTtsEnabled(true);
                TTSService.enqueueTTS(text);
                ConfigManager.getConfig().setTtsEnabled(prevVal);
            }
        });

        btnClearQueue = DashboardLayout.newButton("Dừng & Xoá Hàng Đợi");
        FontIcon stopIcon = new FontIcon(Feather.STOP_CIRCLE);
        stopIcon.setIconColor(Color.web("#f87171"));
        btnClearQueue.setGraphic(stopIcon);
        DashboardLayout.applyDangerButton(btnClearQueue);
        btnClearQueue.setOnAction(e -> {
            TTSService.stopAndClear();
            txtTestTts.clear();
        });

        HBox actionsRow = DashboardLayout.createActionsRow(btnPlayTest, btnClearQueue);

        leftContent.getChildren().addAll(rowTts, rowUsername, rowAudioDevice, rowVolume, rowQueueLimit, sep, lblTestTitle, testFieldBox, actionsRow);
        cardConfig.getChildren().add(leftContent);
        grid.add(cardConfig, 0, 0);
        DashboardLayout.fillGridCell(cardConfig);

        // --- COLUMN 2: BAD WORDS FILTER ---
        VBox cardBlockedWords = DashboardLayout.createCard("BỘ LỌC TỪ KHÓA CẤM / PHẢN CẢM");
        VBox rightContent = DashboardLayout.createSectionContent();

        Label lblBlockedInfo = DashboardLayout.createFieldLabel("TỪ KHÓA CHẶN ĐỌC TTS (CÁCH NHAU BẰNG DẤU PHẨY HOẶC XUỐNG DÒNG):");
        
        taBlockedWords = new TextArea();
        taBlockedWords.setPrefHeight(300);
        taBlockedWords.setWrapText(true);
        taBlockedWords.setText(ConfigManager.getConfig().getTtsBlockedWords());
        taBlockedWords.setPromptText("Nhập các từ phản cảm, tục tĩu tại đây để tránh phát âm thanh vi phạm...");
        taBlockedWords.setStyle(
                "-fx-control-inner-background: #18181b;" +
                "-fx-background-color: #18181b;" +
                "-fx-text-fill: #f4f4f5;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-radius: 8px;" +
                "-fx-background-radius: 8px;" +
                "-fx-padding: 8px;" +
                "-fx-font-family: 'Segoe UI', system-ui;"
        );

        btnSaveConfig = DashboardLayout.newButton("Lưu Bộ Lọc Từ Cấm");
        FontIcon saveIcon = new FontIcon(Feather.SAVE);
        saveIcon.setIconColor(Color.web("#818cf8"));
        btnSaveConfig.setGraphic(saveIcon);
        DashboardLayout.applySuccessButton(btnSaveConfig);
        btnSaveConfig.setOnAction(e -> {
            ConfigManager.getConfig().setTtsBlockedWords(taBlockedWords.getText().trim());
            ConfigManager.save();
            
            Dialogs.info(getScene().getWindow(), "Thông báo", "Đã lưu danh sách từ khóa cấm thành công!");
        });

        HBox saveRow = DashboardLayout.createActionsRow(btnSaveConfig);

        rightContent.getChildren().addAll(lblBlockedInfo, taBlockedWords, saveRow);
        cardBlockedWords.getChildren().add(rightContent);
        grid.add(cardBlockedWords, 1, 0);
        DashboardLayout.fillGridCell(cardBlockedWords);

        setCenter(grid);
    }

    private HBox createSettingRow(String title, String desc, String iconColorHex, Feather icon, ToggleSwitch sw) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(8, 5, 8, 5));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-border-color: rgba(255, 255, 255, 0.05);" +
                "-fx-border-width: 0 0 1px 0;"
        );

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(38, 38);
        iconBox.setMinSize(38, 38);
        iconBox.setMaxSize(38, 38);
        iconBox.setStyle(
                "-fx-background-color: #121214;" +
                "-fx-background-radius: 6px;" +
                "-fx-border-color: rgba(255,255,255,0.06);" +
                "-fx-border-radius: 6px;" +
                "-fx-border-width: 1px;"
        );

        Region leftBar = new Region();
        leftBar.setPrefWidth(3);
        leftBar.setMaxHeight(Double.MAX_VALUE);
        leftBar.setStyle("-fx-background-color: " + iconColorHex + "66; -fx-background-radius: 3px 0 0 3px;");
        StackPane.setAlignment(leftBar, Pos.CENTER_LEFT);

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(Color.web(iconColorHex, 0.8));

        iconBox.getChildren().addAll(leftBar, fontIcon);

        VBox textGroup = new VBox(2);
        HBox.setHgrow(textGroup, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: #71717a; -fx-font-size: 9.5px;");

        textGroup.getChildren().addAll(lblTitle, lblDesc);

        row.getChildren().addAll(iconBox, textGroup, sw);
        return row;
    }

    private HBox createComboSettingRow(String title, String desc, String iconColorHex, Feather icon, ComboBox<String> combo) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(8, 5, 8, 5));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-border-color: rgba(255, 255, 255, 0.05);" +
                "-fx-border-width: 0 0 1px 0;"
        );

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(38, 38);
        iconBox.setMinSize(38, 38);
        iconBox.setMaxSize(38, 38);
        iconBox.setStyle(
                "-fx-background-color: #121214;" +
                "-fx-background-radius: 6px;" +
                "-fx-border-color: rgba(255,255,255,0.06);" +
                "-fx-border-radius: 6px;" +
                "-fx-border-width: 1px;"
        );

        Region leftBar = new Region();
        leftBar.setPrefWidth(3);
        leftBar.setMaxHeight(Double.MAX_VALUE);
        leftBar.setStyle("-fx-background-color: " + iconColorHex + "66; -fx-background-radius: 3px 0 0 3px;");
        StackPane.setAlignment(leftBar, Pos.CENTER_LEFT);

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(Color.web(iconColorHex, 0.8));

        iconBox.getChildren().addAll(leftBar, fontIcon);

        VBox textGroup = new VBox(2);
        HBox.setHgrow(textGroup, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: #71717a; -fx-font-size: 9.5px;");

        textGroup.getChildren().addAll(lblTitle, lblDesc);

        combo.setPrefWidth(150);
        combo.setMinWidth(150);
        combo.setMaxWidth(150);

        row.getChildren().addAll(iconBox, textGroup, combo);
        return row;
    }

    private HBox createSliderSettingRow(String title, String desc, String iconColorHex, Feather icon, Slider slider, Label valueLabel) {
        HBox row = new HBox(12);
        row.setPadding(new Insets(8, 5, 8, 5));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-border-color: rgba(255, 255, 255, 0.05);" +
                "-fx-border-width: 0 0 1px 0;"
        );

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(38, 38);
        iconBox.setMinSize(38, 38);
        iconBox.setMaxSize(38, 38);
        iconBox.setStyle(
                "-fx-background-color: #121214;" +
                "-fx-background-radius: 6px;" +
                "-fx-border-color: rgba(255,255,255,0.06);" +
                "-fx-border-radius: 6px;" +
                "-fx-border-width: 1px;"
        );

        Region leftBar = new Region();
        leftBar.setPrefWidth(3);
        leftBar.setMaxHeight(Double.MAX_VALUE);
        leftBar.setStyle("-fx-background-color: " + iconColorHex + "66; -fx-background-radius: 3px 0 0 3px;");
        StackPane.setAlignment(leftBar, Pos.CENTER_LEFT);

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(Color.web(iconColorHex, 0.8));

        iconBox.getChildren().addAll(leftBar, fontIcon);

        VBox textGroup = new VBox(2);
        HBox.setHgrow(textGroup, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: bold; -fx-font-size: 11px;");

        Label lblDesc = new Label(desc);
        lblDesc.setStyle("-fx-text-fill: #71717a; -fx-font-size: 9.5px;");

        textGroup.getChildren().addAll(lblTitle, lblDesc);

        VBox sliderGroup = new VBox(4);
        sliderGroup.setAlignment(Pos.CENTER_RIGHT);
        sliderGroup.setMinWidth(150);
        sliderGroup.setMaxWidth(150);
        
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setStyle("-fx-cursor: hand;");

        valueLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-weight: bold; -fx-font-size: 10px;");
        
        sliderGroup.getChildren().addAll(slider, valueLabel);

        row.getChildren().addAll(iconBox, textGroup, sliderGroup);
        return row;
    }
}
