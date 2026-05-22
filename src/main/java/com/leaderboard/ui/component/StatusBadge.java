package com.leaderboard.ui.component;

import com.leaderboard.util.FontUtil;
import javax.swing.*;
import java.awt.*;

public class StatusBadge extends JPanel {
    private final JLabel lblText;
    private Color dotColor = new Color(254, 44, 85); // Default TikTok red
    
    public StatusBadge() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 8, 6));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 8)); // leave space on the left for the dot!
        
        lblText = new JLabel("CHƯA KẾT NỐI");
        lblText.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 10f));
        lblText.setForeground(new Color(254, 44, 85));
        
        setBackground(new Color(254, 44, 85, 26)); // 10% opacity bg
        add(lblText);
    }
    
    public void updateStatus(String status, Color textColor, Color bgColor) {
        String cleanStatus = status.toUpperCase().replace("● ", "").trim();
        lblText.setText(cleanStatus);
        
        // Map colors to the StreamPulse redesign
        if (cleanStatus.contains("ĐÃ KẾT NỐI") || cleanStatus.contains("CONNECTED")) {
            dotColor = new Color(37, 244, 238); // Cyan
            lblText.setForeground(dotColor);
            setBackground(new Color(37, 244, 238, 26));
        } else if (cleanStatus.contains("ĐANG")) {
            dotColor = new Color(189, 52, 254); // Purple
            lblText.setForeground(dotColor);
            setBackground(new Color(189, 52, 254, 26));
        } else {
            dotColor = new Color(254, 44, 85); // TikTok Pink/Red
            lblText.setForeground(dotColor);
            setBackground(new Color(254, 44, 85, 26));
        }
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw container pill
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight() - 1, getHeight() - 1); // Full pill shape
        
        // Draw 1px border matching dotColor at 20% opacity
        g2.setColor(new Color(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue(), 51));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight() - 1, getHeight() - 1);
        
        // Draw indicator dot on the left
        g2.setColor(dotColor);
        int dotSize = 8;
        int dotX = 10;
        int dotY = (getHeight() - dotSize) / 2;
        g2.fillOval(dotX, dotY, dotSize, dotSize);
        
        // Draw subtle dot outer glow
        g2.setColor(new Color(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue(), 100));
        g2.drawOval(dotX - 2, dotY - 2, dotSize + 4, dotSize + 4);
        
        g2.dispose();
    }
}
