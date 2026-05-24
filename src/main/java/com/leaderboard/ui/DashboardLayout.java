package com.leaderboard.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Shared dashboard layout tokens and factories for consistent tab UI.
 */
public final class DashboardLayout {
    public static final Insets PAGE_PADDING = new Insets(15, 0, 15, 0);
    public static final Insets CARD_PADDING = new Insets(15, 20, 15, 20);
    public static final Insets SECTION_PADDING = new Insets(10, 0, 10, 0);
    public static final double GRID_HGAP = 24;
    public static final double LEFT_COL_PERCENT = 40;
    public static final double RIGHT_COL_PERCENT = 60;
    public static final double TABLE_PREF_HEIGHT = 420;
    public static final double FORM_GAP = 10;
    public static final double BUTTON_HEIGHT = 32;
    public static final double BUTTON_FONT_SIZE = 11;
    public static final double BUTTON_FONT_SIZE_COMPACT = 10;
    public static final double NAV_BUTTON_FONT_SIZE = 11;
    public static final double FIELD_HEIGHT = 34;

    private static final String CARD_STYLE =
            "-fx-background-color: #121214;" +
                    "-fx-background-radius: 12px;" +
                    "-fx-border-color: rgba(255, 255, 255, 0.05);" +
                    "-fx-border-radius: 12px;" +
                    "-fx-border-width: 1px;";

    private static final String FIELD_STYLE =
            "-fx-background-color: #18181b;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                    "-fx-border-radius: 8px;" +
                    "-fx-border-width: 1px;" +
                    "-fx-text-fill: #f4f4f5;" +
                    "-fx-padding: 0 10 0 10;";

    private static final String TABLE_STYLE =
            "-fx-background-color: #121214;" +
                    "-fx-control-inner-background: #121214;" +
                    "-fx-border-color: rgba(255,255,255,0.05);" +
                    "-fx-border-radius: 8px;" +
                    "-fx-background-radius: 8px;";

    private DashboardLayout() {
    }

    public static void stylePage(Pane page) {
        page.setPadding(PAGE_PADDING);
        page.setStyle("-fx-background-color: transparent;");
        page.setMaxWidth(Double.MAX_VALUE);
        page.setMaxHeight(Double.MAX_VALUE);
    }

    public static GridPane createTwoColumnGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(GRID_HGAP);
        grid.setVgap(0);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setMaxHeight(Double.MAX_VALUE);

