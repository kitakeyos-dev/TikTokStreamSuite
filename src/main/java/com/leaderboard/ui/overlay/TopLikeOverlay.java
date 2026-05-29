package com.leaderboard.ui.overlay;

import com.leaderboard.model.Liker;
import com.leaderboard.ui.component.AvatarView;
import com.leaderboard.ui.component.NameGroupView;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.IconManager;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TopLikeOverlay extends Stage {
    private static final String FONT_FAMILY = "-fx-font-family: 'Segoe UI', system-ui;";
    private static final String HEART_SVG = "M 11 4 C 11 4 7 0 3 0 C 0 0 0 4 0 6 C 0 11 6 17 11 21 C 16 17 22 11 22 6 C 22 4 22 0 19 0 C 15 0 11 4 11 4 Z";

    private final VBox rowsContainer;

    // Throttle + snapshot for flicker-free updates
    private final PauseTransition updateThrottle = new PauseTransition(Duration.millis(600));
    private List<String> lastSnapshotIds = new ArrayList<>();

    public TopLikeOverlay() {
        setTitle("Top Thả Tim"); // Title needed for OBS Window Capture detection
        initStyle(StageStyle.TRANSPARENT);

        // Load application window icon
        IconManager.applyAppIcon(this);

        // Main Glass Panel Container
        AnchorPane root = new AnchorPane();
        root.setPrefSize(360, 660);
        root.setStyle(
            "-fx-background-color: rgba(9, 9, 11, 0.75);" + // Slate-dark Vercel dark mode
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.06);" +
            "-fx-border-radius: 16px;" +
            "-fx-border-width: 1px;"
        );

        // Premium soft drop shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.35));
        shadow.setRadius(12);
        shadow.setOffsetY(3);
        root.setEffect(shadow);

        // 1. Header Bar
        AnchorPane header = new AnchorPane();
        header.setPrefSize(358, 50);
        header.setStyle(
            "-fx-background-color: rgba(24, 24, 27, 0.4);" +
            "-fx-background-radius: 16px 16px 0 0;"
        );
        AnchorPane.setTopAnchor(header, 1.0);
        AnchorPane.setLeftAnchor(header, 1.0);
        AnchorPane.setRightAnchor(header, 1.0);

        // SVG Heart next to title - Soft Crimson Rose (#f43f5e)
        SVGPath titleHeart = new SVGPath();
        titleHeart.setContent(HEART_SVG);
        titleHeart.setFill(Color.web("#f43f5e"));
        titleHeart.setScaleX(0.75);
        titleHeart.setScaleY(0.75);
        AnchorPane.setLeftAnchor(titleHeart, 15.0);
        AnchorPane.setTopAnchor(titleHeart, 10.0);

        Label lblTitle = new Label("TOP THẢ TIM");
        lblTitle.setStyle(
            "-fx-text-fill: #f4f4f5;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 13px;" +
            FONT_FAMILY
        );
        AnchorPane.setLeftAnchor(lblTitle, 45.0);
        AnchorPane.setTopAnchor(lblTitle, 16.0);

        Label lblLiveBadge = new Label("LIVE");
        lblLiveBadge.setAlignment(Pos.CENTER);
        lblLiveBadge.setPrefSize(45, 18);
        lblLiveBadge.setStyle(
            "-fx-background-color: rgba(99, 102, 241, 0.12);" +
            "-fx-background-radius: 6px;" +
            "-fx-text-fill: #818cf8;" +
            "-fx-font-size: 9px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: rgba(99, 102, 241, 0.3);" +
            "-fx-border-radius: 6px;" +
            "-fx-border-width: 1px;" +
            FONT_FAMILY
        );
        AnchorPane.setRightAnchor(lblLiveBadge, 15.0);
        AnchorPane.setTopAnchor(lblLiveBadge, 15.0);

        header.getChildren().addAll(titleHeart, lblTitle, lblLiveBadge);

        // Divider
        Region headerDivider = new Region();
        headerDivider.setPrefSize(360, 1);
        headerDivider.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05);");
        AnchorPane.setTopAnchor(headerDivider, 50.0);

        // 2. Rows VBox Container
        rowsContainer = new VBox(6); // Gap of 6px between cards
        rowsContainer.setPadding(new Insets(10, 15, 10, 15));
        rowsContainer.setPrefWidth(360);
        AnchorPane.setTopAnchor(rowsContainer, 60.0);
        AnchorPane.setLeftAnchor(rowsContainer, 0.0);
        AnchorPane.setRightAnchor(rowsContainer, 0.0);

        root.getChildren().addAll(header, headerDivider, rowsContainer);

        // Scene Configuration
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        // Allow free resizing and dragging
        com.leaderboard.util.ResizeHelper.addResizeListener(this, 280, 250, Double.MAX_VALUE, Double.MAX_VALUE);

        updateLeaderboard();
    }

    public void updateLeaderboard() {
        updateThrottle.setOnFinished(e -> Platform.runLater(this::smartUpdate));
        updateThrottle.playFromStart();
    }

    private void smartUpdate() {
        List<Liker> list;
        synchronized (DataManager.class) {
            list = new ArrayList<>(DataManager.getLikers());
        }
        int limit = Math.min(list.size(), 10);

        // Build current snapshot
        List<String> currentIds = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            currentIds.add(list.get(i).getUniqueId().toLowerCase());
        }

        boolean structureChanged = !currentIds.equals(lastSnapshotIds);
        if (structureChanged) {
            rebuildRows(list, limit);
            lastSnapshotIds = currentIds;
        } else {
            updateRowsInPlace(list, limit);
        }
    }

    private void updateRowsInPlace(List<Liker> list, int limit) {
        List<javafx.scene.Node> rows = rowsContainer.getChildren();
        for (int i = 0; i < limit && i < rows.size(); i++) {
            Liker l = list.get(i);
            HBox card = (HBox) rows.get(i);
            List<javafx.scene.Node> cells = card.getChildren();
            // Points label is second-to-last (before coinStack/heartStack)
            int pointsIdx = cells.size() - 2;
            if (pointsIdx >= 0 && cells.get(pointsIdx) instanceof Label) {
                ((Label) cells.get(pointsIdx)).setText(String.format("%,d", l.getLikes()));
            }
        }
    }

    private void rebuildRows(List<Liker> list, int limit) {
        rowsContainer.getChildren().clear();

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
        double avatarSize = isSpotlight ? (rank == 1 ? 40 : 36) : 0;

        if (rank == 1) {
            accentHex = "#fbbf24"; // Soft Gold
            cardHeight = 56;
        } else if (rank == 2) {
            accentHex = "#cbd5e1"; // Soft Silver
            cardHeight = 50;
        } else if (rank == 3) {
            accentHex = "#d97706"; // Soft Bronze/Amber
            cardHeight = 50;
        } else {
            accentHex = "#71717a"; // Muted Slate-gray
            cardHeight = 38;
        }

        card.setPrefHeight(cardHeight);
        card.setMinHeight(cardHeight);

        // Container custom styling
        if (isSpotlight) {
            card.setPadding(new Insets(5, 12, 5, 12));
            card.setStyle(
                "-fx-background-color: rgba(24, 24, 27, 0.5);" + // Slate-dark spotlight card
                "-fx-background-radius: 10px;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" + // Sleek, clean gray metal border
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 1px;"
            );

            // 1. Float Rank Badge Layout using a StackPane
            StackPane badgeStack = new StackPane();
            badgeStack.setPrefSize(20, 20);
            badgeStack.setMinSize(20, 20);
            
            Circle badgeBg = new Circle(10, Color.web(accentHex));
            Label lblBadgeRank = new Label(String.valueOf(rank));
            lblBadgeRank.setStyle(
                "-fx-text-fill: #09090b;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                FONT_FAMILY
            );
            badgeStack.getChildren().addAll(badgeBg, lblBadgeRank);

            // 2. Circular Image Avatar
            AvatarView avatarStack = new AvatarView(l.getAvatarUrl(), avatarSize, Color.web("#ffffff", 0.12), 1.2);

            // 3. Name Group
            VBox nameGroup = new VBox(1);
            HBox.setHgrow(nameGroup, Priority.ALWAYS);
            nameGroup.setAlignment(Pos.CENTER_LEFT);

            NameGroupView nickFlow = new NameGroupView(l.getNickname(), 12, Color.web("#e4e4e7"), true);
            if (l.getBadgeUrls() != null) {
                for (String badgeUrl : l.getBadgeUrls()) {
                    nickFlow.addBadge(badgeUrl, 14);
                }
            }

            Label lblUser = new Label(l.getNickname().equals(l.getUniqueId()) ? "" : "@" + l.getUniqueId());
            lblUser.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-size: 9.5px;" +
                FONT_FAMILY
            );
            
            nameGroup.getChildren().addAll(nickFlow, lblUser);

            // 4. Likes display
            Label lblLikes = new Label(String.format("%,d", l.getLikes()));
            lblLikes.setStyle(
                "-fx-text-fill: #e4e4e7;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                FONT_FAMILY
            );

            // Crimson Rose Heart Icon Stack
            StackPane heartIconStack = new StackPane();
            heartIconStack.setPrefSize(14, 14);
            SVGPath rowHeart = new SVGPath();
            rowHeart.setContent(HEART_SVG);
            rowHeart.setFill(Color.web("#f43f5e"));
            rowHeart.setScaleX(0.65);
            rowHeart.setScaleY(0.65);
            heartIconStack.getChildren().add(rowHeart);

            card.getChildren().addAll(badgeStack, avatarStack, nameGroup, lblLikes, heartIconStack);

        } else {
            // COMPACT CARD (Ranks 4-10)
            card.setPadding(new Insets(4, 10, 4, 10));
            card.setStyle(
                "-fx-background-color: rgba(24, 24, 27, 0.5);" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: rgba(255, 255, 255, 0.05);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;"
            );

            // 1. Rank Label
            Label lblRank = new Label(String.valueOf(rank));
            lblRank.setPrefWidth(16);
            lblRank.setAlignment(Pos.CENTER);
            lblRank.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                FONT_FAMILY
            );

            // 2. Name Group
            VBox nameGroup = new VBox(0);
            HBox.setHgrow(nameGroup, Priority.ALWAYS);
            nameGroup.setAlignment(Pos.CENTER_LEFT);

            NameGroupView nickFlow = new NameGroupView(l.getNickname(), 11, Color.web("#e4e4e7"), true);
            if (l.getBadgeUrls() != null) {
                for (String badgeUrl : l.getBadgeUrls()) {
                    nickFlow.addBadge(badgeUrl, 13);
                }
            }
            
            Label lblUser = new Label(l.getNickname().equals(l.getUniqueId()) ? "" : "@" + l.getUniqueId());
            lblUser.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-size: 9px;" +
                FONT_FAMILY
            );
            
            nameGroup.getChildren().addAll(nickFlow, lblUser);

            // 3. Likes Label
            Label lblLikes = new Label(String.format("%,d", l.getLikes()));
            lblLikes.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                FONT_FAMILY
            );

            // Smaller Crimson Heart
            StackPane heartIconStack = new StackPane();
            heartIconStack.setPrefSize(10, 10);
            SVGPath rowHeart = new SVGPath();
            rowHeart.setContent(HEART_SVG);
            rowHeart.setFill(Color.web("#f43f5e"));
            rowHeart.setScaleX(0.5);
            rowHeart.setScaleY(0.5);
            heartIconStack.getChildren().add(rowHeart);

            card.getChildren().addAll(lblRank, nameGroup, lblLikes, heartIconStack);
        }

        return card;
    }

    public void dispose() {
        close();
    }
}
