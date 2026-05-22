package com.leaderboard.ui.panel;

import com.leaderboard.ui.DashboardFrame;
import com.leaderboard.ui.component.EmojiTableCellRenderer;
import com.leaderboard.ui.component.ModernCard;
import com.leaderboard.ui.component.StyledCenterRenderer;
import com.leaderboard.model.Gifter;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.FontUtil;
import com.leaderboard.util.IconUtil;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LeaderboardPanel extends JPanel {
    private final DashboardFrame parent;
    private JTable tblGifters;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;
    private JLabel lblTotalDiamondsVal;
    private JLabel lblActiveDonorsVal;
    
    private JButton btnDeleteSelected;
    private JButton btnResetAll;
    private JButton btnAddManual;
    private JButton btnToggleOverlayTab2;

    public LeaderboardPanel(DashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        initComponents();
        refreshTableData();
    }

    private void initComponents() {
        // Main Bento Leaderboard card
        ModernCard cardLeaderboard = new ModernCard(null, null, null);

        // --- SUBHEADER PANEL ---
        btnToggleOverlayTab2 = new JButton();
        btnToggleOverlayTab2.addActionListener(e -> parent.toggleOverlayWindow());

        JPanel pnlCardHeader = new JPanel(new BorderLayout(15, 0));
        pnlCardHeader.setOpaque(false);
        pnlCardHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Left Side: Title & Subtitle
        JPanel pnlTitleArea = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlTitleArea.setOpaque(false);
        
        JLabel lblTitle = new JLabel("BẢNG XẾP HẠNG DONATE TÍCH LŨY");
        lblTitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 13f));
        lblTitle.setForeground(new Color(244, 244, 245));
        
        JLabel lblSubtitle = new JLabel("Quản lý danh sách người ủng hộ tích lũy và tổng số kim cương nhận được.");
        lblSubtitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.PLAIN, 10f));
        lblSubtitle.setForeground(new Color(113, 113, 122));
        
        pnlTitleArea.add(lblTitle);
        pnlTitleArea.add(lblSubtitle);
        pnlCardHeader.add(pnlTitleArea, BorderLayout.WEST);

        // Right Side: Dynamic Stats Summary & OBS Button
        JPanel pnlHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlHeaderRight.setOpaque(false);

        // Stats Box 1: Total Diamonds
        JPanel pnlStatDiamonds = createMiniStatCard("TỔNG KIM CƯƠNG", lblTotalDiamondsVal = new JLabel("0"), new Color(37, 244, 238));
        pnlHeaderRight.add(pnlStatDiamonds);

        // Stats Box 2: Active Donors
        JPanel pnlStatDonors = createMiniStatCard("NHÀ TÀI TRỢ", lblActiveDonorsVal = new JLabel("0"), new Color(254, 44, 85));
        pnlHeaderRight.add(pnlStatDonors);

        pnlHeaderRight.add(btnToggleOverlayTab2);
        pnlCardHeader.add(pnlHeaderRight, BorderLayout.EAST);
        
        cardLeaderboard.add(pnlCardHeader, BorderLayout.NORTH);

        // --- ACTION FILTER BAR ---
        JPanel pnlActionBar = new JPanel(new BorderLayout(15, 0));
        pnlActionBar.setOpaque(false);
        pnlActionBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // Real-time search box
        txtSearch = new JTextField();
        txtSearch.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.PLAIN, 11f));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm TikTok ID hoặc Tên hiển thị...");
        txtSearch.putClientProperty("JTextField.showClearButton", true);
        JLabel lblSearchIcon = IconUtil.iconLabel("ic_search", 14, new Color(100, 100, 110));
        lblSearchIcon.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));
        txtSearch.putClientProperty("JTextField.leadingComponent", lblSearchIcon);
        pnlActionBar.add(txtSearch, BorderLayout.CENTER);

        cardLeaderboard.add(pnlActionBar, BorderLayout.CENTER);

        // --- TABLE CONTAINER ---
        String[] columns = {"Hạng", "TikTok ID", "Tên Hiển Thị", "Điểm (Kim cương)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        rowSorter = new TableRowSorter<>(tableModel);

        tblGifters = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(30, 30, 30) : new Color(35, 35, 38));
                } else {
                    c.setBackground(new Color(37, 244, 238, 50)); // High-contrast cyan selection
                }
                return c;
            }
        };
        tblGifters.setRowSorter(rowSorter);
        tblGifters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblGifters.getTableHeader().setReorderingAllowed(false);
        
        parent.styleTable(tblGifters);
        tblGifters.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblGifters.getColumnModel().getColumn(0).setMaxWidth(80);
        tblGifters.getColumnModel().getColumn(0).setCellRenderer(new StyledCenterRenderer(true, new Color(37, 244, 238))); // cyan rank numbers
        tblGifters.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblGifters.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblGifters.getColumnModel().getColumn(2).setCellRenderer(new EmojiTableCellRenderer());
        tblGifters.getColumnModel().getColumn(3).setPreferredWidth(140);
        tblGifters.getColumnModel().getColumn(3).setCellRenderer(new StyledCenterRenderer(true, new Color(254, 44, 85))); // Pink points

        // Setup real-time search document listener
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filter(); }
            
            private void filter() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2)); // Match ID or Name
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblGifters);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        
        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        pnlTableWrapper.setOpaque(false);
        pnlTableWrapper.add(pnlActionBar, BorderLayout.NORTH);
        pnlTableWrapper.add(scrollPane, BorderLayout.CENTER);
        cardLeaderboard.add(pnlTableWrapper, BorderLayout.CENTER);

        // --- ACTIONS TOOLBAR ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setOpaque(false);
        pnlActions.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        btnDeleteSelected = new JButton("Xoá Người Chọn");
        btnDeleteSelected.putClientProperty("JButton.buttonType", "roundRect");
        btnDeleteSelected.setFont(FontUtil.getAdminButtonFont().deriveFont(Font.BOLD));
        btnDeleteSelected.setBackground(new Color(255, 255, 255, 10)); // Ghost secondary action
        btnDeleteSelected.setForeground(new Color(229, 226, 225));
        btnDeleteSelected.addActionListener(e -> deleteSelectedGifter());
        pnlActions.add(btnDeleteSelected);

        btnResetAll = new JButton("Xoá Hết Bảng");
        btnResetAll.putClientProperty("JButton.buttonType", "roundRect");
        btnResetAll.setFont(FontUtil.getAdminButtonFont().deriveFont(Font.BOLD));
        btnResetAll.setBackground(new Color(254, 44, 85)); // TikTok Pink for destructive actions
        btnResetAll.setForeground(Color.WHITE);
        btnResetAll.addActionListener(e -> resetLeaderboard());
        pnlActions.add(btnResetAll);

        btnAddManual = new JButton("Cộng Điểm Thủ Công");
        btnAddManual.putClientProperty("JButton.buttonType", "roundRect");
        btnAddManual.setFont(FontUtil.getAdminButtonFont().deriveFont(Font.BOLD));
        btnAddManual.setBackground(new Color(37, 244, 238)); // Cyan for manual insertions
        btnAddManual.setForeground(new Color(19, 19, 19));
        btnAddManual.addActionListener(e -> addManualPoints());
        pnlActions.add(btnAddManual);

        cardLeaderboard.add(pnlActions, BorderLayout.SOUTH);
        add(cardLeaderboard, BorderLayout.CENTER);
    }

    private JPanel createMiniStatCard(String label, JLabel valueLabel, Color accent) {
        JPanel pnl = new JPanel(new BorderLayout(2, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 30, 30, 180));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        JLabel lblTitle = new JLabel(label);
        lblTitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 8.5f));
        lblTitle.setForeground(new Color(161, 161, 170));
        
        valueLabel.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 12f));
        valueLabel.setForeground(accent);
        
        pnl.add(lblTitle, BorderLayout.NORTH);
        pnl.add(valueLabel, BorderLayout.SOUTH);
        
        return pnl;
    }

    public void refreshTableData() {
        tableModel.setRowCount(0);
        List<Gifter> list = DataManager.getGifters();
        int rank = 1;
        int totalDiamonds = 0;
        
        for (Gifter g : list) {
            g.setRank(rank);
            tableModel.addRow(new Object[]{
                rank,
                g.getUniqueId(),
                g.getNickname(),
                g.getPoints()
            });
            totalDiamonds += g.getPoints();
            rank++;
        }

        // Update statistics cards dynamically
        if (lblTotalDiamondsVal != null) {
            lblTotalDiamondsVal.setText(String.format("%,d", totalDiamonds));
        }
        if (lblActiveDonorsVal != null) {
            lblActiveDonorsVal.setText(String.format("%,d", list.size()));
        }
    }

    private void deleteSelectedGifter() {
        int selectedRow = tblGifters.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người cần xoá!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Map selected sorter index back to model index
        int modelRow = tblGifters.convertRowIndexToModel(selectedRow);
        String uniqueId = (String) tableModel.getValueAt(modelRow, 1);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xoá người dùng @" + uniqueId + " khỏi bảng xếp hạng?", 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            synchronized (DataManager.class) {
                List<Gifter> list = DataManager.getGifters();
                list.removeIf(g -> g.getUniqueId().equalsIgnoreCase(uniqueId));
                DataManager.save();
            }
            refreshTableData();
            parent.updateLeaderboardOverlay();
        }
    }

    private void resetLeaderboard() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "CẢNH BÁO: Hành động này sẽ xoá SẠCH bảng xếp hạng hiện tại! Bạn có muốn tiếp tục?", 
            "Xác nhận xoá sạch", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            synchronized (DataManager.class) {
                DataManager.getGifters().clear();
                DataManager.save();
            }
            refreshTableData();
            parent.updateLeaderboardOverlay();
        }
    }

    private void addManualPoints() {
        String uniqueId = JOptionPane.showInputDialog(this, "Nhập TikTok ID (ví dụ: user123):", "Cộng điểm thủ công", JOptionPane.QUESTION_MESSAGE);
        if (uniqueId == null || uniqueId.trim().isEmpty()) return;
        uniqueId = uniqueId.trim();

        String nickname = JOptionPane.showInputDialog(this, "Nhập Tên Hiển Thị (không bắt buộc):", "Cộng điểm thủ công", JOptionPane.QUESTION_MESSAGE);
        if (nickname == null) nickname = "";
        nickname = nickname.trim();

        String pointsStr = JOptionPane.showInputDialog(this, "Nhập số Kim cương cần cộng (hoặc trừ nếu nhập số âm):", "Cộng điểm thủ công", JOptionPane.QUESTION_MESSAGE);
        if (pointsStr == null || pointsStr.trim().isEmpty()) return;
        
        int points;
        try {
            points = Integer.parseInt(pointsStr.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số kim cương không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final String finalUniqueId = uniqueId;
        final String finalNickname = nickname.isEmpty() ? uniqueId : nickname;
        final int finalPoints = points;

        synchronized (DataManager.class) {
            List<Gifter> list = DataManager.getGifters();
            Optional<Gifter> existing = list.stream()
                    .filter(g -> g.getUniqueId().equalsIgnoreCase(finalUniqueId))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().addPoints(finalPoints);
                if (!nickname.isEmpty()) {
                    existing.get().setNickname(finalNickname);
                }
            } else {
                list.add(new Gifter(finalUniqueId, finalNickname, null, Math.max(0, finalPoints)));
            }

            Collections.sort(list);
            DataManager.save();
        }

        refreshTableData();
        parent.updateLeaderboardOverlay();
    }

    public JButton getBtnToggleOverlayTab2() {
        return btnToggleOverlayTab2;
    }
}
