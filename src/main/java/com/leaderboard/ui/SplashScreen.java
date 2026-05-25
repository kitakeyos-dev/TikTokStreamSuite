package com.leaderboard.ui;

import com.leaderboard.util.IconManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen extends Stage {
    private static final String FONT_FAMILY = "-fx-font-family: 'Segoe UI', system-ui;";

    private final ProgressBar progressBar;
    private final Label lblStatus;

    public SplashScreen() {
        // Transparent undecorated window style for floating look
        initStyle(StageStyle.TRANSPARENT);

        // Try applying application window icon
        IconManager.applyAppIcon(this);

        // Root Pane - Glassmorphism Slate-dark panels matching OBS overlay themes
        AnchorPane root = new AnchorPane();
        root.setPrefSize(480, 280);
        root.setStyle(
            "-fx-background-color: rgba(9, 9, 11, 0.92);" + //Slate-950 deep dark glass
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.08);" + // Sleek, micro border outline
            "-fx-border-radius: 16px;" +
            "-fx-border-width: 1px;"
        );

        // Soft dropshadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.45));
        shadow.setRadius(15);
        shadow.setOffsetY(4);
        root.setEffect(shadow);

        // 1. Tech Glowing Dots (Cyber Cyan and TikTok Pink) in top right
        HBox glowDots = new HBox(8);
        glowDots.setAlignment(Pos.CENTER_RIGHT);
        AnchorPane.setTopAnchor(glowDots, 18.0);
        AnchorPane.setRightAnchor(glowDots, 20.0);

        StackPane cyanDot = new StackPane();
        Circle cyanGlow = new Circle(7, Color.web("#25f4ee", 0.4));
        DropShadow cg = new DropShadow();
        cg.setColor(Color.web("#25f4ee"));
        cg.setRadius(5);
        cyanGlow.setEffect(cg);
        Circle cyanCore = new Circle(4, Color.web("#25f4ee"));
        cyanDot.getChildren().addAll(cyanGlow, cyanCore);

        StackPane pinkDot = new StackPane();
        Circle pinkGlow = new Circle(7, Color.web("#fe2c55", 0.4));
        DropShadow pg = new DropShadow();
        pg.setColor(Color.web("#fe2c55"));
        pg.setRadius(5);
        pinkGlow.setEffect(pg);
        Circle pinkCore = new Circle(4, Color.web("#fe2c55"));
        pinkDot.getChildren().addAll(pinkGlow, pinkCore);

        glowDots.getChildren().addAll(cyanDot, pinkDot);

        // 2. Central Branding VBox
        VBox brandingBox = new VBox(2);
        brandingBox.setAlignment(Pos.CENTER_LEFT);
        brandingBox.setPadding(new Insets(0, 30, 0, 30));
        AnchorPane.setTopAnchor(brandingBox, 65.0);
        AnchorPane.setLeftAnchor(brandingBox, 0.0);
        AnchorPane.setRightAnchor(brandingBox, 0.0);

        Label lblSubtitle = new Label("LIVE STREAM SUITE");
        lblSubtitle.setStyle(
            "-fx-text-fill: #fe2c55;" + // TikTok Pink Brand Color
            "-fx-font-weight: bold;" +
            "-fx-font-size: 10px;" +
            "-fx-letter-spacing: 2px;" +
            FONT_FAMILY
        );

        Label lblTitle = new Label("TikTok Suite");
        lblTitle.setStyle(
            "-fx-text-fill: #f4f4f5;" +
            "-fx-font-weight: 800;" + // Ultra bold
            "-fx-font-size: 32px;" +
            FONT_FAMILY
        );

        Label lblDescription = new Label("Hệ thống kết nối trực tiếp, overlay OBS và đồng bộ ngân hàng");
        lblDescription.setStyle(
            "-fx-text-fill: #71717a;" + // Muted gray
            "-fx-font-size: 12px;" +
            FONT_FAMILY
        );

        brandingBox.getChildren().addAll(lblSubtitle, lblTitle, lblDescription);

        // 3. Status Progress Elements Box
        VBox progressBox = new VBox(8);
        progressBox.setAlignment(Pos.CENTER_LEFT);
        progressBox.setPadding(new Insets(0, 30, 0, 30));
        AnchorPane.setBottomAnchor(progressBox, 40.0);
        AnchorPane.setLeftAnchor(progressBox, 0.0);
        AnchorPane.setRightAnchor(progressBox, 0.0);

        lblStatus = new Label("Đang khởi tạo các module...");
        lblStatus.setStyle(
            "-fx-text-fill: #a1a1aa;" +
            "-fx-font-size: 11px;" +
            FONT_FAMILY
        );

        progressBar = new ProgressBar(0.0);
        progressBar.setPrefHeight(4); // Ultra thin, sleek, premium progress bar
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle(
            "-fx-box-border: transparent;" +
            "-fx-control-inner-background: rgba(255,255,255,0.04);" +
            "-fx-background-color: transparent;"
        );
        // Custom styling for the progress bar bar itself
        progressBar.getStylesheets().add(getClass().getResource("/css/progressbar.css") != null ? 
            getClass().getResource("/css/progressbar.css").toExternalForm() : "");

        progressBox.getChildren().addAll(lblStatus, progressBar);

        // 4. Footer Small Tags
        Label lblVersion = new Label("v1.0.0");
        lblVersion.setStyle("-fx-text-fill: #52525b; -fx-font-size: 9px; " + FONT_FAMILY);
        AnchorPane.setBottomAnchor(lblVersion, 12.0);
        AnchorPane.setRightAnchor(lblVersion, 25.0);

        Label lblAuthor = new Label("Advanced Live Streaming Toolkit");
        lblAuthor.setStyle("-fx-text-fill: #52525b; -fx-font-size: 9px; " + FONT_FAMILY);
        AnchorPane.setBottomAnchor(lblAuthor, 12.0);
        AnchorPane.setLeftAnchor(lblAuthor, 30.0);

        root.getChildren().addAll(glowDots, brandingBox, progressBox, lblVersion, lblAuthor);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    /**
     * Updates the status message and progress percentage safely from background thread
     */
    public void setStatus(String message, double progress) {
        Platform.runLater(() -> {
            lblStatus.setText(message);
            progressBar.setProgress(progress);
        });
    }
}
