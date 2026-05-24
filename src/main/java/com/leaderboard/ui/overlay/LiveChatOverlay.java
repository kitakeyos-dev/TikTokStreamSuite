package com.leaderboard.ui.overlay;

import com.leaderboard.model.ChatMessage;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class LiveChatOverlay extends Stage {
    private final VBox chatContainer;
    private final List<HBox> bubbleList = new ArrayList<>();
    private double xOffset = 0;
    private double yOffset = 0;

    public LiveChatOverlay() {
        setTitle("Trò Chuyện"); // Title needed for OBS Window Capture detection
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

        // Root Pane
        AnchorPane root = new AnchorPane();
        root.setPrefSize(340, 500);
        root.setStyle(
            "-fx-background-color: rgba(21, 18, 27, 0.78);" + // surface-dim (78% opacity)
            "-fx-background-radius: 20px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.1);" +
            "-fx-border-radius: 20px;" +
            "-fx-border-width: 1.2px;"
        );

        // Glassmorphic shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.45));
        shadow.setRadius(15);
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

        // 1. Header Bar
        AnchorPane header = new AnchorPane();
        header.setPrefSize(338, 50);
        header.setStyle(
            "-fx-background-color: rgba(33, 30, 39, 0.5);" + // surface-container/50
            "-fx-background-radius: 20px 20px 0 0;"
        );
        AnchorPane.setTopAnchor(header, 1.0);
        AnchorPane.setLeftAnchor(header, 1.0);
        AnchorPane.setRightAnchor(header, 1.0);

        Label lblTitle = new Label("TRÒ CHUYỆN TRỰC TIẾP");
        lblTitle.setStyle(
            "-fx-text-fill: #d0bcfc;" + // Light purple Dim
            "-fx-font-family: 'Segoe UI', system-ui;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;"
        );
        AnchorPane.setLeftAnchor(lblTitle, 15.0);
        AnchorPane.setTopAnchor(lblTitle, 15.0);

        Label lblLiveBadge = new Label("CHAT");
        lblLiveBadge.setAlignment(Pos.CENTER);
        lblLiveBadge.setPrefSize(45, 20);
        lblLiveBadge.setStyle(
            "-fx-background-color: rgba(255, 68, 110, 0.3);" +
            "-fx-background-radius: 10px;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-font-family: 'Segoe UI', system-ui;" +
            "-fx-font-size: 9px;" +
            "-fx-font-weight: bold;"
        );
        AnchorPane.setRightAnchor(lblLiveBadge, 15.0);
        AnchorPane.setTopAnchor(lblLiveBadge, 15.0);

        header.getChildren().addAll(lblTitle, lblLiveBadge);

        // 2. Chat messages VBox container
        chatContainer = new VBox(6); // 6px gap like in Swing
        chatContainer.setPadding(new Insets(10));
        chatContainer.setPrefWidth(320);
        AnchorPane.setTopAnchor(chatContainer, 60.0);
        AnchorPane.setLeftAnchor(chatContainer, 10.0);
        AnchorPane.setRightAnchor(chatContainer, 10.0);

        root.getChildren().addAll(header, chatContainer);

        // Configure Scene
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);
    }

    public void addMessage(String uniqueId, String nickname, String comment, String avatarUrl) {
        Platform.runLater(() -> {
            // Build the modern chat bubble HBox
            HBox bubble = new HBox(10);
            bubble.setPadding(new Insets(8, 12, 8, 12));
            bubble.setAlignment(Pos.CENTER_LEFT);
            bubble.setStyle(
                "-fx-background-color: rgba(33, 30, 39, 0.6);" + // surface-container (60% opacity)
                "-fx-background-radius: 12px;" +
                "-fx-border-color: rgba(73, 68, 84, 0.2);" + // outline variant
                "-fx-border-radius: 12px;" +
                "-fx-border-width: 1px;"
            );
            bubble.setPrefHeight(52);
            bubble.setMinHeight(52);
            bubble.setMaxHeight(52);

            // Circular Avatar Container
            StackPane avatarContainer = new StackPane();
            avatarContainer.setPrefSize(32, 32);
            avatarContainer.setMinSize(32, 32);
            
            Circle clipCircle = new Circle(16, 16, 16);
            
            ImageView avatarView = new ImageView();
            avatarView.setFitWidth(32);
            avatarView.setFitHeight(32);
            avatarView.setClip(clipCircle);

            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                // Background asynchronous load in JavaFX is native and thread-safe!
                Image img = new Image(avatarUrl, 32, 32, true, true, true);
                avatarView.setImage(img);
            } else {
                // Load default logo or shape
                avatarView.setImage(new Image(getClass().getResourceAsStream("/icons/logo.png")));
            }

            // Outer cyan/purple glowing border
            Circle borderCircle = new Circle(16, 16, 16);
            borderCircle.setFill(Color.TRANSPARENT);
            borderCircle.setStroke(Color.web("#d0bcfc")); // Light purple
            borderCircle.setStrokeWidth(1.5);

            avatarContainer.getChildren().addAll(avatarView, borderCircle);

            // Message text grouping
            VBox textContainer = new VBox(2);
            HBox.setHgrow(textContainer, Priority.ALWAYS);
            textContainer.setAlignment(Pos.CENTER_LEFT);

            // Use the EmojiParser to create beautiful rich TextFlows
            javafx.scene.text.TextFlow nameFlow = com.leaderboard.util.EmojiParser.createEmojiTextFlow(
                nickname, 12, Color.web("#d0bcfc"), javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 12)
            );
            javafx.scene.text.TextFlow commentFlow = com.leaderboard.util.EmojiParser.createEmojiTextFlow(
                comment, 11.5, Color.web("#e7e0ed"), javafx.scene.text.Font.font("Segoe UI", 11.5)
            );

            textContainer.getChildren().addAll(nameFlow, commentFlow);
            bubble.getChildren().addAll(avatarContainer, textContainer);

            // Slide & Fade-in transitions (GPU accelerated) for micro-animations
            bubble.setOpacity(0);
            bubble.setTranslateY(15);

            chatContainer.getChildren().add(bubble);
            bubbleList.add(bubble);

            // Prune excess messages (Keep max 7 bubbles like in Swing)
            if (bubbleList.size() > 7) {
                HBox oldest = bubbleList.remove(0);
                
                // Fade out animation for oldest bubble before removing it
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldest);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(e -> chatContainer.getChildren().remove(oldest));
                fadeOut.play();
            }

            // Animate new bubble entry
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), bubble);
            fadeIn.setToValue(1);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), bubble);
            slideIn.setToY(0);

            fadeIn.play();
            slideIn.play();
        });
    }

    public void clearChat() {
        Platform.runLater(() -> {
            chatContainer.getChildren().clear();
            bubbleList.clear();
        });
    }

    public void dispose() {
        close();
    }
}
