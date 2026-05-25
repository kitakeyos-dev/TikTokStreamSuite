package com.leaderboard.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;

public final class Dialogs {
    private static final String STYLESHEET = "/css/dialogs.css";

    private Dialogs() {
    }

    public static void info(Window owner, String title, String message) {
        showMessage(owner, Alert.AlertType.INFORMATION, title, message);
    }

    public static void warning(Window owner, String title, String message) {
        showMessage(owner, Alert.AlertType.WARNING, title, message);
    }

    public static void error(Window owner, String title, String message) {
        showMessage(owner, Alert.AlertType.ERROR, title, message);
    }

    public static boolean confirm(Window owner, String title, String message, String confirmText) {
        Stage stage = createStage(owner);
        boolean[] confirmed = {false};

        VBox card = createCard(Alert.AlertType.CONFIRMATION, title, message);
        HBox actions = createActions();

        Button cancelButton = secondaryButton("Huỷ");
        cancelButton.setOnAction(e -> stage.close());

        Button confirmButton = primaryButton(confirmText, "danger-button");
        confirmButton.setOnAction(e -> {
            confirmed[0] = true;
            stage.close();
        });

        actions.getChildren().addAll(cancelButton, confirmButton);
        card.getChildren().add(actions);
        show(stage, card);
        return confirmed[0];
    }

    public static Optional<String> input(Window owner, String title, String prompt, String label, String defaultValue) {
        Stage stage = createStage(owner);
        String[] value = {null};

        VBox card = createCard(Alert.AlertType.INFORMATION, title, prompt);

        Label fieldLabel = new Label(label);
        fieldLabel.getStyleClass().add("field-label");

        TextField input = new TextField(defaultValue);
        input.getStyleClass().add("tss-text-field");
        input.setPrefHeight(38);

        VBox fieldBox = new VBox(7, fieldLabel, input);
        fieldBox.setPadding(new Insets(2, 0, 2, 0));
        card.getChildren().add(fieldBox);

        HBox actions = createActions();
        Button cancelButton = secondaryButton("Huỷ");
        cancelButton.setOnAction(e -> stage.close());

        Button okButton = primaryButton("OK", "default-button");
        okButton.setOnAction(e -> {
            value[0] = input.getText();
            stage.close();
        });

        input.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                value[0] = input.getText();
                stage.close();
            }
        });

        actions.getChildren().addAll(cancelButton, okButton);
        card.getChildren().add(actions);
        show(stage, card);
        return Optional.ofNullable(value[0]);
    }

    private static void showMessage(Window owner, Alert.AlertType type, String title, String message) {
        Stage stage = createStage(owner);
        VBox card = createCard(type, title, message);

        HBox actions = createActions();
        Button okButton = primaryButton("OK", "default-button");
        okButton.setOnAction(e -> stage.close());
        actions.getChildren().add(okButton);
        card.getChildren().add(actions);

        show(stage, card);
    }

    private static Stage createStage(Window owner) {
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        return stage;
    }

    private static VBox createCard(Alert.AlertType type, String title, String message) {
        FontIcon typeIcon = createIcon(type);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modal-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = iconButton(Feather.X);

        HBox header = new HBox(12, typeIcon, titleLabel, spacer, closeButton);
        header.setAlignment(Pos.CENTER_LEFT);

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("modal-message");
        messageLabel.setWrapText(true);

        VBox card = new VBox(14, header, messageLabel);
        card.getStyleClass().addAll("tss-modal", styleClassFor(type));
        card.setPrefWidth(390);
        card.setMinWidth(390);
        card.setMaxWidth(390);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.42));
        shadow.setRadius(22);
        shadow.setSpread(0.12);
        shadow.setOffsetY(8);
        card.setEffect(shadow);

        closeButton.setOnAction(e -> card.getScene().getWindow().hide());
        return card;
    }

    private static HBox createActions() {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(4, 0, 0, 0));
        return actions;
    }

    private static Button primaryButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("modal-button", styleClass);
        button.setMinWidth(76);
        button.setPrefHeight(36);
        return button;
    }

    private static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().addAll("modal-button", "secondary-button");
        button.setMinWidth(76);
        button.setPrefHeight(36);
        return button;
    }

    private static Button iconButton(Feather feather) {
        FontIcon icon = new FontIcon(feather);
        icon.setIconSize(15);
        icon.setIconColor(Color.web("#a1a1aa"));

        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().add("modal-close-button");
        button.setPrefSize(28, 28);
        button.setMinSize(28, 28);
        button.setMaxSize(28, 28);
        return button;
    }

    private static void show(Stage stage, VBox card) {
        StackPane root = new StackPane(card);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        var css = Dialogs.class.getResource(STYLESHEET);
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setScene(scene);
        stage.showAndWait();
    }

    private static String styleClassFor(Alert.AlertType type) {
        switch (type) {
            case ERROR:
                return "tss-error";
            case WARNING:
                return "tss-warning";
            case INFORMATION:
                return "tss-information";
            default:
                return "tss-confirmation";
        }
    }

    private static FontIcon createIcon(Alert.AlertType type) {
        Feather feather;
        switch (type) {
            case ERROR:
                feather = Feather.X_CIRCLE;
                break;
            case WARNING:
                feather = Feather.ALERT_TRIANGLE;
                break;
            case CONFIRMATION:
                feather = Feather.HELP_CIRCLE;
                break;
            default:
                feather = Feather.INFO;
                break;
        }

        FontIcon icon = new FontIcon(feather);
        icon.setIconSize(24);
        
        Color iconColor;
        switch (type) {
            case ERROR:
                iconColor = Color.web("#f87171");
                break;
            case WARNING:
                iconColor = Color.web("#f59e0b");
                break;
            case CONFIRMATION:
                iconColor = Color.web("#818cf8");
                break;
            default:
                iconColor = Color.web("#25f4ee");
                break;
        }
        icon.setIconColor(iconColor);
        icon.getStyleClass().add("modal-type-icon");
        return icon;
    }
}
