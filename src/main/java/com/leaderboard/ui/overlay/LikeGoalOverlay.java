package com.leaderboard.ui.overlay;

import com.leaderboard.util.ConfigManager;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class LikeGoalOverlay extends Stage {
    private int totalLikes = 0;
    private int targetLikes = 10000;

    private double xOffset = 0;
    private double yOffset = 0;

    private final Label lblTitle;
    private final Label lblStatus;
    private final ProgressBar progressBar;
    private final SVGPath heartPath;
    private final ScaleTransition pulseTransition;

    public LikeGoalOverlay() {
        setTitle("Mục Tiêu Tim"); // Needed for OBS Window Capture detection
        initStyle(StageStyle.TRANSPARENT);

        // Load application window icon
        try {
            java.io.InputStream imgStream = getClass().getResourceAsStream("/icons/logo.png");
            if (imgStream != null) {
                getIcons().add(new Image(imgStream));
            }
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        // Load like target from config
        this.targetLikes = ConfigManager.getConfig().getLikeTarget();

        // Root Container with transparent background
        AnchorPane root = new AnchorPane();
        root.setPrefSize(320, 95);
        root.setStyle(
            "-fx-background-color: rgba(21, 18, 27, 0.78);" + // surface-dim (78% opacity)
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.1);" +
            "-fx-border-radius: 16px;" +
            "-fx-border-width: 1.2px;"
        );

        // Enable glassmorphic shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.45));
        shadow.setRadius(12);
        shadow.setOffsetY(4);
        root.setEffect(shadow);

        // Draggable window support
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            setX(event.getScreenX() - xOffset);
            setY(event.getScreenY() - yOffset);
        });

        // 1. Draw Heart Icon (Pulsing SVG Path)
        heartPath = new SVGPath();
        heartPath.setContent("M 11 4 C 11 4 7 0 3 0 C 0 0 0 4 0 6 C 0 11 6 17 11 21 C 16 17 22 11 22 6 C 22 4 22 0 19 0 C 15 0 11 4 11 4 Z");
        
        // Hot Pink to Red Gradient fill
        LinearGradient heartGrad = new LinearGradient(
            0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#ff446e")),
            new Stop(1, Color.web("#dc143c"))
        );
        heartPath.setFill(heartGrad);

        // Positioning the heart within AnchorPane
        AnchorPane.setLeftAnchor(heartPath, 15.0);
        AnchorPane.setTopAnchor(heartPath, 14.0);

        // Add soft glowing neon shadow to the heart
        DropShadow heartGlow = new DropShadow();
        heartGlow.setColor(Color.web("#ff446e", 0.6));
        heartGlow.setRadius(8);
        heartPath.setEffect(heartGlow);

        // Animation for Heart pulsing effect (GPU accelerated)
        pulseTransition = new ScaleTransition(Duration.millis(600), heartPath);
        pulseTransition.setFromX(0.95);
        pulseTransition.setFromY(0.95);
        pulseTransition.setToX(1.15);
        pulseTransition.setToY(1.15);
        pulseTransition.setAutoReverse(true);
        pulseTransition.setCycleCount(Animation.INDEFINITE);
        pulseTransition.play();

        // 2. Goal Title Label
        lblTitle = new Label("MỤC TIÊU THẢ TIM");
        lblTitle.setStyle(
            "-fx-text-fill: #d0bcfc;" + // Light purple Dim
            "-fx-font-family: 'Segoe UI', system-ui;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;"
        );
        AnchorPane.setLeftAnchor(lblTitle, 45.0);
        AnchorPane.setTopAnchor(lblTitle, 13.0);

        // 3. Goal Status Text (e.g. 0 / 10,000)
        lblStatus = new Label("0 / 10,000");
        lblStatus.setStyle(
            "-fx-text-fill: #e7e0ed;" + // #e7e0ed
            "-fx-font-family: 'Segoe UI', system-ui;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );
        AnchorPane.setRightAnchor(lblStatus, 15.0);
        AnchorPane.setTopAnchor(lblStatus, 13.0);

        // 4. Progress Bar
        progressBar = new ProgressBar(0.0);
        progressBar.setPrefSize(290, 15);
        progressBar.setStyle(
            "-fx-box-border: transparent;" +
            "-fx-control-inner-background: rgba(33, 30, 39, 0.6);" + // surface-container
            "-fx-background-color: transparent;"
        );

        // Apply custom modern gradient CSS to ProgressBar bar
        progressBar.getStylesheets().add(getClass().getResource("/css/progressbar.css") != null ? 
            getClass().getResource("/css/progressbar.css").toExternalForm() : "");
            
        AnchorPane.setLeftAnchor(progressBar, 15.0);
        AnchorPane.setTopAnchor(progressBar, 40.0);

        // Assemble root
        root.getChildren().addAll(heartPath, lblTitle, lblStatus, progressBar);

        // Configure Scene
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        // Apply fallback custom CSS styling directly in case stylesheet not found
        progressBar.setProgress(0.0);
        updateProgressUI();
    }

    private void updateProgressUI() {
        double pct = targetLikes > 0 ? (double) totalLikes / targetLikes : 0;
        if (pct > 1.0) pct = 1.0;
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
