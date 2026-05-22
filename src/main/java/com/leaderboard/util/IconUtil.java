package com.leaderboard.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;

/**
 * Utility to load and tint SVG icons from the resources/icons/ directory.
 * Uses FlatSVGIcon from flatlaf-extras for crisp, HiDPI-aware rendering.
 */
public class IconUtil {

    /**
     * Load an SVG icon from resources/icons/<name>.svg and apply a custom tint color.
     *
     * @param name   Filename without extension, e.g. "ic_search"
     * @param size   Pixel size (width = height)
     * @param tint   Color to tint the icon strokes/fills (via ColorFilter)
     * @return       A tinted FlatSVGIcon ready to set on a JLabel or JButton
     */
    public static Icon load(String name, int size, Color tint) {
        try {
            FlatSVGIcon icon = new FlatSVGIcon("icons/" + name + ".svg", size, size);
            if (tint != null) {
                icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> tint));
            }
            return icon;
        } catch (Exception e) {
            // Graceful fallback: return an empty icon if SVG fails to load
            System.err.println("[IconUtil] Failed to load SVG: " + name + " — " + e.getMessage());
            return new ImageIcon();
        }
    }

    /**
     * Create a JLabel with an SVG icon, horizontally and vertically centered.
     */
    public static JLabel iconLabel(String name, int size, Color tint) {
        JLabel lbl = new JLabel(load(name, size, tint), SwingConstants.CENTER);
        lbl.setOpaque(false);
        return lbl;
    }
}
