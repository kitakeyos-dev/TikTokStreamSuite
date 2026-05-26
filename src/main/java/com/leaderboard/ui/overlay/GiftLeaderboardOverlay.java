package com.leaderboard.ui.overlay;

import com.leaderboard.model.Gifter;
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

public class GiftLeaderboardOverlay extends Stage {
    private static final String FONT_FAMILY = "-fx-font-family: 'Segoe UI', system-ui;";

    private final VBox rowsContainer;

    // Throttle + snapshot for flicker-free updates
    private final PauseTransition updateThrottle = new PauseTransition(Duration.millis(600));
    private List<String> lastSnapshotIds = new ArrayList<>();  // ordered IDs of last render

    public GiftLeaderboardOverlay() {
        setTitle("Bảng Xếp Hạng"); // Title needed for OBS Window Capture detection
        initStyle(StageStyle.TRANSPARENT);

        // Load application window icon
        IconManager.applyAppIcon(this);

        // Main Glass Panel Container
        AnchorPane root = new AnchorPane();
        root.setPrefSize(360, 660); // Adjusted height to look more compact
        root.setStyle(
            "-fx-background-color: rgba(9, 9, 11, 0.75);" + // Slate-dark glassmorphism
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

        // Crown Path
        SVGPath crownPath = new SVGPath();
        crownPath.setContent("M 20 43 L 16 27 L 22 33 L 26 23 L 30 33 L 36 27 L 32 43 Z");
        crownPath.setFill(Color.web("#fbbf24")); // Soft Gold Color
        AnchorPane.setLeftAnchor(crownPath, 15.0);
        AnchorPane.setTopAnchor(crownPath, 5.0);

        Label lblTitle = new Label("BẢNG XẾP HẠNG");
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

        header.getChildren().addAll(crownPath, lblTitle, lblLiveBadge);

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
        // Throttle: collapse rapid events into one update per 600ms
        updateThrottle.setOnFinished(e -> Platform.runLater(this::smartUpdate));
        updateThrottle.playFromStart();
    }

    private void smartUpdate() {
        List<Gifter> list;
        synchronized (DataManager.class) {
            list = new ArrayList<>(DataManager.getGifters());
        }
        int limit = Math.min(list.size(), 10);

        // Build current snapshot IDs
        List<String> currentIds = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            currentIds.add(list.get(i).getUniqueId().toLowerCase());
        }

        // Check if structure changed (different people or different order)
        boolean structureChanged = !currentIds.equals(lastSnapshotIds);

        if (structureChanged) {
            // Full rebuild only when top-10 set/order changes
            rebuildRows(list, limit);
            lastSnapshotIds = currentIds;
        } else {
            // Just update labels in-place — no flicker at all
            updateRowsInPlace(list, limit);
        }
    }

    private void updateRowsInPlace(List<Gifter> list, int limit) {
        // rowsContainer children are HBox row cards
        // Each spotlight card (rank 1-3) has points label at index 3, compact cards at index 2
        List<javafx.scene.Node> rows = rowsContainer.getChildren();
        for (int i = 0; i < limit && i < rows.size(); i++) {
            Gifter g = list.get(i);
            int rank = i + 1;
            HBox card = (HBox) rows.get(i);
            List<javafx.scene.Node> cells = card.getChildren();
            // Points label is last child before coinStack (second-to-last)
            int pointsIdx = cells.size() - 2;
            if (pointsIdx >= 0 && cells.get(pointsIdx) instanceof Label) {
                ((Label) cells.get(pointsIdx)).setText(String.format("%,d", g.getPoints()));
            }
        }
    }

    private void rebuildRows(List<Gifter> list, int limit) {
        rowsContainer.getChildren().clear();

        for (int i = 0; i < limit; i++) {
            Gifter g = list.get(i);
            int rank = i + 1;
            g.setRank(rank);

            HBox rowCard = createRowCard(g, rank);
            rowsContainer.getChildren().add(rowCard);
        }
    }

