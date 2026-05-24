package com.leaderboard.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.InputStream;

public final class IconManager {
    private static Image appIcon = null;

    static {
        try {
            InputStream imgStream = IconManager.class.getResourceAsStream("/icons/logo.png");
            if (imgStream != null) {
                appIcon = new Image(imgStream);
            }
        } catch (Exception e) {
            System.err.println("Could not load global application icon: " + e.getMessage());
        }
    }

    private IconManager() {
        // Prevent instantiation
    }

    /**
     * Applies the global application icon to a Stage if available.
     * @param stage the target stage
     */
    public static void applyAppIcon(Stage stage) {
        if (stage != null && appIcon != null) {
            stage.getIcons().add(appIcon);
        }
    }

    /**
     * Gets the global application icon image.
     * @return the Image instance, or null if not loaded
     */
    public static Image getAppIcon() {
        return appIcon;
    }
}
