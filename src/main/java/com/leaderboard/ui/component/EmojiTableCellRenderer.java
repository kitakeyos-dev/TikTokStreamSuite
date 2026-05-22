package com.leaderboard.ui.component;

import com.leaderboard.util.FontUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class EmojiTableCellRenderer extends DefaultTableCellRenderer {
    private String textToDraw = "";

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        this.textToDraw = value != null ? value.toString() : "";
        
        // Clear default label text so it doesn't double-render
        setText("");
        
        // Set background/foreground
        if (!isSelected) {
            setBackground(row % 2 == 0 ? new Color(30, 30, 35) : new Color(24, 24, 27));
            setForeground(table.getForeground());
        } else {
            setBackground(new Color(99, 102, 241, 70));
            setForeground(Color.WHITE);
        }
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Paint background first
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        g2.setColor(getForeground());
        g2.setFont(getFont());
        
        // Center vertically
        FontMetrics fm = g2.getFontMetrics();
        int x = 10; // indentation from left
        int y = (getHeight() + fm.getAscent() - fm.getLeading()) / 2 - 1;
        
        FontUtil.drawStringWithEmoji(g2, textToDraw, x, y, getFont(), this::repaint);
        g2.dispose();
    }
}
