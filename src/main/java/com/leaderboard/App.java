package com.leaderboard;

import com.formdev.flatlaf.FlatDarkLaf;
import com.leaderboard.ui.DashboardFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.RenderingHints;

public class App {
    public static void main(String[] args) {
        // Setup FlatDarkLaf for premium dark theme look and feel
        try {
            FlatDarkLaf.setup();
            
            // Customize FlatLaf configurations to fit modern round design patterns
            // Customize FlatLaf configurations to fit modern round design patterns
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.track", new java.awt.Color(24, 24, 27));
            UIManager.put("ScrollBar.thumb", new java.awt.Color(53, 53, 52));
            UIManager.put("ScrollBar.thumbHover", new java.awt.Color(254, 44, 85, 150));
            
            // TabbedPane customization to match high-tech theme
            UIManager.put("TabbedPane.selectedBackground", new java.awt.Color(24, 24, 27, 0));
            UIManager.put("TabbedPane.underlineColor", new java.awt.Color(254, 44, 85)); // TikTok Pink accent selection!
            UIManager.put("TabbedPane.selectedForeground", new java.awt.Color(254, 44, 85));
            UIManager.put("TabbedPane.hoverColor", new java.awt.Color(37, 244, 238, 20)); // Cyan hover
            UIManager.put("TabbedPane.focusColor", new java.awt.Color(0, 0, 0, 0));
            
            // Focus customization
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.focusedBorderColor", new java.awt.Color(37, 244, 238)); // Cyan focus outline!
            
            // Table customization
            UIManager.put("TableHeader.background", new java.awt.Color(30, 30, 34));
            UIManager.put("TableHeader.foreground", new java.awt.Color(161, 161, 170));
            UIManager.put("Table.selectionBackground", new java.awt.Color(37, 244, 238, 50)); // Semi-transparent Cyan
            UIManager.put("Table.selectionForeground", java.awt.Color.WHITE);
            
            // General background overrides
            UIManager.put("Panel.background", new java.awt.Color(19, 19, 19)); // deep space black bg Level 0
            
            // Enable high-quality anti-aliased font rendering throughout components
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatDarkLaf Look and Feel: " + e.getMessage());
        }

        // Fire Admin dashboard on EDT
        SwingUtilities.invokeLater(() -> {
            DashboardFrame dashboardFrame = new DashboardFrame();
            dashboardFrame.setVisible(true);
        });
    }
}
