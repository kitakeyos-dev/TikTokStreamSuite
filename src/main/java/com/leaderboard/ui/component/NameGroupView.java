package com.leaderboard.ui.component;

import com.leaderboard.util.ImageCacheManager;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class NameGroupView extends EmojiTextFlow {

    public NameGroupView(String nickname, double fontSize, Color textColor, boolean bold) {
        super(nickname, fontSize, textColor, bold);
    }

    /**
     * Asynchronously loads and adds a badge before the nickname.
     *
     * @param badgeUrl  The URL of the remote badge icon.
     * @param badgeSize The display size (width & height) of the badge.
     */
    public void addBadge(String badgeUrl, double badgeSize) {
        if (badgeUrl == null || badgeUrl.isEmpty()) {
            return;
        }

        ImageView badgeView = new ImageView();
        badgeView.setFitWidth(badgeSize);
        badgeView.setFitHeight(badgeSize);
        badgeView.setTranslateY(2); // Slightly offset vertically to align nicely with text flow

        // Load using ImageCacheManager
        ImageCacheManager.loadImage(badgeUrl, badgeSize, badgeSize, img -> {
            badgeView.setImage(img);
            // Insert at index 0 to put it before nickname flow
            if (!getChildren().contains(badgeView)) {
                getChildren().add(0, badgeView);
            }
        }, null);
    }
}
