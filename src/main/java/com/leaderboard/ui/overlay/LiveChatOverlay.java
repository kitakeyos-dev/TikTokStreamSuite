package com.leaderboard.ui.overlay;

import com.leaderboard.model.ChatMessage;
import com.leaderboard.util.FontUtil;
import com.leaderboard.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class LiveChatOverlay extends JFrame {
    private static final Color COLOR_BG = new Color(21, 18, 27, 200); // surface-dim (78% opacity)
    private static final Color COLOR_SURFACE = new Color(33, 30, 39, 153); // surface-container (60% opacity)
    private static final Color COLOR_HEADER = new Color(33, 30, 39, 127); // bg-surface-container/50 (50% opacity)

    private static final Color COLOR_PRIMARY = new Color(208, 188, 255); // Light purple Dim
    private static final Color COLOR_ON_BACKGROUND = new Color(231, 224, 237); // #e7e0ed
    private static final Color COLOR_ON_SURFACE_VARIANT = new Color(203, 195, 215); // #cbc3d7
    private static final Color COLOR_OUTLINE_VARIANT = new Color(73, 68, 84, 50); // border highlight

    private Point dragOffset;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final ChatPanel chatPanel;

    public LiveChatOverlay() {
        setTitle("Trò Chuyện"); // Title needed for OBS Window Capture detection
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // transparent window
        setSize(340, 500);
        setLocationRelativeTo(null);
        setType(Type.NORMAL); // NORMAL type allows OBS/TikTok Live Studio to detect this window

        // Load and set application window icon
        try {
            java.net.URL imgUrl = getClass().getResource("/icons/logo.png");
            if (imgUrl != null) {
                setIconImage(new ImageIcon(imgUrl).getImage());
            }
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        setLayout(null);
        chatPanel = new ChatPanel();
        chatPanel.setBounds(10, 10, 320, 480);
        add(chatPanel);

        // Draggable window support
        MouseAdapter dragListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset != null) {
                    Point curr = getLocation();
                    setLocation(curr.x + e.getX() - dragOffset.x, curr.y + e.getY() - dragOffset.y);
                }
            }
        };
        chatPanel.addMouseListener(dragListener);
        chatPanel.addMouseMotionListener(dragListener);
    }

    public void addMessage(String uniqueId, String nickname, String comment, String avatarUrl) {
        SwingUtilities.invokeLater(() -> {
            synchronized (messages) {
                // Limit to 7 messages to comfortably fit the panel height
                if (messages.size() >= 7) {
                    messages.remove(0);
                }
                messages.add(new ChatMessage(uniqueId, nickname, comment, avatarUrl));
            }

            // Queue async avatar loading
            if (avatarUrl != null && ImageLoader.getImage(uniqueId) == null) {
                ImageLoader.loadImageAsync(uniqueId, avatarUrl, chatPanel::repaint);
            }

            chatPanel.repaint();
        });
    }

    public void clearChat() {
        SwingUtilities.invokeLater(() -> {
            synchronized (messages) {
                messages.clear();
            }
            chatPanel.repaint();
        });
    }

    private class ChatPanel extends JPanel {
        public ChatPanel() {
            setOpaque(false);
            setLayout(null);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Paint Main Card Background
            g2.setColor(COLOR_BG);
            g2.fillRoundRect(0, 0, w, h, 20, 20);

            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(new Color(255, 255, 255, 25));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);

            // 2. Header
            g2.setColor(COLOR_HEADER);
            g2.fillRoundRect(1, 1, w - 2, 50, 20, 20);
            g2.fillRect(1, 30, w - 2, 21); // straighten bottom half

            g2.setColor(COLOR_OUTLINE_VARIANT);
            g2.drawLine(0, 51, w, 51);

            g2.setColor(COLOR_PRIMARY);
            g2.setFont(FontUtil.getTitleFont().deriveFont(15f));
            g2.drawString("TRÒ CHUYỆN TRỰC TIẾP", 15, 31);

            // Live indicator
            g2.setColor(new Color(255, 68, 110, 80));
            g2.fillRoundRect(w - 60, 15, 45, 20, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(FontUtil.getSubtitleFont().deriveFont(8.5f));
            g2.drawString("CHAT", w - 50, 29);

            // 3. Render Messages in bubbles
            int startY = 60;
            int bubbleW = w - 20; // 300px
            int bubbleH = 52;
            int gap = 6;

            List<ChatMessage> localMsgs;
            synchronized (messages) {
                localMsgs = new ArrayList<>(messages);
            }

            for (int i = 0; i < localMsgs.size(); i++) {
                ChatMessage msg = localMsgs.get(i);
                int bubbleY = startY + i * (bubbleH + gap);

                // Draw bubble card background
                g2.setColor(COLOR_SURFACE);
                g2.fillRoundRect(10, bubbleY, bubbleW, bubbleH, 12, 12);

                g2.setStroke(new BasicStroke(1.0f));
                g2.setColor(COLOR_OUTLINE_VARIANT);
                g2.drawRoundRect(10, bubbleY, bubbleW, bubbleH, 12, 12);

                // Draw Avatar on left
                int avatarX = 20;
                int avatarY = bubbleY + (bubbleH - 32) / 2;
                int avatarDim = 32;

                BufferedImage avatar = ImageLoader.getImage(msg.getUniqueId());
                if (avatar != null) {
                    Shape clip = new Ellipse2D.Double(avatarX, avatarY, avatarDim, avatarDim);
                    Shape oldClip = g2.getClip();
                    g2.setClip(clip);
                    g2.drawImage(avatar, avatarX, avatarY, avatarDim, avatarDim, null);
                    g2.setClip(oldClip);

                    g2.setStroke(new BasicStroke(1.5f));
                    g2.setColor(COLOR_PRIMARY);
                    g2.drawOval(avatarX, avatarY, avatarDim, avatarDim);
                } else {
                    ImageLoader.drawPlaceholder(g2, msg.getNickname(), avatarX, avatarY, avatarDim, COLOR_PRIMARY);
                }

                // Draw Name and Comment text
                int textX = avatarX + avatarDim + 10;

                // Username/Nickname
                g2.setColor(COLOR_PRIMARY);
                Font nameFont = FontUtil.getNameFont().deriveFont(Font.BOLD, 12f);
                FontUtil.drawStringWithEmoji(g2, msg.getNickname(), textX, bubbleY + 18, nameFont, chatPanel::repaint);

                // Comment body
                g2.setColor(COLOR_ON_BACKGROUND);
                Font commentFont = FontUtil.getSubtitleFont().deriveFont(Font.PLAIN, 11.5f);

                // Truncate comment if too long to prevent overflowing card width
                String commentText = msg.getComment();
                int maxTextWidth = bubbleW - (textX - 10) - 15;
                int actualWidth = FontUtil.getStringWithEmojiWidth(g2, commentText, commentFont);

                if (actualWidth > maxTextWidth) {
                    // Quick truncation
                    while (commentText.length() > 5 && actualWidth > maxTextWidth) {
                        commentText = commentText.substring(0, commentText.length() - 1);
                        actualWidth = FontUtil.getStringWithEmojiWidth(g2, commentText + "...", commentFont);
                    }
                    commentText += "...";
                }

                FontUtil.drawStringWithEmoji(g2, commentText, textX, bubbleY + 36, commentFont, chatPanel::repaint);
            }

            g2.dispose();
        }
    }
}