        ColumnConstraints left = new ColumnConstraints();
        left.setPercentWidth(LEFT_COL_PERCENT);
        ColumnConstraints right = new ColumnConstraints();
        right.setPercentWidth(RIGHT_COL_PERCENT);
        grid.getColumnConstraints().addAll(left, right);

        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().add(row);
        return grid;
     }

    public static void fillGridCell(Node node) {
        GridPane.setVgrow(node, Priority.ALWAYS);
        GridPane.setHgrow(node, Priority.ALWAYS);
        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
    }

    public static VBox createCard(String title) {
        VBox card = new VBox(10);
        card.setPadding(CARD_PADDING);
        card.setStyle(CARD_STYLE);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #f4f4f5; -fx-font-weight: bold; -fx-font-size: 13px;");

        Separator titleSep = new Separator();
        titleSep.setStyle("-fx-opacity: 0.08; -fx-padding: 2 0 5 0;");

        card.getChildren().addAll(lblTitle, titleSep);
        return card;
    }

    public static VBox createPageContainer() {
        VBox card = new VBox(15);
        card.setPadding(CARD_PADDING);
        card.setStyle(CARD_STYLE);
        VBox.setVgrow(card, Priority.ALWAYS);
        return card;
    }

    public static VBox createSectionContent() {
        VBox content = new VBox(FORM_GAP);
        content.setPadding(SECTION_PADDING);
        content.setMaxWidth(Double.MAX_VALUE);
        return content;
    }

    public static Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #71717a;");
        return label;
    }

    /** Creates a pre-styled TextField ready to be passed into wrapTextField(). */
    public static TextField newTextField() {
        TextField f = new TextField();
        f.setPrefHeight(FIELD_HEIGHT);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #f4f4f5; -fx-prompt-text-fill: #52525b;");
        HBox.setHgrow(f, Priority.ALWAYS);
        return f;
    }

    /** Creates a pre-styled PasswordField ready to be passed into wrapPasswordField(). */
    public static PasswordField newPasswordField() {
        PasswordField f = new PasswordField();
        f.setPrefHeight(FIELD_HEIGHT);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #f4f4f5; -fx-prompt-text-fill: #52525b;");
        HBox.setHgrow(f, Priority.ALWAYS);
        return f;
    }

    public static HBox wrapTextField(TextField field, String prompt) {
        return wrapTextField(field, prompt, null);
    }

    public static HBox wrapTextField(TextField field, String prompt, Node icon) {
        field.setPromptText(prompt);
        field.setPrefHeight(FIELD_HEIGHT);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #f4f4f5; -fx-prompt-text-fill: #52525b;");
        HBox.setHgrow(field, Priority.ALWAYS);

        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 10, 0, 10));
        box.setPrefHeight(FIELD_HEIGHT);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle(
                "-fx-background-color: #18181b;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-width: 1px;");
        if (icon != null) box.getChildren().add(icon);
        box.getChildren().add(field);
        return box;
    }

    public static HBox wrapPasswordField(PasswordField field, String prompt) {
        return wrapPasswordField(field, prompt, null);
    }

    public static HBox wrapPasswordField(PasswordField field, String prompt, Node icon) {
        field.setPromptText(prompt);
        field.setPrefHeight(FIELD_HEIGHT);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #f4f4f5; -fx-prompt-text-fill: #52525b;");
        HBox.setHgrow(field, Priority.ALWAYS);

        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 10, 0, 10));
        box.setPrefHeight(FIELD_HEIGHT);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle(
                "-fx-background-color: #18181b;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-width: 1px;");
        if (icon != null) box.getChildren().add(icon);
        box.getChildren().add(field);
        return box;
    }

    public static HBox createSearchBox(TextField field, String prompt) {
        HBox searchBox = new HBox(8);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(0, 10, 0, 10));
        searchBox.setStyle(
                "-fx-background-color: #18181b;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                        "-fx-border-width: 1px;");

        FontIcon searchIcon = new FontIcon(Feather.SEARCH);
        searchIcon.setIconColor(Color.web("#71717a"));

        field.setPromptText(prompt);
        field.setPrefHeight(FIELD_HEIGHT);
        field.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #f4f4f5;");
        HBox.setHgrow(field, Priority.ALWAYS);

        searchBox.getChildren().addAll(searchIcon, field);
        return searchBox;
    }

    /** Creates a pre-styled TextField for use as a search field, ready to pass into createSearchBox(). */
    public static TextField newSearchField() {
        TextField f = new TextField();
        f.setPrefHeight(FIELD_HEIGHT);
        f.setMaxWidth(Double.MAX_VALUE);
        f.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-text-fill: #f4f4f5; -fx-prompt-text-fill: #52525b;");
        HBox.setHgrow(f, Priority.ALWAYS);
        return f;
    }

    public static <T> TableView<T> createTable() {
        TableView<T> table = new TableView<>();
        table.setPrefHeight(TABLE_PREF_HEIGHT);
        table.setMinHeight(TABLE_PREF_HEIGHT);
        table.setStyle(TABLE_STYLE);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    public static HBox createPageHeader(String title, String subtitle, Node... rightNodes) {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        VBox titleArea = new VBox(2);
        HBox.setHgrow(titleArea, Priority.ALWAYS);

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #f4f4f5; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label lblSubtitle = new Label(subtitle);
        lblSubtitle.setStyle("-fx-text-fill: #71717a; -fx-font-size: 10px;");

        titleArea.getChildren().addAll(lblTitle, lblSubtitle);
        header.getChildren().add(titleArea);

        if (rightNodes != null) {
            for (Node node : rightNodes) {
                if (node != null) {
                    header.getChildren().add(node);
                }
            }
        }
        return header;
    }

    public static HBox createHeaderActions(Node... nodes) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().addAll(nodes);
        return box;
    }

    public static VBox createMiniStatCard(String label, Label valueLabel, String accentHex) {
        VBox panel = new VBox(2);
        panel.setPadding(new Insets(4, 12, 4, 12));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.02);" +
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.06);" +
                        "-fx-border-radius: 10px;" +
                        "-fx-border-width: 1px;");

        Label lblTitle = new Label(label);
        lblTitle.setStyle("-fx-font-size: 8.5px; -fx-font-weight: bold; -fx-text-fill: #71717a;");
        valueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + accentHex + ";");
        panel.getChildren().addAll(lblTitle, valueLabel);
        return panel;
    }

    public static HBox createActionsRow(Node... buttons) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(10, 0, 0, 0));
        row.getChildren().addAll(buttons);
        return row;
    }

    public static HBox createStatusRow(Label badge, Button actionButton) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 10, 0));
        actionButton.setPrefHeight(BUTTON_HEIGHT);
        actionButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(actionButton, Priority.ALWAYS);
        row.getChildren().addAll(badge, actionButton);
        return row;
    }

    public static Label createStatusBadge(String text) {
        Label badge = new Label(text);
        badge.setAlignment(Pos.CENTER);
        badge.setPrefSize(110, BUTTON_HEIGHT);
        badge.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.03);" +
                        "-fx-background-radius: 8px;" +
                        "-fx-text-fill: #a1a1aa;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-font-size: 11px;");
        return badge;
    }

    /** Creates a Button with standard height pre-set. Always use this instead of new Button(). */
    public static Button newButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(BUTTON_HEIGHT);
        btn.setMinHeight(BUTTON_HEIGHT);
        return btn;
    }

    public static void styleButton(Button btn) {
        btn.setPrefHeight(BUTTON_HEIGHT);
        btn.setMinHeight(BUTTON_HEIGHT);
    }

    public static void styleCompactButton(Button btn) {
        btn.setPrefHeight(28);
        btn.setMinHeight(28);
    }

    private static String buttonFont(double sizePx) {
        return "-fx-font-size: " + (int) sizePx + "px;";
    }

    private static String buttonPadding() {
        return "-fx-padding: 0 12 0 12;";
    }

    private static String toggleOnLabel(String title) {
        return "Tắt " + title;
    }

    private static String toggleOffLabel(String title) {
        return "Bật " + title;
    }

    public static void allowButtonGrow(Button btn) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setWrapText(false);
    }

    public static void applyButtonStyle(Button btn, String style, double fontSizePx) {
        styleButton(btn);
        btn.setStyle(style + buttonFont(fontSizePx) + buttonPadding());
    }

    public static void applyButtonStyle(Button btn, String style) {
        applyButtonStyle(btn, style, BUTTON_FONT_SIZE);
    }

    public static void applyPrimaryButton(Button btn) {
        applyButtonStyle(btn,
                "-fx-background-color: rgba(99, 102, 241, 0.08);" +
                        "-fx-text-fill: #818cf8;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(99, 102, 241, 0.4);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-cursor: hand;");
    }

    public static void applySecondaryButton(Button btn) {
        applyButtonStyle(btn,
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #a1a1aa;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-cursor: hand;");
    }

    public static void applyDangerButton(Button btn) {
        applyButtonStyle(btn,
                "-fx-background-color: rgba(239, 68, 68, 0.08);" +
                        "-fx-text-fill: #f87171;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(239, 68, 68, 0.4);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-cursor: hand;");
    }

    public static void applySuccessButton(Button btn) {
        applyButtonStyle(btn,
                "-fx-background-color: rgba(34, 197, 94, 0.08);" +
                        "-fx-text-fill: #4ade80;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(34, 197, 94, 0.4);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-cursor: hand;");
    }

    public static void applyCompactSecondaryButton(Button btn) {
        styleCompactButton(btn);
        btn.setMinWidth(72);
        btn.setText("Bật");
        applyButtonStyle(btn,
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #a1a1aa;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-background-insets: 0;" +
                        "-fx-border-width: 1px;" +
                        "-fx-cursor: hand;",
                BUTTON_FONT_SIZE_COMPACT);
    }

    public static void applyToggleButton(Button btn, String title, boolean active) {
        if (active) {
            btn.setText(toggleOnLabel(title));
            applyButtonStyle(btn,
                    "-fx-background-color: rgba(99, 102, 241, 0.12);" +
                            "-fx-text-fill: #818cf8;" +
                            "-fx-font-weight: bold;" +
                            "-fx-border-color: rgba(99, 102, 241, 0.4);" +
                            "-fx-border-radius: 8px;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-background-insets: 0;" +
                            "-fx-border-width: 1px;" +
                            "-fx-cursor: hand;");
        } else {
            btn.setText(toggleOffLabel(title));
            applySecondaryButton(btn);
        }
    }

    public static void applyCompactToggleButton(Button btn, String title, boolean active) {
        styleCompactButton(btn);
        btn.setMinWidth(72);
        if (active) {
            btn.setText("Tắt");
            applyButtonStyle(btn,
                    "-fx-background-color: rgba(99, 102, 241, 0.12);" +
                            "-fx-text-fill: #818cf8;" +
                            "-fx-font-weight: bold;" +
                            "-fx-border-color: rgba(99, 102, 241, 0.4);" +
                            "-fx-border-radius: 8px;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-background-insets: 0;" +
                            "-fx-border-width: 1px;" +
                            "-fx-cursor: hand;",
                    BUTTON_FONT_SIZE_COMPACT);
        } else {
            btn.setText("Bật");
            applyButtonStyle(btn,
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #a1a1aa;" +
                            "-fx-font-weight: bold;" +
                            "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                            "-fx-border-radius: 8px;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-background-insets: 0;" +
                            "-fx-border-width: 1px;" +
                            "-fx-cursor: hand;",
                    BUTTON_FONT_SIZE_COMPACT);
        }
    }

    public static void styleComboBox(ComboBox<?> comboBox) {
        comboBox.setPrefHeight(FIELD_HEIGHT);
        comboBox.getStyleClass().add("tss-combo");
        comboBox.setStyle(
                "-fx-background-color: #18181b;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                        "-fx-border-radius: 8px;" +
                        "-fx-border-width: 1px;" +
                        "-fx-text-fill: #f4f4f5;" +
                        "-fx-prompt-text-fill: #71717a;");
    }
}
