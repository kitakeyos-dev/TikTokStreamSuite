package com.leaderboard.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private static final Map<String, BufferedImage> cache = new ConcurrentHashMap<>();
    private static final Map<String, String> lastUrls = new ConcurrentHashMap<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public static BufferedImage getImage(String uniqueId) {
        if (uniqueId == null || uniqueId.trim().isEmpty()) {
            return null;
        }
        BufferedImage cached = cache.get(uniqueId);
        if (cached != null) {
            return cached;
        }
        // Try to load from local file cache
        File localFile = new File("avatars", uniqueId + ".png");
        if (localFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(localFile);
                if (img != null) {
                    cache.put(uniqueId, img);
                    return img;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    public static void loadImageAsync(String uniqueId, String urlStr, Runnable onLoaded) {
        if (uniqueId == null || uniqueId.trim().isEmpty() || urlStr == null || urlStr.trim().isEmpty()) {
            return;
        }

        boolean hasLocalFile = new File("avatars", uniqueId + ".png").exists();

        // If it's the first time loading after restart, and we have a local file,
        // we can assume the local file corresponds to the saved URL.
        if (!lastUrls.containsKey(uniqueId) && hasLocalFile) {
            lastUrls.put(uniqueId, urlStr);
        }

        String lastUrl = lastUrls.get(uniqueId);

        // If we already have the image in memory AND the URL is the same as the last downloaded one, we don't need to re-download!
        if (cache.containsKey(uniqueId) && hasLocalFile && urlStr.equals(lastUrl)) {
            return;
        }

        executor.submit(() -> {
            try {
                // Create avatars directory if not exists
                File dir = new File("avatars");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File localFile = new File(dir, uniqueId + ".png");

                // If we already have a local file and the URL is the same, just load it
                if (localFile.exists() && urlStr.equals(lastUrl)) {
                    BufferedImage image = ImageIO.read(localFile);
                    if (image != null) {
                        cache.put(uniqueId, image);
                        lastUrls.put(uniqueId, urlStr);
                        if (onLoaded != null) {
                            EventQueue.invokeLater(onLoaded);
                        }
                        return;
                    }
                }

                // Otherwise, download the new avatar from TikTok CDN
                URL url = new URL(urlStr);
                BufferedImage image = ImageIO.read(url);
                if (image != null) {
                    // Save to local file cache
                    ImageIO.write(image, "png", localFile);

                    // Update memory cache
                    cache.put(uniqueId, image);
                    lastUrls.put(uniqueId, urlStr);

                    if (onLoaded != null) {
                        EventQueue.invokeLater(onLoaded);
                    }
                }
            } catch (Exception e) {
                // If download fails (e.g. expired URL or offline), try to load from local file as fallback
                try {
                    File localFile = new File("avatars", uniqueId + ".png");
                    if (localFile.exists()) {
                        BufferedImage image = ImageIO.read(localFile);
                        if (image != null) {
                            cache.put(uniqueId, image);
                            if (onLoaded != null) {
                                EventQueue.invokeLater(onLoaded);
                            }
                        }
                    }
                } catch (Exception ex) {
                    // ignore
                }
            }
        });
    }

    /**
     * Draws a high-contrast premium letter avatar when the remote image is not yet available or failed.
     */
    public static void drawPlaceholder(Graphics2D g2, String name, int x, int y, int dim, Color accentColor) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Render circular background matching tier accent color slightly transparent
        g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60));
        g2.fillOval(x, y, dim, dim);

        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(accentColor);
        g2.drawOval(x, y, dim, dim);

        // Draw initial inside circle
        String initial = "?";
        if (name != null && !name.trim().isEmpty()) {
            int firstCodePoint = name.codePointAt(0);
            initial = new String(Character.toChars(firstCodePoint)).toUpperCase();
        }
        g2.setColor(Color.WHITE);
        Font baseFont = new Font("Segoe UI", Font.BOLD, dim / 2);
        g2.setFont(baseFont);
        
        int textWidth = FontUtil.getStringWithEmojiWidth(g2, initial, baseFont);
        FontMetrics fm = g2.getFontMetrics(baseFont);
        int textX = x + (dim - textWidth) / 2;
        int textY = y + (dim - fm.getHeight()) / 2 + fm.getAscent();
        
        FontUtil.drawStringWithEmoji(g2, initial, textX, textY, baseFont, null);
    }

    private static final Map<Integer, BufferedImage> emojiCache = new ConcurrentHashMap<>();
    private static final Map<Integer, Boolean> emojiLoading = new ConcurrentHashMap<>();

    public static BufferedImage getEmojiImage(int codePoint, Runnable onLoaded) {
        BufferedImage cached = emojiCache.get(codePoint);
        if (cached != null) {
            return cached;
        }

        // Try local disk cache under "emojis/" folder
        String hex = Integer.toHexString(codePoint).toLowerCase();
        File emojiDir = new File("emojis");
        if (!emojiDir.exists()) {
            emojiDir.mkdirs();
        }
        File localFile = new File(emojiDir, hex + ".png");
        if (localFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(localFile);
                if (img != null) {
                    emojiCache.put(codePoint, img);
                    return img;
                }
            } catch (Exception e) {
                // ignore
            }
        }

        // Download asynchronously if not already loading
        if (emojiLoading.putIfAbsent(codePoint, true) == null) {
            executor.submit(() -> {
                try {
                    String urlStr = "https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + hex + ".png";
                    URL url = new URL(urlStr);
                    BufferedImage img = ImageIO.read(url);
                    if (img != null) {
                        ImageIO.write(img, "png", localFile);
                        emojiCache.put(codePoint, img);
                        if (onLoaded != null) {
                            EventQueue.invokeLater(onLoaded);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                } finally {
                    emojiLoading.remove(codePoint);
                }
            });
        }

        return null;
    }
}

