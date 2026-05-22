package com.leaderboard.ui.overlay;

import com.leaderboard.model.Gifter;
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

public class GiftLeaderboardOverlay extends JFrame {
    // Standard hex transparency colors matching code.html
    private static final Color COLOR_BG = new Color(21, 18, 27, 200); // surface-dim (78% opacity)
    private static final Color COLOR_SURFACE = new Color(33, 30, 39, 153); // surface-container (60% opacity)
    private static final Color COLOR_HEADER = new Color(33, 30, 39, 127); // bg-surface-container/50 (50% opacity)
    private static final Color COLOR_ON_BACKGROUND = new Color(231, 224, 237); // #e7e0ed
    private static final Color COLOR_ON_SURFACE_VARIANT = new Color(203, 195, 215); // #cbc3d7
    private static final Color COLOR_OUTLINE_VARIANT = new Color(73, 68, 84, 50); // #494454 (border highlight)

    private static final Color COLOR_PRIMARY = new Color(208, 188, 255); // Light purple Dim
    private static final Color COLOR_GOLD = new Color(255, 215, 0);
    private static final Color COLOR_SILVER = new Color(192, 192, 192);
    private static final Color COLOR_BRONZE = new Color(205, 127, 50);

    private Point dragOffset;
    private WidgetPanel widgetPanel;

    public GiftLeaderboardOverlay() {
        setTitle("Bảng Xếp Hạng"); // Title needed for OBS Window Capture detection
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
        widgetPanel.setBounds(20, 20, 320, 720); // Height updated to 720px
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

            List<Gifter> list = DataManager.getGifters();
            int limit = Math.min(list.size(), 10); // Display up to 10 gifters

            int currentY = 0;
            for (int i = 0; i < limit; i++) {
                Gifter g = list.get(i);
                int rank = i + 1;
                g.setRank(rank);

                Color accent = COLOR_PRIMARY;
                Color glow = new Color(208, 188, 255, 60);
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

                RowPanel rowPanel = new RowPanel(g, accent, glow, rank);
                rowPanel.setBounds(0, currentY, 300, rowHeight);
                pnlRowsContainer.add(rowPanel);

                currentY += rowGap;

                // Queue background image loading asynchronously only for Ranks 1-3 spotlights
                if (rank <= 3) {
                    if (g.getAvatarUrl() != null && ImageLoader.getImage(g.getUniqueId()) == null) {
                        ImageLoader.loadImageAsync(g.getUniqueId(), g.getAvatarUrl(), this::repaint);
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
            g2.setColor(COLOR_PRIMARY);
            g2.setFont(FontUtil.getTitleFont());
            g2.drawString("BẢNG XẾP HẠNG", 48, 42);

            // 6. Custom Crown drawing
            g2.setColor(COLOR_GOLD);
            Path2D crown = new Path2D.Double();
            crown.moveTo(20, 43);
            crown.lineTo(16, 27);
            crown.lineTo(22, 33);
            crown.lineTo(26, 23);
            crown.lineTo(30, 33);
            crown.lineTo(36, 27);
            crown.lineTo(32, 43);
            crown.closePath();
            g2.fill(crown);

            // 7. Live Indicator badge
            g2.setColor(new Color(110, 68, 255, 80));
            g2.fillRoundRect(w - 70, 20, 50, 26, 13, 13);
            g2.setColor(COLOR_PRIMARY);
            g2.setFont(FontUtil.getSubtitleFont());
            g2.drawString("LIVE", w - 57, 37);

            g2.dispose();
        }
    }

    private class RowPanel extends JPanel {
        private Gifter gifter;
        private Color accentColor;
        private Color glowColor;
        private int rank;
        private int borderPadding;
        private int avatarDim;

        public RowPanel(Gifter gifter, Color accentColor, Color glowColor, int rank) {
            this.gifter = gifter;
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

                BufferedImage avatarImage = ImageLoader.getImage(gifter.getUniqueId());
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
                    ImageLoader.drawPlaceholder(g2, gifter.getNickname(), avatarX, avatarY, avatarDim, accentColor);
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

                String nickname = gifter.getNickname();
                String username = gifter.getNickname().equals(gifter.getUniqueId()) ? "" : "@" + gifter.getUniqueId();

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

                // 7. Draw Points and Coins Icon on the Right
                String coinsStr = String.format("%,d", gifter.getPoints());
                g2.setFont(FontUtil.getCoinsFont());
                FontMetrics fmCoins = g2.getFontMetrics();
                int coinsX = w - offset - 20 - 20 - fmCoins.stringWidth(coinsStr);
                int coinsY = avatarY + 28;

                g2.setColor(COLOR_ON_BACKGROUND);
                g2.drawString(coinsStr, coinsX, coinsY);

                // Gold Coin graphic
                int coinX = w - offset - 30;
                int coinY = coinsY - 14;
                g2.setColor(COLOR_GOLD);
                g2.fillOval(coinX, coinY, 16, 16);
                g2.setColor(new Color(197, 140, 0));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(coinX, coinY, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2.drawString("$", coinX + 5, coinY + 12);

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
                String nickname = gifter.getNickname();
                String username = gifter.getNickname().equals(gifter.getUniqueId()) ? "" : "@" + gifter.getUniqueId();

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

                // 5. Draw Points and Coins Icon on the Right
                String coinsStr = String.format("%,d", gifter.getPoints());
                g2.setFont(FontUtil.getCoinsFont().deriveFont(Font.BOLD, 12f));
                FontMetrics fmCoins = g2.getFontMetrics();
                int coinsX = w - offset - 15 - 16 - fmCoins.stringWidth(coinsStr);
                int coinsY = offset + (rowH + fmCoins.getAscent() - fmCoins.getLeading()) / 2 - 2;

                g2.setColor(COLOR_ON_SURFACE_VARIANT);
                g2.drawString(coinsStr, coinsX, coinsY);

                // Gold Coin graphic (smaller 12x12px for compact design)
                int coinX = w - offset - 25;
                int coinY = coinsY - 10;
                g2.setColor(COLOR_GOLD);
                g2.fillOval(coinX, coinY, 12, 12);
                g2.setColor(new Color(197, 140, 0));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawOval(coinX, coinY, 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 8));
                g2.drawString("$", coinX + 4, coinY + 9);
            }

            g2.dispose();
        }
    }
}
