package com.leaderboard.ui.panel;

import com.leaderboard.ui.DashboardFrame;
import com.leaderboard.ui.component.ModernCard;
import com.leaderboard.ui.component.StatusBadge;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.FontUtil;
import com.leaderboard.util.IconUtil;
import javax.swing.*;
import java.awt.*;

public class OverviewPanel extends JPanel {
    private final DashboardFrame parent;
    private JTextField txtUsername;
    private JPasswordField txtApiKey;
    private StatusBadge statusBadge;
    private JButton btnConnect;

    private JButton btnToggleOverlay;
    private JButton btnToggleChatOverlay;
    private JButton btnToggleLikeOverlay;
    private JButton btnToggleTopLikeOverlay;

    private JCheckBox chkLeaderboardOnTop;
    private JCheckBox chkChatOnTop;
    private JCheckBox chkLikeOnTop;
    private JCheckBox chkTopLikeOnTop;

    // Dynamic system diagnostics labels
    private JLabel lblWebSocketDiag;
    private JLabel lblLatencyDiag;
    private JLabel lblSyncDiag;

    public OverviewPanel(DashboardFrame parent) {
        this.parent = parent;
        setLayout(new GridLayout(1, 2, 24, 0)); // 24px gutter
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        initComponents();
        updateDiagnostics(false, "--");
    }

