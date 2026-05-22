package com.leaderboard.ui.component;

import com.leaderboard.util.FontUtil;
import javax.swing.*;
import java.awt.*;

public class ModernCard extends JPanel {
    private final Color accentColor;

    public ModernCard(String title, String emoji, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        if (title != null) {
            JPanel pnlCardHeader = new JPanel(new BorderLayout(10, 0));
            pnlCardHeader.setOpaque(false);
            pnlCardHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

            JLabel lblTitle = new JLabel(title.toUpperCase());
            lblTitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 11f));
            lblTitle.setForeground(new Color(161, 161, 170));

            pnlCardHeader.add(lblTitle, BorderLayout.WEST);
            add(pnlCardHeader, BorderLayout.NORTH);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Sleek dark space container background (Level 1 Container #1E1E1E equivalent)
        g2.setColor(new Color(30, 30, 30));
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        
        // Draw gorgeous neon gradient accent bar (3px height) at the top of the card
        g2.setPaint(new GradientPaint(0, 0, new Color(254, 44, 85), getWidth(), 0, new Color(37, 244, 238)));
        g2.fillRoundRect(0, 0, getWidth() - 1, 3, 16, 16);
        g2.fillRect(0, 2, getWidth() - 1, 2); // fill curves at lower part of bar
        
        // Draw 1px subtle glass outline
        g2.setColor(new Color(255, 255, 255, 20)); // white with 8% opacity
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
        
        g2.dispose();
    }
}
