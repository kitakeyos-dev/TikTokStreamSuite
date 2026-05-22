package com.leaderboard.ui.overlay;

import com.leaderboard.model.Liker;
import com.leaderboard.util.DataManager;
import com.leaderboard.util.FontUtil;
import com.leaderboard.util.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class TopLikeOverlay extends JFrame {
    // Standard hex transparency colors matching modern dark StreamPulse theme
    private static final Color COLOR_BG = new Color(21, 18, 27, 200); // surface-dim (78% opacity)
    private static final Color COLOR_SURFACE = new Color(33, 30, 39, 153); // surface-container (60% opacity)
    private static final Color COLOR_HEADER = new Color(33, 30, 39, 127); // bg-surface-container/50 (50% opacity)
    private static final Color COLOR_ON_BACKGROUND = new Color(231, 224, 237); // #e7e0ed
    private static final Color COLOR_ON_SURFACE_VARIANT = new Color(203, 195, 215); // #cbc3d7
    private static final Color COLOR_OUTLINE_VARIANT = new Color(73, 68, 84, 50); // #494454 (border highlight)

    private static final Color COLOR_PRIMARY = new Color(254, 44, 85); // TikTok Pink
    private static final Color COLOR_GOLD = new Color(255, 215, 0);
    private static final Color COLOR_SILVER = new Color(192, 192, 192);
    private static final Color COLOR_BRONZE = new Color(205, 127, 50);
    private static final Color COLOR_NEON_BLUE = new Color(37, 244, 238); // Cyan Accent

    private Point dragOffset;
    private WidgetPanel widgetPanel;

    public TopLikeOverlay() {
        setTitle("Top Thả Tim"); // Title needed for OBS Window Capture detection
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // Transparent container
        setSize(360, 760); // Taller window to display up to 10 slots
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
        widgetPanel = new WidgetPanel();
        widgetPanel.setBounds(20, 20, 320, 720); // Width 320px, Height 720px
        add(widgetPanel);

        // Make window draggable
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
        widgetPanel.addMouseListener(dragListener);
        widgetPanel.addMouseMotionListener(dragListener);

        updateLeaderboard();
    }

    public void updateLeaderboard() {
        SwingUtilities.invokeLater(() -> {
            widgetPanel.rebuildRows();
            widgetPanel.repaint();
        });
    }

    private static void drawHeart(Graphics2D g2, int x, int y, int size, Color color) {
        Graphics2D g = (Graphics2D) g2.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);
        
        Path2D heart = new Path2D.Double();
        double scale = size / 24.0;
        // Standard heart path scaled to size
        heart.moveTo(x + 12 * scale, y + 21 * scale);
        heart.curveTo(x + 6 * scale, y + 16 * scale, x + 2 * scale, y + 11 * scale, x + 2 * scale, y + 7 * scale);
        heart.curveTo(x + 2 * scale, y + 3 * scale, x + 5 * scale, y + 0 * scale, x + 9 * scale, y + 0 * scale);
        heart.curveTo(x + 11.5 * scale, y + 0 * scale, x + 13 * scale, y + 1.5 * scale, x + 14 * scale, y + 3 * scale);
        heart.curveTo(x + 15 * scale, y + 1.5 * scale, x + 16.5 * scale, y + 0 * scale, x + 19 * scale, y + 0 * scale);
        heart.curveTo(x + 23 * scale, y + 0 * scale, x + 26 * scale, y + 3 * scale, x + 26 * scale, y + 7 * scale);
        heart.curveTo(x + 26 * scale, y + 11 * scale, x + 22 * scale, y + 16 * scale, x + 12 * scale, y + 21 * scale);
        heart.closePath();
        
        g.fill(heart);
        g.dispose();
    }

    private class WidgetPanel extends JPanel {
        private JPanel pnlRowsContainer;

        public WidgetPanel() {
            setOpaque(false);
            setLayout(null);

            // Container for dynamic leaderboard rows
            pnlRowsContainer = new JPanel();
            pnlRowsContainer.setOpaque(false);
            pnlRowsContainer.setLayout(null);
            pnlRowsContainer.setBounds(10, 85, 300, 625); // Expanded bounds to 625px
            add(pnlRowsContainer);
        }

        public void rebuildRows() {
            pnlRowsContainer.removeAll();

            List<Liker> list = DataManager.getLikers();
            int limit = Math.min(list.size(), 10); // Display up to 10 likers

            int currentY = 0;
            for (int i = 0; i < limit; i++) {
                Liker l = list.get(i);
                int rank = i + 1;
                l.setRank(rank);

                Color accent = COLOR_PRIMARY;
                Color glow = new Color(254, 44, 85, 60);
                int rowHeight;
                int rowGap;

                if (rank == 1) {
                    accent = COLOR_GOLD;
                    glow = new Color(255, 215, 0, 100);
                    rowHeight = 85;
                    rowGap = 92;
                } else if (rank == 2) {
                    accent = COLOR_SILVER;
                    glow = new Color(192, 192, 192, 80);
                    rowHeight = 75;
                    rowGap = 82;
                } else if (rank == 3) {
                    accent = COLOR_BRONZE;
                    glow = new Color(205, 127, 50, 60);
                    rowHeight = 75;
                    rowGap = 82;
                } else {
                    // Compact rows for Ranks 4-10
                    accent = COLOR_PRIMARY;
                    glow = null;
                    rowHeight = 42;
                    rowGap = 48;
                }

                RowPanel rowPanel = new RowPanel(l, accent, glow, rank);
                rowPanel.setBounds(0, currentY, 300, rowHeight);
                pnlRowsContainer.add(rowPanel);

                currentY += rowGap;

                // Queue background image loading asynchronously only for Ranks 1-3 spotlights
                if (rank <= 3) {
                    if (l.getAvatarUrl() != null && ImageLoader.getImage(l.getUniqueId()) == null) {
                        ImageLoader.loadImageAsync(l.getUniqueId(), l.getAvatarUrl(), this::repaint);
                    }
                }
            }
            pnlRowsContainer.revalidate();
            pnlRowsContainer.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 1. Draw main glass panel background card
            g2.setColor(COLOR_BG);
            g2.fillRoundRect(0, 0, w, h, 24, 24);

            // 2. Draw white outline glass highlight
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(new Color(255, 255, 255, 25));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 24, 24);

            // 3. Draw Header Fill
            g2.setColor(COLOR_HEADER);
            g2.fillRoundRect(1, 1, w - 2, 70, 24, 24);
            g2.fillRect(1, 40, w - 2, 31); // straighten bottom half

            // 4. Header line separator
            g2.setColor(COLOR_OUTLINE_VARIANT);
            g2.drawLine(0, 71, w, 71);

            // 5. Title
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(COLOR_ON_BACKGROUND);
            g2.setFont(FontUtil.getTitleFont());
            g2.drawString("TOP THẢ TIM", 48, 42);

            // 6. Beautiful Vector Heart Drawing next to title (No low-quality emoji)
            drawHeart(g2, 18, 23, 20, COLOR_PRIMARY);

            // 7. Live Indicator badge
            g2.setColor(new Color(110, 68, 255, 80));
            g2.fillRoundRect(w - 70, 20, 50, 26, 13, 13);
            g2.setColor(COLOR_ON_BACKGROUND);
            g2.setFont(FontUtil.getSubtitleFont());
            g2.drawString("LIVE", w - 57, 37);

            g2.dispose();
        }
    }

    private class RowPanel extends JPanel {
        private Liker liker;
        private Color accentColor;
        private Color glowColor;
        private int rank;
        private int borderPadding;
        private int avatarDim;

        public RowPanel(Liker liker, Color accentColor, Color glowColor, int rank) {
            this.liker = liker;
            this.accentColor = accentColor;
            this.glowColor = glowColor;
            this.rank = rank;
            this.borderPadding = (rank <= 3) ? 6 : 0;
            this.avatarDim = (rank == 1) ? 46 : 40;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int offset = borderPadding;
            int rowW = w - 2 * offset;
            int rowH = h - 2 * offset;

            if (rank <= 3) {
                // SPOTLIGHT CARD RENDERING (Ranks 1-3)
                // 1. Draw outer glowing outline
                if (glowColor != null) {
                    for (int i = 0; i < borderPadding; i++) {
                        float opacity = (float) (borderPadding - i) / (borderPadding * borderPadding) * 0.4f;
                        g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(),
                                (int) (opacity * 255)));
                        g2.drawRoundRect(i, i, w - 1 - i * 2, h - 1 - i * 2, 20 - i, 20 - i);
                    }
                }

                // 2. Row background glass card
                g2.setColor(COLOR_SURFACE);
                g2.fillRoundRect(offset, offset, rowW, rowH, 16, 16);

                // 3. Row Border Highlight matching rank accent
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 120));
                g2.drawRoundRect(offset, offset, rowW, rowH, 16, 16);

                // 4. Draw Avatar
                int avatarX = offset + 15;
                int avatarY = offset + (rowH - avatarDim) / 2;

                BufferedImage avatarImage = ImageLoader.getImage(liker.getUniqueId());
                if (avatarImage != null) {
                    Shape clip = new Ellipse2D.Double(avatarX, avatarY, avatarDim, avatarDim);
                    g2.setClip(clip);
                    g2.drawImage(avatarImage, avatarX, avatarY, avatarDim, avatarDim, null);
                    g2.setClip(null); // Reset clip

                    // Ring Stroke around image avatar
                    g2.setStroke(new BasicStroke(2.0f));
                    g2.setColor(accentColor);
                    g2.drawOval(avatarX, avatarY, avatarDim, avatarDim);
                } else {
                    // Vector placeholder text initial
                    ImageLoader.drawPlaceholder(g2, liker.getNickname(), avatarX, avatarY, avatarDim, accentColor);
                }

                // 5. Draw Rank Badge (floating on top-left)
                int badgeDim = 20;
                g2.setColor(accentColor);
                g2.fillOval(offset - 2, offset - 2, badgeDim, badgeDim);

                g2.setColor(Color.BLACK);
                g2.setFont(FontUtil.getSubtitleFont().deriveFont(Font.BOLD, 12f));
                FontMetrics fmBadge = g2.getFontMetrics();
                String badgeText = String.valueOf(rank);
                int badgeTextX = offset - 2 + (badgeDim - fmBadge.stringWidth(badgeText)) / 2;
                int badgeTextY = offset - 2 + (badgeDim + fmBadge.getAscent() - fmBadge.getLeading()) / 2 - 2;
                g2.drawString(badgeText, badgeTextX, badgeTextY);

                // 6. Draw Names
                int infoX = avatarX + avatarDim + 15;
                g2.setColor(COLOR_ON_BACKGROUND);

                String nickname = liker.getNickname();
                String username = liker.getNickname().equals(liker.getUniqueId()) ? "" : "@" + liker.getUniqueId();

                if (username.isEmpty()) {
                    FontUtil.drawStringWithEmoji(g2, nickname, infoX, avatarY + 28, FontUtil.getNameFont(),
                            this::repaint);
                } else {
                    FontUtil.drawStringWithEmoji(g2, nickname, infoX, avatarY + 20, FontUtil.getNameFont(),
                            this::repaint);
                    g2.setColor(COLOR_ON_SURFACE_VARIANT);
                    g2.setFont(FontUtil.getSubtitleFont());
                    g2.drawString(username, infoX, avatarY + 38);
                }

                // 7. Draw Likes Count and Heart Icon on the Right
                String likesStr = String.format("%,d", liker.getLikes());
                g2.setFont(FontUtil.getCoinsFont());
                FontMetrics fmLikes = g2.getFontMetrics();
                int likesX = w - offset - 20 - 20 - fmLikes.stringWidth(likesStr);
                int likesY = avatarY + 28;

                g2.setColor(COLOR_ON_BACKGROUND);
                g2.drawString(likesStr, likesX, likesY);

                // Pink Heart graphic
                int heartX = w - offset - 30;
                int heartY = likesY - 14;
                drawHeart(g2, heartX, heartY, 16, COLOR_PRIMARY);

            } else {
                // COMPACT ROW RENDERING (Ranks 4-10)
                // 1. Row background card
                g2.setColor(COLOR_SURFACE);
                g2.fillRoundRect(offset, offset, rowW, rowH, 8, 8);

                // 2. Subtle Border Highlight
                g2.setStroke(new BasicStroke(1.0f));
                g2.setColor(COLOR_OUTLINE_VARIANT);
                g2.drawRoundRect(offset, offset, rowW, rowH, 8, 8);

                // 3. Draw Rank Number
                g2.setColor(COLOR_ON_SURFACE_VARIANT);
                g2.setFont(FontUtil.getNameFont().deriveFont(Font.BOLD, 14f));
                FontMetrics fmRank = g2.getFontMetrics();
                String rankText = String.valueOf(rank);
                int rankX = offset + 15 + (16 - fmRank.stringWidth(rankText)) / 2;
                int rankY = offset + (rowH + fmRank.getAscent() - fmRank.getLeading()) / 2 - 2;
                g2.drawString(rankText, rankX, rankY);

                // 4. Draw Names
                int infoX = offset + 45;
                String nickname = liker.getNickname();
                String username = liker.getNickname().equals(liker.getUniqueId()) ? "" : "@" + liker.getUniqueId();

                g2.setColor(COLOR_ON_BACKGROUND);
                Font nameFont = FontUtil.getNameFont().deriveFont(Font.BOLD, 13f);
                if (username.isEmpty()) {
                    int textY = offset + (rowH + g2.getFontMetrics(nameFont).getAscent()) / 2 - 2;
                    FontUtil.drawStringWithEmoji(g2, nickname, infoX, textY, nameFont, this::repaint);
                } else {
                    FontUtil.drawStringWithEmoji(g2, nickname, infoX, offset + 17, nameFont, this::repaint);
                    g2.setColor(COLOR_ON_SURFACE_VARIANT);
                    g2.setFont(FontUtil.getSubtitleFont().deriveFont(Font.BOLD, 9.5f));
                    g2.drawString(username, infoX, offset + 31);
                }

                // 5. Draw Likes Count and Heart Icon on the Right
                String likesStr = String.format("%,d", liker.getLikes());
                g2.setFont(FontUtil.getCoinsFont().deriveFont(Font.BOLD, 12f));
                FontMetrics fmLikes = g2.getFontMetrics();
                int likesX = w - offset - 15 - 16 - fmLikes.stringWidth(likesStr);
                int likesY = offset + (rowH + fmLikes.getAscent() - fmLikes.getLeading()) / 2 - 2;

                g2.setColor(COLOR_ON_SURFACE_VARIANT);
                g2.drawString(likesStr, likesX, likesY);

                // Smaller 12x12px Heart graphic for compact list
                int heartX = w - offset - 25;
                int heartY = likesY - 10;
                drawHeart(g2, heartX, heartY, 12, COLOR_PRIMARY);
            }

            g2.dispose();
        }
    }
}
