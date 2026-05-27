package com.leaderboard.ui.tab;

import com.leaderboard.model.action.ActionType;
import com.leaderboard.model.action.StreamRule;
import com.leaderboard.model.action.TriggerType;
import com.leaderboard.service.action.ActionRulesEngine;
import com.leaderboard.ui.DashboardLayout;
import com.leaderboard.ui.DashboardStage;
import com.leaderboard.ui.Dialogs;
import com.leaderboard.ui.ToggleSwitch;
import com.leaderboard.util.I18n;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;

/**
 * Premium dashboard tab for creating, managing and simulating custom Actions & Events.
 */
public class ActionsEventsTab extends BorderPane {
    private final DashboardStage parent;
    private final TableView<StreamRule> tblRules;
    private final ObservableList<StreamRule> rulesData = FXCollections.observableArrayList();

    // Add form controls
    private final TextField txtRuleName;
    private final ComboBox<TriggerTypeWrapper> cbTriggerType;
    private final TextField txtTriggerTarget;
    private final ComboBox<ActionTypeWrapper> cbActionType;
    private final TextField txtTtsPayload;
    private final TextField txtSoundPath;
    private final Button btnBrowseSound;
    private final HBox soundPayloadBox;
    private final VBox targetFormGroup;

    public ActionsEventsTab(DashboardStage parent) {
        this.parent = parent;
        DashboardLayout.stylePage(this);

        // Header
        HBox header = DashboardLayout.createPageHeader(
                I18n.get("actionsEvents.title"),
                I18n.get("actionsEvents.subtitle")
        );
        setTop(header);

        GridPane grid = DashboardLayout.createTwoColumnGrid();

        // --- LEFT COLUMN: Add New Rule ---
        VBox cardAdd = DashboardLayout.createCard(I18n.get("actionsEvents.card.add"));
        VBox addForm = DashboardLayout.createSectionContent();

        // 1. Rule Name
        Label lblName = DashboardLayout.createFieldLabel("TÊN QUY TẮC / RULE NAME:");
        txtRuleName = DashboardLayout.newTextField();
        HBox nameFieldBox = DashboardLayout.wrapTextField(txtRuleName, "Ví dụ: Nhạc dọa ma, Cảm ơn Follow...");

        // 2. Trigger Type
        Label lblTrigger = DashboardLayout.createFieldLabel(I18n.get("actionsEvents.label.trigger"));
        cbTriggerType = new ComboBox<>();
        cbTriggerType.getItems().addAll(
                new TriggerTypeWrapper(TriggerType.GIFT, "Tặng quà / Receive Gift"),
                new TriggerTypeWrapper(TriggerType.FOLLOW, "Người theo dõi mới / New Follower"),
                new TriggerTypeWrapper(TriggerType.SHARE, "Chia sẻ Live / Share Stream"),
                new TriggerTypeWrapper(TriggerType.SUBSCRIBE, "Hội viên mới / New Subscriber"),
                new TriggerTypeWrapper(TriggerType.CHAT, "Lệnh chat / Chat Command")
        );
        cbTriggerType.setValue(cbTriggerType.getItems().get(0));
        DashboardLayout.styleComboBox(cbTriggerType);
        cbTriggerType.setMaxWidth(Double.MAX_VALUE);

        // 3. Trigger Target (GIFT or CHAT only)
        Label lblTarget = DashboardLayout.createFieldLabel(I18n.get("actionsEvents.label.target"));
        txtTriggerTarget = DashboardLayout.newTextField();
        HBox targetFieldBox = DashboardLayout.wrapTextField(txtTriggerTarget, "Ví dụ: Rose hoặc !discord");
        targetFormGroup = new VBox(DashboardLayout.FORM_GAP, lblTarget, targetFieldBox);

        // Toggle target field visibility depending on trigger type
        cbTriggerType.setOnAction(e -> {
            TriggerType t = cbTriggerType.getValue().type;
            boolean showTarget = (t == TriggerType.GIFT || t == TriggerType.CHAT);
            targetFormGroup.setVisible(showTarget);
            targetFormGroup.setManaged(showTarget);
        });

        // 4. Action Type
        Label lblAction = DashboardLayout.createFieldLabel(I18n.get("actionsEvents.label.action"));
        cbActionType = new ComboBox<>();
        cbActionType.getItems().addAll(
                new ActionTypeWrapper(ActionType.SOUND, "Phát âm thanh / Play Sound Alert"),
                new ActionTypeWrapper(ActionType.TTS, "Đọc giọng nói / Speak TTS Message")
        );
        cbActionType.setValue(cbActionType.getItems().get(0));
        DashboardLayout.styleComboBox(cbActionType);
        cbActionType.setMaxWidth(Double.MAX_VALUE);

        // 5. Action Payload
        Label lblPayload = DashboardLayout.createFieldLabel(I18n.get("actionsEvents.label.payload"));
        
        // TTS payload
        txtTtsPayload = DashboardLayout.newTextField();
        HBox ttsPayloadBox = DashboardLayout.wrapTextField(txtTtsPayload, "Placeholder: %nickname%, %target%, %payload%");

        // Sound payload (FileChooser)
        txtSoundPath = DashboardLayout.newTextField();
        txtSoundPath.setEditable(false);
        txtSoundPath.setPromptText("Chọn tệp tin MP3...");
        btnBrowseSound = DashboardLayout.newButton(I18n.get("actionsEvents.btn.chooseFile"));
        DashboardLayout.applySecondaryButton(btnBrowseSound);
        btnBrowseSound.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Chọn tệp âm thanh hiệu ứng");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP3 Audio", "*.mp3"));
            File file = chooser.showOpenDialog(parent.getScene().getWindow());
            if (file != null) {
                txtSoundPath.setText(file.getAbsolutePath());
            }
        });
        soundPayloadBox = new HBox(8, DashboardLayout.wrapTextField(txtSoundPath, "Chưa chọn tệp..."), btnBrowseSound);
        HBox.setHgrow(soundPayloadBox.getChildren().get(0), Priority.ALWAYS);

        VBox payloadFormGroup = new VBox(DashboardLayout.FORM_GAP, lblPayload, soundPayloadBox, ttsPayloadBox);

        // Toggle payload field visibility depending on action type
        cbActionType.setOnAction(e -> {
            boolean isSound = cbActionType.getValue().type == ActionType.SOUND;
            soundPayloadBox.setVisible(isSound);
            soundPayloadBox.setManaged(isSound);
            ttsPayloadBox.setVisible(!isSound);
            ttsPayloadBox.setManaged(!isSound);
        });
        
        // Init form state
        ttsPayloadBox.setVisible(false);
        ttsPayloadBox.setManaged(false);

        // 6. Add Button
        Button btnAdd = DashboardLayout.newButton(I18n.get("actionsEvents.btn.addRule"));
        DashboardLayout.applyPrimaryButton(btnAdd);
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setOnAction(e -> handleAddRule());

        addForm.getChildren().addAll(
                lblName, nameFieldBox,
                lblTrigger, cbTriggerType,
                targetFormGroup,
                lblAction, cbActionType,
                payloadFormGroup,
                btnAdd
        );
        cardAdd.getChildren().add(addForm);
        grid.add(cardAdd, 0, 0);
        DashboardLayout.fillGridCell(cardAdd);

        // --- RIGHT COLUMN: Rules List ---
        VBox cardList = DashboardLayout.createCard(I18n.get("actionsEvents.card.list"));
        tblRules = DashboardLayout.createTable();

        TableColumn<StreamRule, String> colName = new TableColumn<>(I18n.get("actionsEvents.col.name"));
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colName.setPrefWidth(130);

        TableColumn<StreamRule, String> colTrigger = new TableColumn<>(I18n.get("actionsEvents.col.trigger"));
        colTrigger.setCellValueFactory(d -> {
            StreamRule r = d.getValue();
            String desc = r.getTriggerType().toString();
            if (r.getTriggerTarget() != null && !r.getTriggerTarget().trim().isEmpty()) {
                desc += " (" + r.getTriggerTarget() + ")";
            }
            return new SimpleStringProperty(desc);
        });
        colTrigger.setPrefWidth(120);

        TableColumn<StreamRule, String> colAction = new TableColumn<>(I18n.get("actionsEvents.col.action"));
        colAction.setCellValueFactory(d -> {
            StreamRule r = d.getValue();
            String payload = r.getActionPayload();
            if (r.getActionType() == ActionType.SOUND) {
                File f = new File(payload);
                payload = f.getName(); // Show file name instead of full absolute path
            }
            return new SimpleStringProperty(r.getActionType() + ": " + payload);
        });
        colAction.setPrefWidth(180);

        TableColumn<StreamRule, Void> colActions = new TableColumn<>(I18n.get("actionsEvents.col.status"));
        colActions.setPrefWidth(180);
        colActions.setCellFactory(col -> new TableCell<>(){
            private final Button btnSimulate = new Button();
            private final ToggleSwitch swEnabled = new ToggleSwitch();
            private final Button btnDelete = new Button();
            private final HBox layout = new HBox(8, btnSimulate, swEnabled, btnDelete);

            {
                layout.setAlignment(Pos.CENTER);
                
                // Simulate button setup
                FontIcon playIcon = new FontIcon(Feather.PLAY);
                playIcon.setIconColor(Color.web("#4ade80"));
                btnSimulate.setGraphic(playIcon);
                btnSimulate.setTooltip(new Tooltip(I18n.get("actionsEvents.btn.simulate")));
                btnSimulate.setStyle("-fx-background-color: rgba(34, 197, 94, 0.08); -fx-border-color: rgba(34, 197, 94, 0.25); -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-cursor: hand;");
                btnSimulate.setOnAction(e -> {
                    StreamRule rule = getTableView().getItems().get(getIndex());
                    ActionRulesEngine.triggerRule(rule, "simulated_user", "Khán Giả Ảo 🎩", "Simulated Event");
                });

                // Delete button setup
                FontIcon trashIcon = new FontIcon(Feather.TRASH_2);
                trashIcon.setIconColor(Color.web("#f87171"));
                btnDelete.setGraphic(trashIcon);
                btnDelete.setTooltip(new Tooltip(I18n.get("actionsEvents.btn.delete")));
                btnDelete.setStyle("-fx-background-color: rgba(239, 68, 68, 0.08); -fx-border-color: rgba(239, 68, 68, 0.25); -fx-border-radius: 4px; -fx-background-radius: 4px; -fx-cursor: hand;");
                btnDelete.setOnAction(e -> {
                    StreamRule rule = getTableView().getItems().get(getIndex());
                    handleDeleteRule(rule);
                });

                // Toggle switch setup
                swEnabled.setOnToggle(() -> {
                    StreamRule rule = getTableView().getItems().get(getIndex());
                    rule.setEnabled(swEnabled.isSelected());
                    ActionRulesEngine.save();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    StreamRule rule = getTableView().getItems().get(getIndex());
                    swEnabled.setSelected(rule.isEnabled());
                    setGraphic(layout);
                }
            }
        });

        tblRules.getColumns().addAll(colName, colTrigger, colAction, colActions);
        tblRules.setItems(rulesData);
        
        cardList.getChildren().add(tblRules);
        grid.add(cardList, 1, 0);
        DashboardLayout.fillGridCell(cardList);

        setCenter(grid);
        BorderPane.setMargin(grid, new Insets(15, 0, 0, 0));

        // Load initial rules data
        refreshRulesTable();
    }

    private void refreshRulesTable() {
        rulesData.clear();
        rulesData.addAll(ActionRulesEngine.getRules());
    }

    private void handleAddRule() {
        String name = txtRuleName.getText().trim();
        TriggerType trigger = cbTriggerType.getValue().type;
        String target = txtTriggerTarget.getText().trim();
        ActionType action = cbActionType.getValue().type;
        String payload = (action == ActionType.SOUND) ? txtSoundPath.getText().trim() : txtTtsPayload.getText().trim();

        if (name.isEmpty() || payload.isEmpty()) {
            Dialogs.warning(parent.getScene().getWindow(), I18n.get("dialog.warning"), I18n.get("actionsEvents.warn.empty"));
            return;
        }

        if ((trigger == TriggerType.GIFT || trigger == TriggerType.CHAT) && target.isEmpty()) {
            Dialogs.warning(parent.getScene().getWindow(), I18n.get("dialog.warning"), I18n.get("actionsEvents.warn.empty"));
            return;
        }

        StreamRule newRule = new StreamRule(name, trigger, target, action, payload);
        ActionRulesEngine.addRule(newRule);
        
        // Reset form
        txtRuleName.clear();
        txtTriggerTarget.clear();
        txtSoundPath.clear();
        txtTtsPayload.clear();

        // Refresh UI table
        refreshRulesTable();

        Dialogs.info(parent.getScene().getWindow(), I18n.get("dialog.success"), I18n.get("actionsEvents.success.add"));
    }

    private void handleDeleteRule(StreamRule rule) {
        ActionRulesEngine.deleteRule(rule.getId());
        refreshRulesTable();
    }

    // Helper wrappers to show beautiful localized text in ComboBoxes
    private static class TriggerTypeWrapper {
        final TriggerType type;
        final String displayName;

        TriggerTypeWrapper(TriggerType type, String displayName) {
            this.type = type;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static class ActionTypeWrapper {
        final ActionType type;
        final String displayName;

        ActionTypeWrapper(ActionType type, String displayName) {
            this.type = type;
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
