package com.leaderboard.ui.component;

import com.leaderboard.util.IconManager;
import com.leaderboard.util.ImageCacheManager;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class AvatarView extends StackPane {

    public AvatarView(String avatarUrl, double size, Color borderColor, double borderWidth) {
        super();
        setPrefSize(size, size);
        setMinSize(size, size);
        setMaxSize(size, size);

        double radius = size / 2.0;

        Circle clipCircle = new Circle(radius, radius, radius);

        ImageView avatarView = new ImageView();
        avatarView.setFitWidth(size);
        avatarView.setFitHeight(size);
        avatarView.setClip(clipCircle);

        // Load avatar using the unified ImageCacheManager
        ImageCacheManager.loadImage(avatarUrl, size, size, avatarView::setImage, IconManager.getAppIcon());

        Circle borderCircle = new Circle(radius, radius, radius);
        borderCircle.setFill(Color.TRANSPARENT);
        borderCircle.setStroke(borderColor);
        borderCircle.setStrokeWidth(borderWidth);

        getChildren().addAll(avatarView, borderCircle);
    }
}
