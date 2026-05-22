package com.leaderboard.ui.component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StyledCenterRenderer extends DefaultTableCellRenderer {
    private final boolean isBold;
    private final Color foregroundColor;

    public StyledCenterRenderer(boolean isBold, Color foregroundColor) {
        this.isBold = isBold;
        this.foregroundColor = foregroundColor;
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setFont(table.getFont().deriveFont(isBold ? Font.BOLD : Font.PLAIN));
        
        if (!isSelected) {
            setBackground(row % 2 == 0 ? new Color(30, 30, 35) : new Color(24, 24, 27));
            if (foregroundColor != null) {
                setForeground(foregroundColor);
            } else {
                setForeground(table.getForeground());
            }
        } else {
            setBackground(new Color(99, 102, 241, 70));
            setForeground(Color.WHITE);
        }
        return this;
    }
}
