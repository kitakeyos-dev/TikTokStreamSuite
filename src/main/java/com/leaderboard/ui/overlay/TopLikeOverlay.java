package com.leaderboard.ui.overlay;

import com.leaderboard.model.Liker;
import com.leaderboard.util.DataManager;
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
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class TopLikeOverlay extends Stage {
    private static final String FONT_FAMILY = "-fx-font-family: 'Segoe UI', system-ui;";
    private static final String HEART_SVG = "M 11 4 C 11 4 7 0 3 0 C 0 0 0 4 0 6 C 0 11 6 17 11 21 C 16 17 22 11 22 6 C 22 4 22 0 19 0 C 15 0 11 4 11 4 Z";

    private final VBox rowsContainer;
    private double xOffset = 0;
    private double yOffset = 0;

    public TopLikeOverlay() {
        setTitle("Top Thả Tim"); // Title needed for OBS Window Capture detection
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

        // Main Glass Panel Container
        AnchorPane root = new AnchorPane();
        root.setPrefSize(360, 760);
        root.setStyle(
            "-fx-background-color: rgba(21, 18, 27, 0.78);" + // surface-dim (78% opacity)
            "-fx-background-radius: 24px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.1);" +
            "-fx-border-radius: 24px;" +
            "-fx-border-width: 1.2px;"
        );

        // Premium glassmorphic drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.45));
        shadow.setRadius(15);
        shadow.setOffsetY(4);
        root.setEffect(shadow);

        // Make window draggable
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
        header.setPrefSize(358, 70);
        header.setStyle(
            "-fx-background-color: rgba(33, 30, 39, 0.5);" + // bg-surface-container/50
            "-fx-background-radius: 24px 24px 0 0;"
        );
        AnchorPane.setTopAnchor(header, 1.0);
        AnchorPane.setLeftAnchor(header, 1.0);
        AnchorPane.setRightAnchor(header, 1.0);

        // SVG Heart next to title
        SVGPath titleHeart = new SVGPath();
        titleHeart.setContent(HEART_SVG);
        titleHeart.setFill(Color.web("#fe2c55")); // TikTok Pink
        titleHeart.setScaleX(0.9);
        titleHeart.setScaleY(0.9);
        AnchorPane.setLeftAnchor(titleHeart, 18.0);
        AnchorPane.setTopAnchor(titleHeart, 25.0);

        Label lblTitle = new Label("TOP THẢ TIM");
        lblTitle.setStyle(
            "-fx-text-fill: #e7e0ed;" + // #e7e0ed
            "-fx-font-weight: bold;" +
            "-fx-font-size: 15px;" +
            FONT_FAMILY
        );
        AnchorPane.setLeftAnchor(lblTitle, 48.0);
        AnchorPane.setTopAnchor(lblTitle, 22.0);

        Label lblLiveBadge = new Label("LIVE");
        lblLiveBadge.setAlignment(Pos.CENTER);
        lblLiveBadge.setPrefSize(50, 26);
        lblLiveBadge.setStyle(
            "-fx-background-color: rgba(110, 68, 255, 0.3);" +
            "-fx-background-radius: 13px;" +
            "-fx-text-fill: #e7e0ed;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            FONT_FAMILY
        );
        AnchorPane.setRightAnchor(lblLiveBadge, 20.0);
        AnchorPane.setTopAnchor(lblLiveBadge, 20.0);

        header.getChildren().addAll(titleHeart, lblTitle, lblLiveBadge);

        // Divider
        Region headerDivider = new Region();
        headerDivider.setPrefSize(360, 1.2);
        headerDivider.setStyle("-fx-background-color: rgba(73, 68, 84, 0.2);");
        AnchorPane.setTopAnchor(headerDivider, 70.0);

        // 2. Rows VBox Container
        rowsContainer = new VBox(6); // Gap of 6px between cards
        rowsContainer.setPadding(new Insets(10, 20, 10, 20));
        rowsContainer.setPrefWidth(360);
        AnchorPane.setTopAnchor(rowsContainer, 80.0);

        root.getChildren().addAll(header, headerDivider, rowsContainer);

        // Scene Configuration
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        updateLeaderboard();
    }

    public void updateLeaderboard() {
        Platform.runLater(this::rebuildRows);
    }

    private void rebuildRows() {
        rowsContainer.getChildren().clear();

        List<Liker> list = DataManager.getLikers();
        int limit = Math.min(list.size(), 10);

        for (int i = 0; i < limit; i++) {
            Liker l = list.get(i);
            int rank = i + 1;
            l.setRank(rank);

            HBox rowCard = createRowCard(l, rank);
            rowsContainer.getChildren().add(rowCard);
        }
    }

    private HBox createRowCard(Liker l, int rank) {
        boolean isSpotlight = rank <= 3;
        
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Colors & Configs
        String accentHex;
        double cardHeight;
        double avatarSize = isSpotlight ? (rank == 1 ? 46 : 40) : 0;

        if (rank == 1) {
            accentHex = "#ffd700"; // Gold
            cardHeight = 73; // (85 - border padding 12)
        } else if (rank == 2) {
            accentHex = "#c0c0c0"; // Silver
            cardHeight = 63; // (75 - border padding 12)
        } else if (rank == 3) {
            accentHex = "#cd7f32"; // Bronze
            cardHeight = 63;
        } else {
            accentHex = "#fe2c55"; // TikTok Pink
            cardHeight = 42;
        }

        card.setPrefHeight(cardHeight);
        card.setMinHeight(cardHeight);

        // Container custom styling
        if (isSpotlight) {
            card.setPadding(new Insets(6, 12, 6, 12));
            card.setStyle(
                "-fx-background-color: rgba(33, 30, 39, 0.6);" + // surface-container
                "-fx-background-radius: 16px;" +
                "-fx-border-color: " + accentHex + "99;" + // 60% opacity outline matching rank
                "-fx-border-radius: 16px;" +
                "-fx-border-width: 1.5px;"
            );

            // Add soft drop shadow glow to Ranks 1-3
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(accentHex, rank == 1 ? 0.3 : 0.2));
            glow.setRadius(8);
            card.setEffect(glow);

            // 1. Float Rank Badge Layout
            StackPane badgeStack = new StackPane();
            badgeStack.setPrefSize(20, 20);
            badgeStack.setMinSize(20, 20);
            
            Circle badgeBg = new Circle(10, Color.web(accentHex));
            Label lblBadgeRank = new Label(String.valueOf(rank));
            lblBadgeRank.setStyle(
                "-fx-text-fill: #15121b;" + // Dark background text
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                FONT_FAMILY
            );
            badgeStack.getChildren().addAll(badgeBg, lblBadgeRank);

            // 2. Circular Image Avatar
            StackPane avatarStack = new StackPane();
            avatarStack.setPrefSize(avatarSize, avatarSize);
            avatarStack.setMinSize(avatarSize, avatarSize);

            Circle clipCircle = new Circle(avatarSize / 2, avatarSize / 2, avatarSize / 2);
            ImageView avatarImg = new ImageView();
            avatarImg.setFitWidth(avatarSize);
            avatarImg.setFitHeight(avatarSize);
            avatarImg.setClip(clipCircle);

            if (l.getAvatarUrl() != null && !l.getAvatarUrl().isEmpty()) {
                Image img = new Image(l.getAvatarUrl(), avatarSize, avatarSize, true, true, true);
                avatarImg.setImage(img);
            } else {
                avatarImg.setImage(new Image(getClass().getResourceAsStream("/icons/logo.png")));
            }

            Circle avatarBorder = new Circle(avatarSize / 2, avatarSize / 2, avatarSize / 2);
            avatarBorder.setFill(Color.TRANSPARENT);
            avatarBorder.setStroke(Color.web(accentHex));
            avatarBorder.setStrokeWidth(2.0);
            avatarStack.getChildren().addAll(avatarImg, avatarBorder);

            // 3. Name Group
            VBox nameGroup = new VBox(1);
            HBox.setHgrow(nameGroup, Priority.ALWAYS);
            nameGroup.setAlignment(Pos.CENTER_LEFT);

            javafx.scene.text.TextFlow nickFlow = com.leaderboard.util.EmojiParser.createEmojiTextFlow(
                l.getNickname(), 12.5, Color.web("#e7e0ed"), javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 12.5)
            );

            Label lblUser = new Label(l.getNickname().equals(l.getUniqueId()) ? "" : "@" + l.getUniqueId());
            lblUser.setStyle(
                "-fx-text-fill: #cbc3d7;" +
                "-fx-font-size: 10px;" +
                FONT_FAMILY
            );
            
            nameGroup.getChildren().addAll(nickFlow, lblUser);

            // 4. Likes display
            Label lblLikes = new Label(String.format("%,d", l.getLikes()));
            lblLikes.setStyle(
                "-fx-text-fill: #e7e0ed;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12.5px;" +
                FONT_FAMILY
            );

            // Pink Heart Icon Stack
            StackPane heartIconStack = new StackPane();
            heartIconStack.setPrefSize(16, 16);
            SVGPath rowHeart = new SVGPath();
            rowHeart.setContent(HEART_SVG);
            rowHeart.setFill(Color.web("#fe2c55")); // TikTok Pink
            rowHeart.setScaleX(0.72);
            rowHeart.setScaleY(0.72);
            heartIconStack.getChildren().add(rowHeart);

            card.getChildren().addAll(badgeStack, avatarStack, nameGroup, lblLikes, heartIconStack);

        } else {
            // COMPACT CARD (Ranks 4-10)
            card.setPadding(new Insets(4, 10, 4, 10));
            card.setStyle(
                "-fx-background-color: rgba(33, 30, 39, 0.6);" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: rgba(73, 68, 84, 0.2);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;"
            );

            // 1. Rank Label
            Label lblRank = new Label(String.valueOf(rank));
            lblRank.setPrefWidth(16);
            lblRank.setAlignment(Pos.CENTER);
            lblRank.setStyle(
                "-fx-text-fill: #cbc3d7;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13.5px;" +
                FONT_FAMILY
            );

            // 2. Name Group
            VBox nameGroup = new VBox(0);
            HBox.setHgrow(nameGroup, Priority.ALWAYS);
            nameGroup.setAlignment(Pos.CENTER_LEFT);

            javafx.scene.text.TextFlow nickFlow = com.leaderboard.util.EmojiParser.createEmojiTextFlow(
                l.getNickname(), 12, Color.web("#e7e0ed"), javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 12)
            );
            
            Label lblUser = new Label(l.getNickname().equals(l.getUniqueId()) ? "" : "@" + l.getUniqueId());
            lblUser.setStyle(
                "-fx-text-fill: #cbc3d7;" +
                "-fx-font-size: 9.5px;" +
                FONT_FAMILY
            );
            
            nameGroup.getChildren().addAll(nickFlow, lblUser);

            // 3. Likes Label
            Label lblLikes = new Label(String.format("%,d", l.getLikes()));
            lblLikes.setStyle(
                "-fx-text-fill: #cbc3d7;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                FONT_FAMILY
            );

            // Smaller Pink Heart Icon
            StackPane heartIconStack = new StackPane();
            heartIconStack.setPrefSize(12, 12);
            SVGPath rowHeart = new SVGPath();
            rowHeart.setContent(HEART_SVG);
            rowHeart.setFill(Color.web("#fe2c55"));
            rowHeart.setScaleX(0.55);
            rowHeart.setScaleY(0.55);
            heartIconStack.getChildren().add(rowHeart);

            card.getChildren().addAll(lblRank, nameGroup, lblLikes, heartIconStack);
        }

        return card;
    }

    public void dispose() {
        close();
    }
}