    private HBox createRowCard(Gifter g, int rank) {
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
                "-fx-text-fill: #09090b;" + // Dark text for contrast inside solid badge
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

            if (g.getAvatarUrl() != null && !g.getAvatarUrl().isEmpty()) {
                Image img = new Image(g.getAvatarUrl(), avatarSize, avatarSize, true, true, true);
                avatarImg.setImage(img);
            } else {
                avatarImg.setImage(IconManager.getAppIcon());
            }

            Circle avatarBorder = new Circle(avatarSize / 2, avatarSize / 2, avatarSize / 2);
            avatarBorder.setFill(Color.TRANSPARENT);
            avatarBorder.setStroke(Color.web("#ffffff", 0.12)); // Elegant, thin silver avatar border
            avatarBorder.setStrokeWidth(1.2);
            avatarStack.getChildren().addAll(avatarImg, avatarBorder);

            // 3. Name Group
            VBox nameGroup = new VBox(1);
            HBox.setHgrow(nameGroup, Priority.ALWAYS);
            nameGroup.setAlignment(Pos.CENTER_LEFT);

            javafx.scene.text.TextFlow nickFlow = com.leaderboard.util.EmojiParser.createEmojiTextFlow(
                g.getNickname(), 12, Color.web("#e4e4e7"), javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 12)
            );

            Label lblUser = new Label(g.getNickname().equals(g.getUniqueId()) ? "" : "@" + g.getUniqueId());
            lblUser.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-size: 9.5px;" +
                FONT_FAMILY
            );
            
            nameGroup.getChildren().addAll(nickFlow, lblUser);

            // 4. Points display
            Label lblPoints = new Label(String.format("%,d", g.getPoints()));
            lblPoints.setStyle(
                "-fx-text-fill: #e4e4e7;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                FONT_FAMILY
            );

            // Tiny Gold Coin Stack
            StackPane coinStack = new StackPane();
            coinStack.setPrefSize(14, 14);
            Circle coinBg = new Circle(7, Color.web("#fbbf24"));
            Circle coinBorder = new Circle(7, Color.TRANSPARENT);
            coinBorder.setStroke(Color.web("#d97706"));
            coinBorder.setStrokeWidth(1.0);
            Label lblCoinSym = new Label("$");
            lblCoinSym.setStyle(
                "-fx-text-fill: #ffffff;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 8px;" +
                FONT_FAMILY
            );
            coinStack.getChildren().addAll(coinBg, coinBorder, lblCoinSym);

            card.getChildren().addAll(badgeStack, avatarStack, nameGroup, lblPoints, coinStack);

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

            javafx.scene.text.TextFlow nickFlow = com.leaderboard.util.EmojiParser.createEmojiTextFlow(
                g.getNickname(), 11, Color.web("#e4e4e7"), javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 11)
            );
            
            Label lblUser = new Label(g.getNickname().equals(g.getUniqueId()) ? "" : "@" + g.getUniqueId());
            lblUser.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-size: 9px;" +
                FONT_FAMILY
            );
            
            nameGroup.getChildren().addAll(nickFlow, lblUser);

            // 3. Points Label
            Label lblPoints = new Label(String.format("%,d", g.getPoints()));
            lblPoints.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                FONT_FAMILY
            );

            // Smaller Gold Coin
            StackPane coinStack = new StackPane();
            coinStack.setPrefSize(10, 10);
            Circle coinBg = new Circle(5, Color.web("#fbbf24"));
            Circle coinBorder = new Circle(5, Color.TRANSPARENT);
            coinBorder.setStroke(Color.web("#d97706"));
            coinBorder.setStrokeWidth(0.8);
            Label lblCoinSym = new Label("$");
            lblCoinSym.setStyle(
                "-fx-text-fill: #ffffff;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 6px;" +
                FONT_FAMILY
            );
            coinStack.getChildren().addAll(coinBg, coinBorder, lblCoinSym);

            card.getChildren().addAll(lblRank, nameGroup, lblPoints, coinStack);
        }

        return card;
    }

    public void dispose() {
        close();
    }
}
