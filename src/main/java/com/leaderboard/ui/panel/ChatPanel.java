package com.leaderboard.ui.panel;

import com.leaderboard.ui.DashboardFrame;
import com.leaderboard.ui.component.EmojiTableCellRenderer;
import com.leaderboard.ui.component.ModernCard;
import com.leaderboard.ui.component.StyledCenterRenderer;
import com.leaderboard.util.FontUtil;
import com.leaderboard.util.IconUtil;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class ChatPanel extends JPanel {
    private final DashboardFrame parent;
    private JTable tblChatLog;
    private DefaultTableModel chatTableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField txtSearch;
    private JButton btnClearChat;
    private JButton btnToggleChatOverlayTab3;

    public ChatPanel(DashboardFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 5, 15, 5));
        initComponents();
    }

    private void initComponents() {
        ModernCard cardChat = new ModernCard(null, null, null);

        // --- SUBHEADER PANEL ---
        btnToggleChatOverlayTab3 = new JButton();
        btnToggleChatOverlayTab3.addActionListener(e -> parent.toggleChatOverlayWindow());

        JPanel pnlCardHeader = new JPanel(new BorderLayout());
        pnlCardHeader.setOpaque(false);
        pnlCardHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel pnlLeft = new JPanel(new GridLayout(2, 1, 2, 2));
        pnlLeft.setOpaque(false);
        
        JLabel lblTitle = new JLabel("LỊCH SỬ TRÒ CHUYỆN TRỰC TIẾP");
        lblTitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.BOLD, 13f));
        lblTitle.setForeground(new Color(244, 244, 245));
        
        JLabel lblSubtitle = new JLabel("Xem tin nhắn thời gian thực truyền trực tiếp từ cổng kết nối Live stream.");
        lblSubtitle.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.PLAIN, 10f));
        lblSubtitle.setForeground(new Color(113, 113, 122));
        
        pnlLeft.add(lblTitle);
        pnlLeft.add(lblSubtitle);
        
        pnlCardHeader.add(pnlLeft, BorderLayout.WEST);
        pnlCardHeader.add(btnToggleChatOverlayTab3, BorderLayout.EAST);
        cardChat.add(pnlCardHeader, BorderLayout.NORTH);

        // --- SEARCH ACTION ROW ---
        JPanel pnlActionBar = new JPanel(new BorderLayout(15, 0));
        pnlActionBar.setOpaque(false);
        pnlActionBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        txtSearch = new JTextField();
        txtSearch.setFont(FontUtil.getDashboardLabelFont().deriveFont(Font.PLAIN, 11f));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo TikTok ID, Tên hiển thị hoặc Nội dung bình luận...");
        txtSearch.putClientProperty("JTextField.showClearButton", true);
        JLabel lblSearchIcon = IconUtil.iconLabel("ic_search", 14, new Color(100, 100, 110));
        lblSearchIcon.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));
        txtSearch.putClientProperty("JTextField.leadingComponent", lblSearchIcon);
        pnlActionBar.add(txtSearch, BorderLayout.CENTER);

        // --- CHAT TABLE CONTAINER ---
        String[] columns = {"Thời Gian", "TikTok ID", "Tên Hiển Thị", "Nội Dung Bình Luận"};
        chatTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        rowSorter = new TableRowSorter<>(chatTableModel);

        tblChatLog = new JTable(chatTableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(30, 30, 30) : new Color(35, 35, 38));
                } else {
                    c.setBackground(new Color(37, 244, 238, 50)); // Cyan selection highlight
                }
                return c;
            }
        };
        tblChatLog.setRowSorter(rowSorter);
        tblChatLog.getTableHeader().setReorderingAllowed(false);
        
        parent.styleTable(tblChatLog);
        tblChatLog.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblChatLog.getColumnModel().getColumn(0).setMaxWidth(100);
        tblChatLog.getColumnModel().getColumn(0).setCellRenderer(new StyledCenterRenderer(false, new Color(143, 143, 152)));
        tblChatLog.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblChatLog.getColumnModel().getColumn(1).setCellRenderer(new StyledCenterRenderer(true, new Color(161, 161, 170)));
        tblChatLog.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblChatLog.getColumnModel().getColumn(2).setCellRenderer(new EmojiTableCellRenderer());
        tblChatLog.getColumnModel().getColumn(3).setPreferredWidth(450);
        tblChatLog.getColumnModel().getColumn(3).setCellRenderer(new EmojiTableCellRenderer());

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
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 3)); // Match ID, Name or Comment
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblChatLog);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        
        JPanel pnlTableWrapper = new JPanel(new BorderLayout());
        pnlTableWrapper.setOpaque(false);
        pnlTableWrapper.add(pnlActionBar, BorderLayout.NORTH);
        pnlTableWrapper.add(scrollPane, BorderLayout.CENTER);
        cardChat.add(pnlTableWrapper, BorderLayout.CENTER);

        // --- ACTIONS TOOLBAR ---
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlActions.setOpaque(false);
        pnlActions.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        btnClearChat = new JButton("Xoá Lịch Sử Chat");
        btnClearChat.putClientProperty("JButton.buttonType", "roundRect");
        btnClearChat.setFont(FontUtil.getAdminButtonFont().deriveFont(Font.BOLD));
        btnClearChat.setBackground(new Color(254, 44, 85)); // TikTok Pink for destructive actions
        btnClearChat.setForeground(Color.WHITE);
        btnClearChat.addActionListener(e -> chatTableModel.setRowCount(0));
        pnlActions.add(btnClearChat);

        cardChat.add(pnlActions, BorderLayout.SOUTH);
        add(cardChat, BorderLayout.CENTER);
    }

    public DefaultTableModel getChatTableModel() {
        return chatTableModel;
    }

    public JButton getBtnToggleChatOverlayTab3() {
        return btnToggleChatOverlayTab3;
    }
}
