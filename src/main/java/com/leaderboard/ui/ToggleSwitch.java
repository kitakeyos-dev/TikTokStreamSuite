package com.leaderboard.ui;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * A custom toggle switch control (iOS-style sliding toggle).
 * Use isSelected() / setSelected() to get/set state.
 * Use setOnToggle(Runnable) to react to user clicks.
 */
public class ToggleSwitch extends HBox {

    private static final double TRACK_W  = 40;
    private static final double TRACK_H  = 22;
    private static final double THUMB_R  = 9;
    private static final double PADDING  = 2;
    private static final double TRAVEL   = TRACK_W - THUMB_R * 2 - PADDING * 2;

    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final Rectangle track;
    private final Circle   thumb;
    private final Label    label;
    private Runnable       onToggle;

    // Colors
    private static final String COLOR_OFF    = "#3f3f46";
    private static final String COLOR_ON     = "#6366f1";
    private static final String COLOR_THUMB  = "#f4f4f5";

    public ToggleSwitch(String labelText) {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);

        // Track
        track = new Rectangle(TRACK_W, TRACK_H);
        track.setArcWidth(TRACK_H);
        track.setArcHeight(TRACK_H);
        track.setFill(Color.web(COLOR_OFF));

        // Thumb
        thumb = new Circle(THUMB_R, Color.web(COLOR_THUMB));
        thumb.setEffect(new javafx.scene.effect.DropShadow(4, 0, 1, Color.rgb(0, 0, 0, 0.35)));

        // Container
        StackPane trackPane = new StackPane(track, thumb);
        trackPane.setAlignment(Pos.CENTER_LEFT);
        trackPane.setPrefSize(TRACK_W, TRACK_H);
        thumb.setTranslateX(PADDING + THUMB_R - TRACK_W / 2.0 + THUMB_R);

        // Label
        label = new Label(labelText);
        label.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 11px; -fx-font-weight: bold;");

        getChildren().addAll(trackPane, label);
        setCursor(javafx.scene.Cursor.HAND);

        // Click handler
        setOnMouseClicked(e -> toggle());

        // React to programmatic changes
        selected.addListener((obs, wasOn, isOn) -> applyState(isOn, true));
    }

    public ToggleSwitch() {
        this("");
    }

    private void toggle() {
        selected.set(!selected.get());
        if (onToggle != null) onToggle.run();
    }

    private void applyState(boolean isOn, boolean animate) {
        double targetX = isOn
                ? PADDING + THUMB_R - TRACK_W / 2.0 + THUMB_R + TRAVEL
                : PADDING + THUMB_R - TRACK_W / 2.0 + THUMB_R;

        track.setFill(Color.web(isOn ? COLOR_ON : COLOR_OFF));
        label.setStyle("-fx-text-fill: " + (isOn ? "#e4e4e7" : "#a1a1aa") + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        if (animate) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), thumb);
            tt.setToX(targetX);
            tt.play();
        } else {
            thumb.setTranslateX(targetX);
        }
    }

    // ── Public API ──────────────────────────────────────────────────────────

    public boolean isSelected() { return selected.get(); }

    public void setSelected(boolean value) {
        if (selected.get() != value) {
            selected.set(value);
        } else {
            // Force visual sync even if value unchanged (e.g. initial state)
            applyState(value, false);
        }
    }

    public BooleanProperty selectedProperty() { return selected; }

    public void setOnToggle(Runnable handler) { this.onToggle = handler; }

    public void setLabelText(String text) { label.setText(text); }
}