    private void initComponents() {
        // Column 1: Connection configurations & Status card
        ModernCard cardConfig = new ModernCard("CẤU HÌNH KẾT NỐI", null, null);

        JPanel pnlLeftLayout = new JPanel(new GridBagLayout());
        pnlLeftLayout.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // TikTok Username field block
        gbc.gridy = 0;
        JLabel lblUser = new JLabel("TIKTOK USERNAME (TÊN KÊNH LIVE):");
        lblUser.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 10f));
        lblUser.setForeground(new Color(161, 161, 170));
        pnlLeftLayout.add(lblUser, gbc);

        gbc.gridy = 1;
        txtUsername = new JTextField(ConfigManager.getConfig().getStreamerUsername());
        txtUsername.setFont(FontUtil.getDashboardLabelFont());
        txtUsername.putClientProperty("JTextField.placeholderText", "Ví dụ: streamer_live");
        txtUsername.putClientProperty("JTextField.showClearButton", true);

        JLabel lblAt = new JLabel("  @  ");
        lblAt.setFont(FontUtil.getDashboardLabelFont());
        lblAt.setForeground(new Color(161, 161, 170));
        txtUsername.putClientProperty("JTextField.leadingComponent", lblAt); // Leading @ symbol prefix
        pnlLeftLayout.add(txtUsername, gbc);

        // API Key field block
        gbc.gridy = 2;
        JLabel lblKey = new JLabel("EULERSTREAM API KEY (TÙY CHỌN):");
        lblKey.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 10f));
        lblKey.setForeground(new Color(161, 161, 170));
        pnlLeftLayout.add(lblKey, gbc);

        gbc.gridy = 3;
        txtApiKey = new JPasswordField(ConfigManager.getConfig().getEulerstreamKey());
        txtApiKey.setFont(FontUtil.getDashboardLabelFont());
        txtApiKey.putClientProperty("JTextField.placeholderText", "Nhập API Key để livestream ổn định...");
        txtApiKey.putClientProperty("JTextField.showClearButton", true);
        txtApiKey.putClientProperty("JPasswordField.showRevealButton", true);

        // API key field — SVG key icon as leading component
        JLabel lblKeyIcon = IconUtil.iconLabel("ic_key", 14, new Color(100, 100, 110));
        lblKeyIcon.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));
        txtApiKey.putClientProperty("JTextField.leadingComponent", lblKeyIcon);
        pnlLeftLayout.add(txtApiKey, gbc);

        // Status & Action row block
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 0, 15, 0);
        JPanel pnlControls = new JPanel(new BorderLayout(15, 0));
        pnlControls.setOpaque(false);

        statusBadge = new StatusBadge();
        pnlControls.add(statusBadge, BorderLayout.WEST);

        btnConnect = new JButton("Kết nối LIVE");
        btnConnect.putClientProperty("JButton.buttonType", "roundRect");
        btnConnect.setFont(FontUtil.getAdminButtonFont().deriveFont(Font.BOLD));
        btnConnect.setBackground(new Color(37, 244, 238)); // Cyan connected color initially
        btnConnect.setForeground(new Color(19, 19, 19));
        btnConnect.addActionListener(e -> parent.toggleConnection());
        pnlControls.add(btnConnect, BorderLayout.CENTER);
        pnlLeftLayout.add(pnlControls, gbc);

        // Divider
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 0, 5, 0);
        JPanel pnlDivider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 255, 255, 20));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        pnlDivider.setPreferredSize(new Dimension(0, 1));
        pnlDivider.setOpaque(false);
        pnlLeftLayout.add(pnlDivider, gbc);

        // Status Diagnostics Sub-panel
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 0, 5, 0);
        JLabel lblDiagTitle = new JLabel("TRẠNG THÁI HỆ THỐNG / DIAGNOSTICS:");
        lblDiagTitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 10f));
        lblDiagTitle.setForeground(new Color(161, 161, 170));
        pnlLeftLayout.add(lblDiagTitle, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel pnlDiagnostics = new JPanel(new GridLayout(3, 1, 0, 10));
        pnlDiagnostics.setOpaque(false);
        pnlDiagnostics.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        lblWebSocketDiag = createDiagRow(pnlDiagnostics, "WebSocket", "CHƯA KẾT NỐI", new Color(254, 44, 85));
        lblLatencyDiag = createDiagRow(pnlDiagnostics, "Độ trễ kết nối (Latency)", "--", Color.WHITE);
        lblSyncDiag = createDiagRow(pnlDiagnostics, "Đồng bộ mắt xem (Viewer Sync)", "INACTIVE",
                new Color(161, 161, 170));

        pnlLeftLayout.add(pnlDiagnostics, gbc);
        cardConfig.add(pnlLeftLayout, BorderLayout.CENTER);

        // Column 2: Quick OBS Overlays toggles card (Bento Widget Layout)
        ModernCard cardWidgets = new ModernCard("ĐIỀU KHIỂN WIDGETS OBS", null, null);

        JPanel pnlWidgets = new JPanel(new GridLayout(4, 1, 0, 12));
        pnlWidgets.setOpaque(false);
        pnlWidgets.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // Bento 1: Rankings Overlay
        btnToggleOverlay = new JButton();
        btnToggleOverlay.addActionListener(e -> parent.toggleOverlayWindow());
        chkLeaderboardOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayLeaderboardOnTop());
        pnlWidgets.add(createWidgetBento("BẢNG XẾP HẠNG LIVE", "Hiển thị Top nhà tài trợ và quà tặng.", "ic_ranking",
                new Color(37, 244, 238), btnToggleOverlay, chkLeaderboardOnTop));

        // Bento 2: Chat Overlay
        btnToggleChatOverlay = new JButton();
        btnToggleChatOverlay.addActionListener(e -> parent.toggleChatOverlayWindow());
        chkChatOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayChatOnTop());
        pnlWidgets.add(createWidgetBento("KHUNG CHAT TRONG SUỐT", "Hiển thị dòng chat game capture trực tiếp.",
                "ic_chat", new Color(168, 85, 247), btnToggleChatOverlay, chkChatOnTop));

        // Bento 3: Like Goal Overlay
        btnToggleLikeOverlay = new JButton();
        btnToggleLikeOverlay.addActionListener(e -> parent.toggleLikeOverlayWindow());
        chkLikeOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayLikeOnTop());
        pnlWidgets.add(createWidgetBento("MỤC TIÊU THẢ TIM", "Thanh tim bay lơ lửng và đếm tim.", "ic_goal",
                new Color(254, 44, 85), btnToggleLikeOverlay, chkLikeOnTop));

        // Bento 4: Top Like Overlay
        btnToggleTopLikeOverlay = new JButton();
        btnToggleTopLikeOverlay.addActionListener(e -> parent.toggleTopLikeOverlayWindow());
        chkTopLikeOnTop = createOnTopCheckbox(ConfigManager.getConfig().isOverlayTopLikeOnTop());
        pnlWidgets.add(createWidgetBento("TOP THẢ TIM", "Bảng xếp hạng thả tim thời gian thực.", "ic_ranking",
                new Color(254, 44, 85), btnToggleTopLikeOverlay, chkTopLikeOnTop));

        cardWidgets.add(pnlWidgets, BorderLayout.CENTER);

        add(cardConfig);
        add(cardWidgets);
    }

    private JLabel createDiagRow(JPanel parent, String label, String value, Color valueColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.PLAIN, 11f));
        lblLabel.setForeground(new Color(161, 161, 170));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 11f));
        lblValue.setForeground(valueColor);

        row.add(lblLabel, BorderLayout.WEST);
        row.add(lblValue, BorderLayout.EAST);
        parent.add(row);

        return lblValue;
    }

    private JCheckBox createOnTopCheckbox(boolean initialState) {
        JCheckBox chk = new JCheckBox("Luôn trên cùng");
        chk.setSelected(initialState);
        chk.setOpaque(false);
        chk.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.PLAIN, 10f));
        chk.setForeground(new Color(161, 161, 170));
        chk.setFocusPainted(false);
        return chk;
    }

    private JPanel createWidgetBento(String title, String desc, String iconName, Color iconColor, JButton toggleBtn,
            JCheckBox onTopChk) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 12)),
                BorderFactory.createEmptyBorder(8, 5, 12, 5)));

        // SVG icon box
        final Color fIconColor = iconColor;
        JPanel pnlIconBox = new JPanel(new GridBagLayout());
        pnlIconBox.setOpaque(false);
        pnlIconBox.setPreferredSize(new Dimension(50, 50));

        JPanel pnlIconDraw = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dark glass background
                g2.setColor(new Color(28, 28, 30));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                // Left accent bar in icon color
                g2.setColor(new Color(fIconColor.getRed(), fIconColor.getGreen(), fIconColor.getBlue(), 180));
                g2.fillRoundRect(0, 0, 3, getHeight() - 1, 3, 3);
                // Glass outline
                g2.setColor(new Color(255, 255, 255, 18));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        pnlIconDraw.setOpaque(false);
        pnlIconDraw.setPreferredSize(new Dimension(44, 40));

        JLabel lblIcon = IconUtil.iconLabel(iconName, 18, iconColor);
        pnlIconDraw.add(lblIcon, BorderLayout.CENTER);
        pnlIconBox.add(pnlIconDraw);
        panel.add(pnlIconBox, BorderLayout.WEST);

        // Title, description and always-on-top checkbox
        JPanel pnlTitleDesc = new JPanel(new GridLayout(3, 1, 1, 1));
        pnlTitleDesc.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 11.5f));
        lblTitle.setForeground(new Color(244, 244, 245));

        JLabel lblDesc = new JLabel(desc);
        lblDesc.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.PLAIN, 10f));
        lblDesc.setForeground(new Color(161, 161, 170));

        pnlTitleDesc.add(lblTitle);
        pnlTitleDesc.add(lblDesc);
        pnlTitleDesc.add(onTopChk);
        panel.add(pnlTitleDesc, BorderLayout.CENTER);

        // Toggle button on the right
        toggleBtn.setPreferredSize(new Dimension(150, 32));

        JPanel pnlBtnContainer = new JPanel(new GridBagLayout());
        pnlBtnContainer.setOpaque(false);
        pnlBtnContainer.add(toggleBtn);
        panel.add(pnlBtnContainer, BorderLayout.EAST);

        return panel;
    }

    public void updateDiagnostics(boolean isConnected, String latency) {
        if (isConnected) {
            lblWebSocketDiag.setText("ĐÃ KẾT NỐI (CONNECTED)");
            lblWebSocketDiag.setForeground(new Color(37, 244, 238)); // Cyan
            lblLatencyDiag.setText(latency);
            lblLatencyDiag.setForeground(Color.WHITE);
            lblSyncDiag.setText("HOẠT ĐỘNG (ACTIVE)");
            lblSyncDiag.setForeground(new Color(37, 244, 238)); // Cyan
        } else {
            lblWebSocketDiag.setText("CHƯA KẾT NỐI (OFFLINE)");
            lblWebSocketDiag.setForeground(new Color(254, 44, 85)); // Pink
            lblLatencyDiag.setText("--");
            lblLatencyDiag.setForeground(new Color(161, 161, 170));
            lblSyncDiag.setText("TẮT (INACTIVE)");
            lblSyncDiag.setForeground(new Color(161, 161, 170));
        }
    }

    public JTextField getTxtUsername() {
        return txtUsername;
    }

    public JPasswordField getTxtApiKey() {
        return txtApiKey;
    }

    public StatusBadge getStatusBadge() {
        return statusBadge;
    }

    public JButton getBtnConnect() {
        return btnConnect;
    }

    public JButton getBtnToggleOverlay() {
        return btnToggleOverlay;
    }

    public JButton getBtnToggleChatOverlay() {
        return btnToggleChatOverlay;
    }

    public JButton getBtnToggleLikeOverlay() {
        return btnToggleLikeOverlay;
    }

    public JButton getBtnToggleTopLikeOverlay() {
        return btnToggleTopLikeOverlay;
    }

    public JCheckBox getChkLeaderboardOnTop() {
        return chkLeaderboardOnTop;
    }

    public JCheckBox getChkChatOnTop() {
        return chkChatOnTop;
    }

    public JCheckBox getChkLikeOnTop() {
        return chkLikeOnTop;
    }

    public JCheckBox getChkTopLikeOnTop() {
        return chkTopLikeOnTop;
    }

    public JLabel getLblSyncDiag() {
        return lblSyncDiag;
    }
}
