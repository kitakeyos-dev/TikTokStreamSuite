package com.leaderboard.ui.overlay;

import com.leaderboard.util.ConfigManager;
import com.leaderboard.util.FontUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;

public class LikeGoalOverlay extends JFrame {
    private static final Color COLOR_BG = new Color(21, 18, 27, 200); // surface-dim (78% opacity)
    private static final Color COLOR_SURFACE = new Color(33, 30, 39, 153); // surface-container (60% opacity)
    private static final Color COLOR_PRIMARY = new Color(208, 188, 255); // Light purple Dim
    private static final Color COLOR_ON_BACKGROUND = new Color(231, 224, 237); // #e7e0ed
    private static final Color COLOR_ON_SURFACE_VARIANT = new Color(203, 195, 215); // #cbc3d7
    private static final Color COLOR_OUTLINE_VARIANT = new Color(73, 68, 84, 50); // border highlight

    private Point dragOffset;
    private int totalLikes = 0;
    private int targetLikes = 10000;

    private float pulseScale = 1.0f;
    private boolean pulseGrowing = true;
    private final Timer pulseTimer;
    private final GoalPanel goalPanel;

    public LikeGoalOverlay() {
        setTitle("Mục Tiêu Tim"); // Title needed for OBS Window Capture detection
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // transparent window
        setSize(320, 95);
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

        // Load like target from config
        this.targetLikes = ConfigManager.getConfig().getLikeTarget();

        setLayout(null);
        goalPanel = new GoalPanel();
        goalPanel.setBounds(10, 10, 300, 75);
        add(goalPanel);

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
        goalPanel.addMouseListener(dragListener);
        goalPanel.addMouseMotionListener(dragListener);

        // Animation Timer for Heart pulsing effect
        pulseTimer = new Timer(35, e -> {
            if (pulseGrowing) {
                pulseScale += 0.015f;
                if (pulseScale >= 1.12f) {
                    pulseGrowing = false;
                }
            } else {
                pulseScale -= 0.015f;
                if (pulseScale <= 0.95f) {
                    pulseGrowing = true;
                }
            }
            goalPanel.repaint();
        });
        pulseTimer.start();
    }

    public synchronized void setLikes(int totalLikes) {
        SwingUtilities.invokeLater(() -> {
            synchronized (this) {
                this.totalLikes = totalLikes;
                // Auto-scale target goals in steps of 10,000
                while (this.totalLikes >= this.targetLikes) {
                    this.targetLikes += 10000;
                }
            }
            goalPanel.repaint();
        });
    }

    public synchronized void setTargetLikes(int targetLikes) {
        SwingUtilities.invokeLater(() -> {
            synchronized (this) {
                this.targetLikes = targetLikes;
            }
            goalPanel.repaint();
        });
    }

    public synchronized int getTotalLikes() {
        return totalLikes;
    }

    public synchronized int getTargetLikes() {
        return targetLikes;
    }

    @Override
    public void dispose() {
        if (pulseTimer != null) {
            pulseTimer.stop();
        }
        super.dispose();
    }

    private class GoalPanel extends JPanel {
        public GoalPanel() {
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

            // 1. Paint Glassmorphic Card Background
            g2.setColor(COLOR_BG);
            g2.fillRoundRect(0, 0, w, h, 16, 16);

            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(new Color(255, 255, 255, 25));
            g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

            // 2. Draw Heart Icon (Pulsing Vector Path)
            int heartX = 22;
            int heartY = 24;
            int heartSize = 22;

            g2.translate(heartX, heartY);
            g2.scale(pulseScale, pulseScale);
            g2.translate(-heartSize / 2.0, -heartSize / 2.0);

            // Custom painted heart path
            Path2D heart = new Path2D.Double();
            heart.moveTo(11, 4);
            heart.curveTo(11, 4, 7, 0, 3, 0);
            heart.curveTo(0, 0, 0, 4, 0, 6);
            heart.curveTo(0, 11, 6, 17, 11, 21);
            heart.curveTo(16, 17, 22, 11, 22, 6);
            heart.curveTo(22, 4, 22, 0, 19, 0);
            heart.curveTo(15, 0, 11, 4, 11, 4);
            heart.closePath();

            // Hot Pink to Red Gradient fill
            GradientPaint heartGrad = new GradientPaint(0, 0, new Color(255, 68, 110), 0, heartSize,
                    new Color(220, 20, 60));
            g2.setPaint(heartGrad);
            g2.fill(heart);

            // Reset translation/scale matrix
            g2.translate(heartSize / 2.0, heartSize / 2.0);
            g2.scale(1.0 / pulseScale, 1.0 / pulseScale);
            g2.translate(-heartX, -heartY);

            // 3. Draw Goal Status Text
            int textX = 42;
            g2.setColor(COLOR_PRIMARY);
            g2.setFont(FontUtil.getNameFont().deriveFont(Font.BOLD, 12.5f));
            g2.drawString("MỤC TIÊU THẢ TIM", textX, 22);

            int currentVal, targetVal;
            synchronized (LikeGoalOverlay.this) {
                currentVal = totalLikes;
                targetVal = targetLikes;
            }

            g2.setColor(COLOR_ON_BACKGROUND);
            g2.setFont(FontUtil.getCoinsFont().deriveFont(12f));
            String statusStr = String.format("%,d / %,d", currentVal, targetVal);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(statusStr, w - 15 - fm.stringWidth(statusStr), 22);

            // 4. Draw Progress Bar Track
            int barX = 15;
            int barY = 40;
            int barW = w - 30; // 270px
            int barH = 15;

            g2.setColor(COLOR_SURFACE);
            g2.fillRoundRect(barX, barY, barW, barH, 8, 8);

            g2.setStroke(new BasicStroke(1.0f));
            g2.setColor(COLOR_OUTLINE_VARIANT);
            g2.drawRoundRect(barX, barY, barW, barH, 8, 8);

            // 5. Draw Progress Bar Gradient Fill
            double pct = targetVal > 0 ? (double) currentVal / targetVal : 0;
            if (pct > 1.0)
                pct = 1.0;
            int fillW = (int) (barW * pct);

            if (fillW > 4) {
                Graphics2D fillG = (Graphics2D) g2.create();
                fillG.setClip(new java.awt.geom.RoundRectangle2D.Double(barX, barY, barW, barH, 8, 8));

                // Hot pink to violet gradient fill
                GradientPaint barGrad = new GradientPaint(barX, 0, new Color(255, 68, 110), barX + barW, 0,
                        new Color(110, 68, 255));
                fillG.setPaint(barGrad);
                fillG.fillRoundRect(barX, barY, fillW, barH, 8, 8);
                fillG.dispose();
            }

            g2.dispose();
        }
    }
}
