package com.leaderboard.ui.overlay;

import com.leaderboard.ui.component.AvatarView;
import com.leaderboard.ui.component.EmojiTextFlow;
import com.leaderboard.ui.component.NameGroupView;
import com.leaderboard.util.IconManager;
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

    public LiveChatOverlay() {
        setTitle("Trò Chuyện"); // Title needed for OBS Window Capture detection
        initStyle(StageStyle.TRANSPARENT);

        // Load application window icon
        IconManager.applyAppIcon(this);

        // Root Pane with Vercel glassmorphism styling
        AnchorPane root = new AnchorPane();
        root.setPrefSize(340, 500);
        root.setStyle(
                "-fx-background-color: rgba(9, 9, 11, 0.75);" + // Slate-dark base
                        "-fx-background-radius: 16px;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.06);" +
                        "-fx-border-radius: 16px;" +
                        "-fx-border-width: 1px;");

        // Glassmorphic soft shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.35));
        shadow.setRadius(12);
        shadow.setOffsetY(3);
        root.setEffect(shadow);

        // 1. Header Bar
        AnchorPane header = new AnchorPane();
        header.setPrefSize(338, 46);
        header.setStyle(
                "-fx-background-color: rgba(24, 24, 27, 0.4);" + // Semi-translucent dark slate
                        "-fx-background-radius: 16px 16px 0 0;");
        AnchorPane.setTopAnchor(header, 1.0);
        AnchorPane.setLeftAnchor(header, 1.0);
        AnchorPane.setRightAnchor(header, 1.0);

        Label lblTitle = new Label("TRÒ CHUYỆN TRỰC TIẾP");
        lblTitle.setStyle(
                "-fx-text-fill: #f4f4f5;" + // Crisp white-gray
                        "-fx-font-family: 'Segoe UI', system-ui;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;");
        AnchorPane.setLeftAnchor(lblTitle, 15.0);
        AnchorPane.setTopAnchor(lblTitle, 15.0);

        Label lblLiveBadge = new Label("CHAT");
        lblLiveBadge.setAlignment(Pos.CENTER);
        lblLiveBadge.setPrefSize(45, 18);
        lblLiveBadge.setStyle(
                "-fx-background-color: rgba(99, 102, 241, 0.12);" + // soft indigo tint
                        "-fx-background-radius: 6px;" +
                        "-fx-text-fill: #818cf8;" +
                        "-fx-font-family: 'Segoe UI', system-ui;" +
                        "-fx-font-size: 9px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: rgba(99, 102, 241, 0.3);" +
                        "-fx-border-radius: 6px;" +
                        "-fx-border-width: 1px;");
        AnchorPane.setRightAnchor(lblLiveBadge, 15.0);
        AnchorPane.setTopAnchor(lblLiveBadge, 14.0);

        header.getChildren().addAll(lblTitle, lblLiveBadge);

        // 2. Chat messages VBox container
        chatContainer = new VBox(6); // 6px gap like in Swing
        chatContainer.setPadding(new Insets(10));
        chatContainer.setPrefWidth(320);
        AnchorPane.setTopAnchor(chatContainer, 55.0);
        AnchorPane.setLeftAnchor(chatContainer, 10.0);
        AnchorPane.setRightAnchor(chatContainer, 10.0);

        root.getChildren().addAll(header, chatContainer);

        // Configure Scene
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        // Allow free resizing and dragging
        com.leaderboard.util.ResizeHelper.addResizeListener(this, 240, 250, Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public void addMessage(String uniqueId, String nickname, String comment, String avatarUrl, java.util.List<String> badgeUrls) {
        Platform.runLater(() -> {
            // Build the modern chat bubble HBox
            HBox bubble = new HBox(10);
            bubble.setPadding(new Insets(8, 12, 8, 12));
            bubble.setAlignment(Pos.CENTER_LEFT);
            bubble.setStyle(
                    "-fx-background-color: rgba(24, 24, 27, 0.5);" + // Slate-dark bubble
                            "-fx-background-radius: 10px;" +
                            "-fx-border-color: rgba(255, 255, 255, 0.05);" + // Thin outline
                            "-fx-border-radius: 10px;" +
                            "-fx-border-width: 1px;");
            bubble.setPrefHeight(52);
            bubble.setMinHeight(52);
            bubble.setMaxHeight(52);

            // Circular Avatar Container
            AvatarView avatarContainer = new AvatarView(avatarUrl, 32, Color.web("#818cf8", 0.4), 1.2);

            // Message text grouping
            VBox textContainer = new VBox(2);
            HBox.setHgrow(textContainer, Priority.ALWAYS);
            textContainer.setAlignment(Pos.CENTER_LEFT);

            // Use custom components for nickname and chat comments
            NameGroupView nameFlow = new NameGroupView(nickname, 11.5, Color.web("#818cf8"), true);
            if (badgeUrls != null) {
                for (String badgeUrl : badgeUrls) {
                    nameFlow.addBadge(badgeUrl, 13); // Cache & render badge next to nickname
                }
            }
            EmojiTextFlow commentFlow = new EmojiTextFlow(comment, 11, Color.web("#e4e4e7"), false);

            textContainer.getChildren().addAll(nameFlow, commentFlow);
            bubble.getChildren().addAll(avatarContainer, textContainer);

            // Slide & Fade-in transitions for micro-animations
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
