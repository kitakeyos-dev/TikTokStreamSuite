package com.leaderboard.ui;

import com.leaderboard.service.TikTokConnector;
import com.leaderboard.ui.overlay.GiftLeaderboardOverlay;
import com.leaderboard.ui.overlay.LikeGoalOverlay;
import com.leaderboard.ui.overlay.LiveChatOverlay;
import com.leaderboard.ui.overlay.TopLikeOverlay;
import com.leaderboard.ui.panel.ChatPanel;
import com.leaderboard.ui.panel.LeaderboardPanel;
import com.leaderboard.ui.panel.LikesPanel;
import com.leaderboard.ui.panel.OverviewPanel;
import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.FontUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DashboardFrame extends JFrame {
    private OverviewPanel overviewPanel;
    private LeaderboardPanel leaderboardPanel;
    private ChatPanel chatPanel;
    private LikesPanel likesPanel;

    private GiftLeaderboardOverlay overlayFrame;
    private LiveChatOverlay chatOverlay;
    private LikeGoalOverlay likeOverlay;
    private TopLikeOverlay topLikeOverlay;

    private JLabel lblSubtitle;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public DashboardFrame() {
        setTitle("TikTok Live Stream Suite - Bảng Điều Khiển");
        setSize(980, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load and set application window icon
        try {
            java.net.URL imgUrl = getClass().getResource("/icons/logo.png");
            if (imgUrl != null) {
                setIconImage(new ImageIcon(imgUrl).getImage());
            }
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        // Sleek minimalist dark theme
        getContentPane().setBackground(new Color(19, 19, 19));
        setLayout(new BorderLayout());

        // Load current config and data layers
        ConfigManager.load();
        DataManager.load();

        // Build premium branding top bar
        buildHeaderBar();

        // Build the structured modular tabs UI
        initComponents();

        // Connect connection events
        setupTikTokListeners();

        // Initialize button styling states
        updateOverlayButtonStates();
    }

    private void buildHeaderBar() {
        JPanel pnlHeaderBar = new JPanel(new BorderLayout());
        pnlHeaderBar.setBackground(new Color(19, 19, 19)); // Level 0 Base #131313
        pnlHeaderBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        JPanel pnlHeaderText = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        pnlHeaderText.setOpaque(false);

        // Tech visual indicator dot
        JPanel pnlIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Solid Pink core
                g2.setColor(new Color(254, 44, 85));
                g2.fillOval(4, 4, 12, 12);

                // Cyan pulsing halo
                g2.setColor(new Color(37, 244, 238, 120));
                g2.drawOval(1, 1, 17, 17);
                g2.dispose();
            }
        };
        pnlIcon.setPreferredSize(new Dimension(20, 20));
        pnlIcon.setOpaque(false);
        pnlHeaderText.add(pnlIcon);

        JPanel pnlTitleGroup = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlTitleGroup.setOpaque(false);

        JLabel lblTitle = new JLabel("LIVE STREAM SUITE");
        lblTitle.setFont(FontUtil.getTitleFont().deriveFont(Font.BOLD, 18f));
        lblTitle.setForeground(new Color(244, 244, 245));

        lblSubtitle = new JLabel("Bảng điều khiển quản lý OBS Overlays & Kết nối Livestream");
        lblSubtitle.setFont(FontUtil.getSubtitleFont().deriveFont(11f));
        lblSubtitle.setForeground(new Color(161, 161, 170));

        pnlTitleGroup.add(lblTitle);
        pnlTitleGroup.add(lblSubtitle);
        pnlHeaderText.add(pnlTitleGroup);
        pnlHeaderBar.add(pnlHeaderText, BorderLayout.WEST);

        // Thin professional dark divider line at bottom of header
        JPanel pnlDivider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(39, 39, 42));
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        pnlDivider.setPreferredSize(new Dimension(0, 1));
        pnlDivider.setOpaque(false);
        pnlHeaderBar.add(pnlDivider, BorderLayout.SOUTH);

        add(pnlHeaderBar, BorderLayout.NORTH);
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.putClientProperty("JTabbedPane.tabType", "underline");
        tabbedPane.putClientProperty("JTabbedPane.showTabSeparators", true);
        tabbedPane.putClientProperty("JTabbedPane.tabHeight", 40);
        tabbedPane.putClientProperty("JTabbedPane.hasFullBorder", true);
        tabbedPane.putClientProperty("JTabbedPane.selectedBackground", new Color(254, 44, 85)); // TikTok Pink accent selection!
        tabbedPane.putClientProperty("JTabbedPane.underlineColor", new Color(254, 44, 85));
        tabbedPane.setFont(FontUtil.getDashboardLabelFont());

        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));

        overviewPanel = new OverviewPanel(this);
        leaderboardPanel = new LeaderboardPanel(this);
        chatPanel = new ChatPanel(this);
        likesPanel = new LikesPanel(this);

        tabbedPane.addTab("Tổng Quan", overviewPanel);
        tabbedPane.addTab("Bảng Xếp Hạng", leaderboardPanel);
        tabbedPane.addTab("Trò Chuyện", chatPanel);
        tabbedPane.addTab("Mục Tiêu Tim", likesPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Wire always-on-top checkboxes to live overlays (and persist to config)
        overviewPanel.getChkLeaderboardOnTop().addItemListener(e -> {
            boolean v = overviewPanel.getChkLeaderboardOnTop().isSelected();
            ConfigManager.getConfig().setOverlayLeaderboardOnTop(v);
            ConfigManager.save();
            if (overlayFrame != null) overlayFrame.setAlwaysOnTop(v);
        });
        overviewPanel.getChkChatOnTop().addItemListener(e -> {
            boolean v = overviewPanel.getChkChatOnTop().isSelected();
            ConfigManager.getConfig().setOverlayChatOnTop(v);
            ConfigManager.save();
            if (chatOverlay != null) chatOverlay.setAlwaysOnTop(v);
        });
        overviewPanel.getChkLikeOnTop().addItemListener(e -> {
            boolean v = overviewPanel.getChkLikeOnTop().isSelected();
            ConfigManager.getConfig().setOverlayLikeOnTop(v);
            ConfigManager.save();
            if (likeOverlay != null) likeOverlay.setAlwaysOnTop(v);
        });
        overviewPanel.getChkTopLikeOnTop().addItemListener(e -> {
            boolean v = overviewPanel.getChkTopLikeOnTop().isSelected();
            ConfigManager.getConfig().setOverlayTopLikeOnTop(v);
            ConfigManager.save();
            if (topLikeOverlay != null) topLikeOverlay.setAlwaysOnTop(v);
        });

        // Window Closing hook
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveInputSettings();
                TikTokConnector.disconnect();
                if (overlayFrame != null) overlayFrame.dispose();
                if (chatOverlay != null) chatOverlay.dispose();
                if (likeOverlay != null) likeOverlay.dispose();
                if (topLikeOverlay != null) topLikeOverlay.dispose();
            }
        });
    }

    private void setupTikTokListeners() {
        TikTokConnector.setRoomInfoListener((title, viewers, likes) -> {
            // 1. Update Subtitle in header bar if connected
            if (title != null && !title.trim().isEmpty()) {
                String username = ConfigManager.getConfig().getStreamerUsername();
                lblSubtitle.setText("Phiên LIVE: @" + username + " - \"" + title + "\"");
            }

            // 2. Update Viewer Sync diagnostic label in OverviewPanel
            overviewPanel.getLblSyncDiag().setText(String.format("HOẠT ĐỘNG (%,d người xem)", viewers));
            overviewPanel.getLblSyncDiag().setForeground(new Color(37, 244, 238)); // Cyan

            // 3. Update total likes in LikesPanel
            likesPanel.getLblTotalLikes().setText(String.format("%,d", likes));
            likesPanel.updateProgress(likes);

            // 4. Update Like Goal Overlay
            if (likeOverlay != null) {
                likeOverlay.setLikes(likes);
            }
        });

        TikTokConnector.setChatListener((uniqueId, nickname, comment, avatarUrl) -> {
            String time = LocalTime.now().format(TIME_FORMATTER);
            chatPanel.getChatTableModel().insertRow(0, new Object[]{time, uniqueId, nickname, comment});
            if (chatPanel.getChatTableModel().getRowCount() > 100) {
                chatPanel.getChatTableModel().setRowCount(100);
            }
            if (chatOverlay != null) {
                chatOverlay.addMessage(uniqueId, nickname, comment, avatarUrl);
            }
        });

        TikTokConnector.setLikeListener((uniqueId, nickname, likesSent, totalLikesVal, avatarUrl) -> {
            String time = LocalTime.now().format(TIME_FORMATTER);
            likesPanel.getLikeTableModel().insertRow(0, new Object[]{time, uniqueId, nickname, likesSent});
            if (likesPanel.getLikeTableModel().getRowCount() > 100) {
                likesPanel.getLikeTableModel().setRowCount(100);
            }

            // Update Total Likes text
            likesPanel.getLblTotalLikes().setText(String.format("%,d", totalLikesVal));

            // Update Like Goal Progress
            likesPanel.updateProgress(totalLikesVal);

            // Update Like Goal Overlay if visible
            if (likeOverlay != null) {
                likeOverlay.setLikes(totalLikesVal);
            }

            // Update Liker and save to data.json
            DataManager.addLike(uniqueId, nickname, avatarUrl, likesSent);

            // Update Top Like Overlay
            if (topLikeOverlay != null) {
                topLikeOverlay.updateLeaderboard();
            }
        });
    }

    public void toggleConnection() {
        if (TikTokConnector.isConnected()) {
            overviewPanel.getStatusBadge().updateStatus("ĐANG NGẮT KẾT NỐI...", Color.WHITE, new Color(251, 146, 60, 30));
            TikTokConnector.disconnect();
            overviewPanel.updateDiagnostics(false, "--");
            lblSubtitle.setText("Bảng điều khiển quản lý OBS Overlays & Kết nối Livestream");
        } else {
            String username = overviewPanel.getTxtUsername().getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập TikTok Username!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            saveInputSettings();
            overviewPanel.getStatusBadge().updateStatus("ĐANG KẾT NỐI...", Color.WHITE, new Color(99, 102, 241, 30));

            TikTokConnector.connect(
                    username,
                    overviewPanel.getTxtApiKey().getText().trim(),
                    () -> {
                        overviewPanel.getStatusBadge().updateStatus("ĐÃ KẾT NỐI", new Color(37, 244, 238), new Color(37, 244, 238, 26));
                        overviewPanel.getBtnConnect().setText("Ngắt kết nối LIVE");
                        overviewPanel.getBtnConnect().setBackground(new Color(254, 44, 85));
                        overviewPanel.updateDiagnostics(true, "42ms");
                    },
                    () -> {
                        overviewPanel.getStatusBadge().updateStatus("CHƯA KẾT NỐI", new Color(254, 44, 85), new Color(254, 44, 85, 26));
                        overviewPanel.getBtnConnect().setText("Kết nối LIVE");
                        overviewPanel.getBtnConnect().setBackground(new Color(37, 244, 238));
                        overviewPanel.updateDiagnostics(false, "--");
                        lblSubtitle.setText("Bảng điều khiển quản lý OBS Overlays & Kết nối Livestream");
                    },
                    errorMsg -> {
                        JOptionPane.showMessageDialog(this, "Kết nối thất bại: " + errorMsg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                        overviewPanel.getStatusBadge().updateStatus("CHƯA KẾT NỐI", new Color(254, 44, 85), new Color(254, 44, 85, 26));
                        overviewPanel.getBtnConnect().setText("Kết nối LIVE");
                        overviewPanel.getBtnConnect().setBackground(new Color(37, 244, 238));
                        overviewPanel.updateDiagnostics(false, "--");
                        lblSubtitle.setText("Bảng điều khiển quản lý OBS Overlays & Kết nối Livestream");
                    },
                    () -> {
                        leaderboardPanel.refreshTableData();
                        if (overlayFrame != null) {
                            overlayFrame.updateLeaderboard();
                        }
                    }
            );
        }
    }

    public void toggleOverlayWindow() {
        if (overlayFrame == null) {
            overlayFrame = new GiftLeaderboardOverlay();
            overlayFrame.setAlwaysOnTop(overviewPanel.getChkLeaderboardOnTop().isSelected());
            overlayFrame.setVisible(true);
        } else {
            overlayFrame.dispose();
            overlayFrame = null;
        }
        updateOverlayButtonStates();
    }

    public void toggleChatOverlayWindow() {
        if (chatOverlay == null) {
            chatOverlay = new LiveChatOverlay();
            chatOverlay.setAlwaysOnTop(overviewPanel.getChkChatOnTop().isSelected());
            chatOverlay.setVisible(true);
        } else {
            chatOverlay.dispose();
            chatOverlay = null;
        }
        updateOverlayButtonStates();
    }

    public void toggleLikeOverlayWindow() {
        if (likeOverlay == null) {
            likeOverlay = new LikeGoalOverlay();
            likeOverlay.setAlwaysOnTop(overviewPanel.getChkLikeOnTop().isSelected());
            likeOverlay.setVisible(true);
        } else {
            likeOverlay.dispose();
            likeOverlay = null;
        }
        updateOverlayButtonStates();
    }

    public void toggleTopLikeOverlayWindow() {
        if (topLikeOverlay == null) {
            topLikeOverlay = new TopLikeOverlay();
            topLikeOverlay.setAlwaysOnTop(overviewPanel.getChkTopLikeOnTop().isSelected());
            topLikeOverlay.setVisible(true);
        } else {
            topLikeOverlay.dispose();
            topLikeOverlay = null;
        }
        updateOverlayButtonStates();
    }

    public void updateOverlayButtonStates() {
        boolean isLeaderboardOpen = (overlayFrame != null);
        styleToggleButton(overviewPanel.getBtnToggleOverlay(), "", "", "Bảng Xếp Hạng", isLeaderboardOpen);
        styleToggleButton(leaderboardPanel.getBtnToggleOverlayTab2(), "", "", "Bảng Xếp Hạng", isLeaderboardOpen);

        boolean isChatOpen = (chatOverlay != null);
        styleToggleButton(overviewPanel.getBtnToggleChatOverlay(), "", "", "Khung Chat", isChatOpen);
        styleToggleButton(chatPanel.getBtnToggleChatOverlayTab3(), "", "", "Khung Chat", isChatOpen);

        boolean isLikeOpen = (likeOverlay != null);
        styleToggleButton(overviewPanel.getBtnToggleLikeOverlay(), "", "", "Mục Tiêu Tim", isLikeOpen);
        styleToggleButton(likesPanel.getBtnToggleLikeOverlayTab4(), "", "", "Mục Tiêu Tim", isLikeOpen);

        boolean isTopLikeOpen = (topLikeOverlay != null);
        styleToggleButton(overviewPanel.getBtnToggleTopLikeOverlay(), "", "", "Top Thả Tim", isTopLikeOpen);
        styleToggleButton(likesPanel.getBtnToggleTopLikeOverlayTab4(), "", "", "Top Thả Tim", isTopLikeOpen);
    }

    public void updateLeaderboardOverlay() {
        if (overlayFrame != null) {
            overlayFrame.updateLeaderboard();
        }
    }

    public void updateLikeTargetOverlay(int target) {
        if (likeOverlay != null) {
            likeOverlay.setTargetLikes(target);
        }
    }

    public void resetLikesOverlay() {
        if (likeOverlay != null) {
            likeOverlay.setLikes(0);
        }
    }

    public void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.PLAIN, 12f));
        table.setBackground(new Color(30, 30, 30)); // Blend with ModernCard background!
        table.setForeground(new Color(229, 226, 225)); // #e5e2e1 text color
        table.setSelectionBackground(new Color(37, 244, 238, 50)); // cyan selection
        table.setSelectionForeground(Color.WHITE);

        // Style header
        table.getTableHeader().setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 12f));
        table.getTableHeader().setBackground(new Color(40, 40, 42)); // High contrast dark slate header
        table.getTableHeader().setForeground(new Color(161, 161, 170));
        table.getTableHeader().setOpaque(true);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 15)));
    }

    private void styleToggleButton(JButton button, String activeEmoji, String inactiveEmoji, String title, boolean isActive) {
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setFont(FontUtil.getAdminButtonFont().deriveFont(Font.BOLD, 11f));
        button.setMargin(new Insets(8, 14, 8, 14)); // consistent padding — no setBorder override

        if (isActive) {
            button.setText("TẮT " + title.toUpperCase());
            button.setBackground(new Color(254, 44, 85)); // Solid TikTok Pink fill
            button.setForeground(Color.WHITE);
            button.putClientProperty("JButton.focusedBackground", new Color(230, 30, 65));
        } else {
            button.setText("BẬT " + title.toUpperCase());
            button.setBackground(new Color(37, 244, 238, 20)); // Very subtle cyan tint
            button.setForeground(new Color(37, 244, 238));   // Cyan text
            button.putClientProperty("JButton.focusedBackground", new Color(37, 244, 238, 40));
            // FlatLaf draws the roundRect border in the foreground color when bg is transparent-ish
        }
    }

    private void saveInputSettings() {
        ConfigManager.AppConfig config = ConfigManager.getConfig();
        config.setStreamerUsername(overviewPanel.getTxtUsername().getText().trim());
        config.setEulerstreamKey(overviewPanel.getTxtApiKey().getText().trim());
        ConfigManager.save();
    }
}
