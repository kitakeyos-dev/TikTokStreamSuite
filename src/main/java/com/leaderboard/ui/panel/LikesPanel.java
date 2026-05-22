package com.leaderboard.ui.panel;

import com.leaderboard.ui.DashboardFrame;
import com.leaderboard.ui.component.EmojiTableCellRenderer;
import com.leaderboard.ui.component.ModernCard;
import com.leaderboard.ui.component.StyledCenterRenderer;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.FontUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class LikesPanel extends JPanel {
    private final DashboardFrame parent;
    private JTable tblLikeLog;
    private DefaultTableModel likeTableModel;
    private JTextField txtLikeTarget;
    private JLabel lblTotalLikes;
    private JLabel lblPercent;
    private JLabel lblRemaining;
    private LikeGoalProgressBar progressBar;
    private JButton btnUpdateTarget;
    private JButton btnResetLikes;
    private JButton btnToggleLikeOverlayTab4;
    private JButton btnToggleTopLikeOverlayTab4;

    public LikesPanel(DashboardFrame parent) {
        this.parent = parent;
        setLayout(new GridLayout(1, 2, 24, 0)); // 24px gutter
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        initComponents();
    }

    private void initComponents() {
        // Column 1: Config, Stats and Progress Analytics card
        ModernCard cardStats = new ModernCard("MỤC TIÊU THẢ TIM & THỐNG KÊ", null, null);

        JPanel pnlStatsLayout = new JPanel(new GridBagLayout());
        pnlStatsLayout.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Counter Title Header
        gbc.gridy = 0;
        JLabel lblLikesTitle = new JLabel("TỔNG SỐ TIM ĐÃ NHẬN TỪ LIVE CLIENT:");
        lblLikesTitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 10f));
        lblLikesTitle.setForeground(new Color(161, 161, 170));
        pnlStatsLayout.add(lblLikesTitle, gbc);

        // Huge Pill Counter Row
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 15, 0);
        JPanel pnlLikesCounter = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlLikesCounter.setOpaque(false);

        lblTotalLikes = new JLabel("0");
        lblTotalLikes.setFont(new Font("Segoe UI", Font.BOLD, 48)); // Huge bold stat text
        lblTotalLikes.setForeground(Color.WHITE);
        pnlLikesCounter.add(lblTotalLikes);
        pnlStatsLayout.add(pnlLikesCounter, gbc);

        // Goal Config Input Form
        gbc.gridy = 2;
        gbc.insets = new Insets(5, 0, 5, 0);
        JLabel lblTargetTitle = new JLabel("MỤC TIÊU THẢ TIM TIẾP THEO (LIKE GOAL):");
        lblTargetTitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(11f));
        lblTargetTitle.setForeground(new Color(161, 161, 170));
        pnlStatsLayout.add(lblTargetTitle, gbc);

        gbc.gridy = 3;
        txtLikeTarget = new JTextField(String.valueOf(ConfigManager.getConfig().getLikeTarget()));
        txtLikeTarget.setFont(FontUtil.getDashboardLabelFont());
        txtLikeTarget.putClientProperty("JTextField.showClearButton", true);
        pnlStatsLayout.add(txtLikeTarget, gbc);

        // Custom Progress Bar Component
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 0, 5, 0);
        JLabel lblProgressTitle = new JLabel("TIẾN TRÌNH HIỆN TẠI:");
        lblProgressTitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(10f));
        lblProgressTitle.setForeground(new Color(161, 161, 170));
        pnlStatsLayout.add(lblProgressTitle, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 5, 0);
        progressBar = new LikeGoalProgressBar();
        pnlStatsLayout.add(progressBar, gbc);

        // Progress Details Labels (percentage and remaining)
        gbc.gridy = 6;
        gbc.insets = new Insets(2, 0, 15, 0);
        JPanel pnlProgressLabels = new JPanel(new BorderLayout());
        pnlProgressLabels.setOpaque(false);

        lblPercent = new JLabel("0.0%");
        lblPercent.setFont(FontUtil.getDashboardLabelFont().deriveFont(11f));
        lblPercent.setForeground(new Color(37, 244, 238)); // Cyan accent text

        lblRemaining = new JLabel("Đang chờ kết nối...");
        lblRemaining.setFont(FontUtil.getDashboardLabelFont().deriveFont(11f));
        lblRemaining.setForeground(new Color(161, 161, 170));

        pnlProgressLabels.add(lblPercent, BorderLayout.WEST);
        pnlProgressLabels.add(lblRemaining, BorderLayout.EAST);
        pnlStatsLayout.add(pnlProgressLabels, gbc);

        // Goal Config Action Buttons
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 0, 0, 0);
        JPanel pnlTargetControls = new JPanel(new GridLayout(1, 3, 12, 0));
        pnlTargetControls.setOpaque(false);

        btnUpdateTarget = new JButton("Cập Nhật Mục Tiêu");
        btnUpdateTarget.putClientProperty("JButton.buttonType", "roundRect");
        btnUpdateTarget.setFont(FontUtil.getAdminButtonFont().deriveFont(Font.BOLD));
        btnUpdateTarget.setBackground(new Color(37, 244, 238)); // Ghost style secondary blue init
        btnUpdateTarget.setForeground(new Color(19, 19, 19));
        btnUpdateTarget.addActionListener(e -> updateLikeTarget());
        pnlTargetControls.add(btnUpdateTarget);

        btnToggleLikeOverlayTab4 = new JButton();
        btnToggleLikeOverlayTab4.addActionListener(e -> parent.toggleLikeOverlayWindow());
        pnlTargetControls.add(btnToggleLikeOverlayTab4);

        btnToggleTopLikeOverlayTab4 = new JButton();
        btnToggleTopLikeOverlayTab4.addActionListener(e -> parent.toggleTopLikeOverlayWindow());
        pnlTargetControls.add(btnToggleTopLikeOverlayTab4);

        pnlStatsLayout.add(pnlTargetControls, gbc);
        cardStats.add(pnlStatsLayout, BorderLayout.CENTER);

        // Column 2: Real-time Like Activity Log card
        ModernCard cardLikesLog = new ModernCard("DÒNG SỰ KIỆN THẢ TIM THỰC TỌ", null, null);

        // Like table log JTable
        String[] columns = {"Thời Gian", "TikTok ID", "Tên Hiển Thị", "Số Tim Thả"};
        likeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblLikeLog = new JTable(likeTableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(30, 30, 30) : new Color(35, 35, 38));
                } else {
                    c.setBackground(new Color(37, 244, 238, 50)); // Cyan highlight
                }
                return c;
            }
        };
        tblLikeLog.getTableHeader().setReorderingAllowed(false);

        parent.styleTable(tblLikeLog);
        tblLikeLog.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblLikeLog.getColumnModel().getColumn(0).setMaxWidth(100);
        tblLikeLog.getColumnModel().getColumn(0).setCellRenderer(new StyledCenterRenderer(false, new Color(143, 143, 152)));
        tblLikeLog.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblLikeLog.getColumnModel().getColumn(1).setCellRenderer(new StyledCenterRenderer(true, new Color(161, 161, 170)));
        tblLikeLog.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblLikeLog.getColumnModel().getColumn(2).setCellRenderer(new EmojiTableCellRenderer());
        tblLikeLog.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblLikeLog.getColumnModel().getColumn(3).setCellRenderer(new StyledCenterRenderer(true, new Color(228, 228, 231)));

        JScrollPane scrollPane = new JScrollPane(tblLikeLog);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        cardLikesLog.add(scrollPane, BorderLayout.CENTER);

        // Actions Bottom Toolbar
        JPanel pnlLikesActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlLikesActions.setOpaque(false);
        pnlLikesActions.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        btnResetLikes = new JButton("Xoá Lịch Sử");
        btnResetLikes.putClientProperty("JButton.buttonType", "roundRect");
        btnResetLikes.setFont(FontUtil.getAdminButtonFont().deriveFont(Font.BOLD));
        btnResetLikes.setBackground(new Color(254, 44, 85)); // TikTok Pink for destructive actions
        btnResetLikes.setForeground(Color.WHITE);
        btnResetLikes.addActionListener(e -> {
            likeTableModel.setRowCount(0);
            lblTotalLikes.setText("0");
            parent.resetLikesOverlay();
            updateProgress(0);
        });
        pnlLikesActions.add(btnResetLikes);

        cardLikesLog.add(pnlLikesActions, BorderLayout.SOUTH);

        add(cardStats);
        add(cardLikesLog);

        // Initialize progress bar settings
        updateProgress(0);
    }

    private void updateLikeTarget() {
        String targetStr = txtLikeTarget.getText().trim();
        if (targetStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mục tiêu tim!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int target;
        try {
            target = Integer.parseInt(targetStr.replace(",", ""));
            if (target <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Mục tiêu tim phải là một số nguyên dương!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ConfigManager.getConfig().setLikeTarget(target);
        ConfigManager.save();

        parent.updateLikeTargetOverlay(target);

        // Refresh progress visuals
        try {
            int current = Integer.parseInt(lblTotalLikes.getText().replace(",", ""));
            updateProgress(current);
        } catch (Exception ex) {
            updateProgress(0);
        }

        JOptionPane.showMessageDialog(this, "Đã cập nhật mục tiêu tim thành " + String.format("%,d", target) + "!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateProgress(int totalLikes) {
        int target = ConfigManager.getConfig().getLikeTarget();
        if (progressBar != null) {
            progressBar.setProgress(totalLikes, target);
        }
        double percent = Math.min(100.0, ((double) totalLikes / Math.max(1, target)) * 100);
        if (lblPercent != null) {
            lblPercent.setText(String.format("%.1f%%", percent));
        }
        if (lblRemaining != null) {
            if (totalLikes >= target) {
                lblRemaining.setText("Mục tiêu đã hoàn thành!");
                lblRemaining.setForeground(new Color(37, 244, 238)); // Cyan complete color
            } else {
                int remaining = Math.max(0, target - totalLikes);
                lblRemaining.setText(String.format("Còn thiếu %,d tim", remaining));
                lblRemaining.setForeground(new Color(161, 161, 170));
            }
        }
    }

    public DefaultTableModel getLikeTableModel() {
        return likeTableModel;
    }

    public JLabel getLblTotalLikes() {
        return lblTotalLikes;
    }

    public JButton getBtnToggleLikeOverlayTab4() {
        return btnToggleLikeOverlayTab4;
    }

    public JButton getBtnToggleTopLikeOverlayTab4() {
        return btnToggleTopLikeOverlayTab4;
    }

    // High-tech anti-aliased dual gradient progress bar class
    private static class LikeGoalProgressBar extends JComponent {
        private int progress = 0;
        private int target = 1000;

        public LikeGoalProgressBar() {
            setPreferredSize(new Dimension(0, 16));
            setMinimumSize(new Dimension(0, 16));
        }

        public void setProgress(int progress, int target) {
            this.progress = progress;
            this.target = Math.max(1, target);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Draw track/background (solid charcoal #0D0D0D)
            g2.setColor(new Color(13, 13, 13));
            g2.fillRoundRect(0, 0, w, h, 8, 8);

            // 2. Draw 1px subtle outline at 10% opacity
            g2.setColor(new Color(255, 255, 255, 20));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);

            // 3. Draw gradient bar (TikTok Pink #FE2C55 to Cyan #25F4EE)
            double percent = Math.min(1.0, (double) progress / target);
            int fillW = (int) (w * percent);
            if (fillW > 0) {
                // Gradient paint from TikTok Pink to Cyan
                g2.setPaint(new GradientPaint(0, 0, new Color(254, 44, 85), w, 0, new Color(37, 244, 238)));
                g2.fillRoundRect(0, 0, fillW, h, 8, 8);

                // Draw a subtle gloss overlay
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, fillW, h / 2, 8, 8);
            }

            g2.dispose();
        }
    }
}
