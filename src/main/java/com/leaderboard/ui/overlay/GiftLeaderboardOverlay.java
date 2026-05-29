package com.leaderboard.ui.overlay;

import com.leaderboard.model.TikTokUser;
import com.leaderboard.ui.component.AvatarView;
import com.leaderboard.ui.component.NameGroupView;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.IconManager;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * GiftLeaderboardOverlay OBS Overlay Stage displaying top 10 gifters.
 * Rebuilt using the unified TikTokUser domain entity.
 */
public class GiftLeaderboardOverlay extends Stage {
    private static final String FONT_FAMILY = "-fx-font-family: 'Segoe UI', system-ui;";

    private final VBox rowsContainer;

    // Throttle + snapshot for flicker-free updates
    private final PauseTransition updateThrottle = new PauseTransition(Duration.millis(600));
    private List<String> lastSnapshotIds = new ArrayList<>();  // ordered IDs of last render

    public GiftLeaderboardOverlay() {
        setTitle("Bảng Xếp Hạng"); // Title needed for OBS Window Capture detection
        initStyle(StageStyle.TRANSPARENT);

        IconManager.applyAppIcon(this);

        AnchorPane root = new AnchorPane();
        root.setPrefSize(360, 660);
        root.setStyle(
            "-fx-background-color: rgba(9, 9, 11, 0.75);" + // Slate-dark glassmorphism
            "-fx-background-radius: 16px;" +
            "-fx-border-color: rgba(255, 255, 255, 0.06);" +
            "-fx-border-radius: 16px;" +
            "-fx-border-width: 1px;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web("#000000", 0.35));
        shadow.setRadius(12);
        shadow.setOffsetY(3);
        root.setEffect(shadow);

        AnchorPane header = new AnchorPane();
        header.setPrefSize(358, 50);
        header.setStyle(
            "-fx-background-color: rgba(24, 24, 27, 0.4);" +
            "-fx-background-radius: 16px 16px 0 0;"
        );
        AnchorPane.setTopAnchor(header, 1.0);
        AnchorPane.setLeftAnchor(header, 1.0);
        AnchorPane.setRightAnchor(header, 1.0);

        SVGPath crownPath = new SVGPath();
        crownPath.setContent("M 20 43 L 16 27 L 22 33 L 26 23 L 30 33 L 36 27 L 32 43 Z");
        crownPath.setFill(Color.web("#fbbf24"));
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

        Region headerDivider = new Region();
        headerDivider.setPrefSize(360, 1);
        headerDivider.setStyle("-fx-background-color: rgba(255, 255, 255, 0.05);");
        AnchorPane.setTopAnchor(headerDivider, 50.0);

        rowsContainer = new VBox(6);
        rowsContainer.setPadding(new Insets(10, 15, 10, 15));

        ScrollPane scrollPane = new ScrollPane(rowsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;"
        );
        scrollPane.skinProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                javafx.scene.Node viewport = scrollPane.lookup(".viewport");
                if (viewport != null) {
                    viewport.setStyle("-fx-background-color: transparent;");
                }
            }
        });

        AnchorPane.setTopAnchor(scrollPane, 60.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 15.0);

        setupScrollbarFadeEffect(scrollPane);

        root.getChildren().addAll(header, headerDivider, scrollPane);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        com.leaderboard.util.ResizeHelper.addResizeListener(this, 280, 250, Double.MAX_VALUE, Double.MAX_VALUE);

        updateLeaderboard();
    }

    public void updateLeaderboard() {
        updateThrottle.setOnFinished(e -> Platform.runLater(this::smartUpdate));
        updateThrottle.playFromStart();
    }

    private void smartUpdate() {
        List<TikTokUser> list;
        synchronized (DataManager.class) {
            list = new ArrayList<>(DataManager.getGifters());
        }
        int limit = Math.min(list.size(), 100);

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

    private void updateRowsInPlace(List<TikTokUser> list, int limit) {
        List<javafx.scene.Node> rows = rowsContainer.getChildren();
        for (int i = 0; i < limit && i < rows.size(); i++) {
            TikTokUser g = list.get(i);
            HBox card = (HBox) rows.get(i);
            List<javafx.scene.Node> cells = card.getChildren();
            int pointsIdx = cells.size() - 2;
            if (pointsIdx >= 0 && cells.get(pointsIdx) instanceof Label) {
                ((Label) cells.get(pointsIdx)).setText(String.format("%,d", g.getGiftPoints()));
            }
        }
    }

    private void rebuildRows(List<TikTokUser> list, int limit) {
        rowsContainer.getChildren().clear();

        for (int i = 0; i < limit; i++) {
            TikTokUser g = list.get(i);
            int rank = i + 1;
            g.setRank(rank);

            HBox rowCard = createRowCard(g, rank);
            rowsContainer.getChildren().add(rowCard);
        }
    }

    private HBox createRowCard(TikTokUser g, int rank) {
        boolean isSpotlight = rank <= 3;
        
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        
        String accentHex;
        double cardHeight;
        double avatarSize = isSpotlight ? (rank == 1 ? 40 : 36) : 0;

        if (rank == 1) {
            accentHex = "#fbbf24";
            cardHeight = 56;
        } else if (rank == 2) {
            accentHex = "#cbd5e1";
            cardHeight = 50;
        } else if (rank == 3) {
            accentHex = "#d97706";
            cardHeight = 50;
        } else {
            accentHex = "#71717a";
            cardHeight = 38;
        }

        card.setPrefHeight(cardHeight);
        card.setMinHeight(cardHeight);

        if (isSpotlight) {
            card.setPadding(new Insets(5, 12, 5, 12));
            card.setStyle(
                "-fx-background-color: rgba(24, 24, 27, 0.5);" +
                "-fx-background-radius: 10px;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 1px;"
            );

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

            AvatarView avatarStack = new AvatarView(g.getAvatarUrl(), avatarSize, Color.web("#ffffff", 0.12), 1.2);

            VBox nameGroup = new VBox(1);
            HBox.setHgrow(nameGroup, Priority.ALWAYS);
            nameGroup.setAlignment(Pos.CENTER_LEFT);

            NameGroupView nickFlow = new NameGroupView(g.getNickname(), 12, Color.web("#e4e4e7"), true);
            if (g.getBadgeUrls() != null) {
                for (String badgeUrl : g.getBadgeUrls()) {
                    nickFlow.addBadge(badgeUrl, 14);
                }
            }

            Label lblUser = new Label(g.getNickname().equals(g.getUniqueId()) ? "" : "@" + g.getUniqueId());
            lblUser.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-size: 9.5px;" +
                FONT_FAMILY
            );
            
            nameGroup.getChildren().addAll(nickFlow, lblUser);

            Label lblPoints = new Label(String.format("%,d", g.getGiftPoints()));
            lblPoints.setStyle(
                "-fx-text-fill: #e4e4e7;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 12px;" +
                FONT_FAMILY
            );

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
            card.setPadding(new Insets(4, 10, 4, 10));
            card.setStyle(
                "-fx-background-color: rgba(24, 24, 27, 0.5);" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: rgba(255, 255, 255, 0.05);" +
                "-fx-border-radius: 8px;" +
                "-fx-border-width: 1px;"
            );

            Label lblRank = new Label(String.valueOf(rank));
            lblRank.setPrefWidth(16);
            lblRank.setAlignment(Pos.CENTER);
            lblRank.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                FONT_FAMILY
            );

            VBox nameGroup = new VBox(0);
            HBox.setHgrow(nameGroup, Priority.ALWAYS);
            nameGroup.setAlignment(Pos.CENTER_LEFT);

            NameGroupView nickFlow = new NameGroupView(g.getNickname(), 11, Color.web("#e4e4e7"), true);
            if (g.getBadgeUrls() != null) {
                for (String badgeUrl : g.getBadgeUrls()) {
                    nickFlow.addBadge(badgeUrl, 13);
                }
            }
            
            Label lblUser = new Label(g.getNickname().equals(g.getUniqueId()) ? "" : "@" + g.getUniqueId());
            lblUser.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-size: 9px;" +
                FONT_FAMILY
            );
            
            nameGroup.getChildren().addAll(nickFlow, lblUser);

            Label lblPoints = new Label(String.format("%,d", g.getGiftPoints()));
            lblPoints.setStyle(
                "-fx-text-fill: #71717a;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 11px;" +
                FONT_FAMILY
            );

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

    private void setupScrollbarFadeEffect(ScrollPane scrollPane) {
        scrollPane.skinProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                javafx.scene.Node sbNode = scrollPane.lookup(".scroll-bar:vertical");
                if (sbNode instanceof ScrollBar scrollBar) {
                    scrollBar.setOpacity(0.0);

                    PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), scrollBar);
                    fadeOut.setToValue(0.0);

                    delay.setOnFinished(e -> fadeOut.play());

                    scrollPane.vvalueProperty().addListener((obsVal, oldVal, newValVal) -> {
                        fadeOut.stop();
                        scrollBar.setOpacity(1.0);
                        delay.playFromStart();
                    });

                    scrollPane.setOnMouseMoved(e -> {
                        fadeOut.stop();
                        scrollBar.setOpacity(1.0);
                        delay.playFromStart();
                    });

                    scrollPane.setOnScroll(e -> {
                        fadeOut.stop();
                        scrollBar.setOpacity(1.0);
                        delay.playFromStart();
                    });
                }
            }
        });
    }
}
