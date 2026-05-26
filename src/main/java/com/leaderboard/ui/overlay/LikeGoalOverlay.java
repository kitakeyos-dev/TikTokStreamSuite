package com.leaderboard.ui.overlay;

import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.IconManager;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LikeGoalOverlay extends Stage {
    private int totalLikes = 0;
    private int targetLikes = 10000;

    private final Label lblTitle;
    private final Label lblStatus;
    private final ProgressBar progressBar;
    private final SVGPath heartPath;
    private final ScaleTransition pulseTransition;

    public LikeGoalOverlay() {
        setTitle("Mục Tiêu Tim"); // Needed for OBS Window Capture detection
        initStyle(StageStyle.TRANSPARENT);

        // Load application window icon
        IconManager.applyAppIcon(this);

        // Load like target from config
        this.targetLikes = ConfigManager.getConfig().getLikeTarget();

        // Root Container with premium glassmorphism
        AnchorPane root = new AnchorPane();
        root.setPrefSize(320, 65);
        root.setStyle(
                "-fx-background-color: rgba(9, 9, 11, 0.75);" + // Slate-dark Vercel dark base
                        "-fx-background-radius: 10px;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.06);" +
                        "-fx-border-radius: 10px;" +
                        "-fx-border-width: 1px;");

        // Drop shadow for elegant float
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.3));
        shadow.setRadius(10);
        shadow.setOffsetY(3);
        root.setEffect(shadow);

        // 1. Draw Heart Icon (Pulsing SVG Path) - Soft Crimson Rose (#f43f5e)
        heartPath = new SVGPath();
        heartPath.setContent(
                "M 11 4 C 11 4 7 0 3 0 C 0 0 0 4 0 6 C 0 11 6 17 11 21 C 16 17 22 11 22 6 C 22 4 22 0 19 0 C 15 0 11 4 11 4 Z");
        heartPath.setFill(Color.web("#f43f5e"));

        // Positioning the heart within AnchorPane
        AnchorPane.setLeftAnchor(heartPath, 15.0);
        AnchorPane.setTopAnchor(heartPath, 13.0);

        // Animation for Heart pulsing effect (GPU accelerated)
        pulseTransition = new ScaleTransition(Duration.millis(800), heartPath);
        pulseTransition.setFromX(0.95);
        pulseTransition.setFromY(0.95);
        pulseTransition.setToX(1.1);
        pulseTransition.setToY(1.1);
        pulseTransition.setAutoReverse(true);
        pulseTransition.setCycleCount(Animation.INDEFINITE);
        pulseTransition.play();

        // 2. Goal Title Label
        lblTitle = new Label("MỤC TIÊU THẢ TIM");
        lblTitle.setStyle(
                "-fx-text-fill: #e4e4e7;" +
                        "-fx-font-family: 'Segoe UI', system-ui;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;");
        AnchorPane.setLeftAnchor(lblTitle, 45.0);
        AnchorPane.setTopAnchor(lblTitle, 13.0);

        // 3. Goal Status Text (e.g. 0 / 10,000)
        lblStatus = new Label("0 / 10,000");
        lblStatus.setStyle(
                "-fx-text-fill: #a1a1aa;" +
                        "-fx-font-family: 'Segoe UI', system-ui;" +
                        "-fx-font-size: 11px;" +
                        "-fx-font-weight: bold;");
        AnchorPane.setRightAnchor(lblStatus, 15.0);
        AnchorPane.setTopAnchor(lblStatus, 13.0);

        // 4. Progress Bar (Thin & elegant 8px height)
        progressBar = new ProgressBar(0.0);
        progressBar.setPrefHeight(8);

        AnchorPane.setLeftAnchor(progressBar, 15.0);
        AnchorPane.setRightAnchor(progressBar, 15.0);
        AnchorPane.setTopAnchor(progressBar, 42.0);

        progressBar.setStyle(
                "-fx-box-border: transparent;" +
                        "-fx-control-inner-background: rgba(24, 24, 27, 0.6);" +
                        "-fx-background-color: transparent;");

        // Apply custom modern gradient CSS to ProgressBar bar
        progressBar.getStylesheets()
                .add(getClass().getResource("/css/progressbar.css") != null
                        ? getClass().getResource("/css/progressbar.css").toExternalForm()
                        : "");

        // Assemble root
        root.getChildren().addAll(heartPath, lblTitle, lblStatus, progressBar);

        // Configure Scene
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        // Allow free resizing and dragging
        com.leaderboard.util.ResizeHelper.addResizeListener(this, 200, 45, Double.MAX_VALUE, Double.MAX_VALUE);

        // Apply fallback custom CSS styling directly in case stylesheet not found
        progressBar.setProgress(0.0);
        updateProgressUI();
    }

    private void updateProgressUI() {
        double pct = targetLikes > 0 ? (double) totalLikes / targetLikes : 0;
        if (pct > 1.0)
            pct = 1.0;
        progressBar.setProgress(pct);
        lblStatus.setText(String.format("%,d / %,d", totalLikes, targetLikes));
    }

    public synchronized void setLikes(int totalLikes) {
        Platform.runLater(() -> {
            synchronized (this) {
                this.totalLikes = totalLikes;
                // Auto-scale target goals in steps of 10,000
                while (this.totalLikes >= this.targetLikes) {
                    this.targetLikes += 10000;
                }
            }
            updateProgressUI();
        });
    }

    public synchronized void setTargetLikes(int targetLikes) {
        Platform.runLater(() -> {
            synchronized (this) {
                this.targetLikes = targetLikes;
            }
            updateProgressUI();
        });
    }

    public synchronized int getTotalLikes() {
        return totalLikes;
    }

    public synchronized int getTargetLikes() {
        return targetLikes;
    }

    public void dispose() {
        pulseTransition.stop();
        close();
    }
}
