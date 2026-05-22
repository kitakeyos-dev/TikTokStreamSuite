package com.leaderboard.util;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FontUtil {
    private static Font baseFont;

    static {
        // Safe robust font selection
        String[] preferredFonts = {"Segoe UI", "Inter", "Arial", "SansSerif"};
        for (String fontName : preferredFonts) {
            baseFont = new Font(fontName, Font.PLAIN, 12);
            // Check if available on system
            if (baseFont.getFamily().equalsIgnoreCase(fontName)) {
                break;
            }
        }
        if (baseFont == null) {
            baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
    }

    public static Font getTitleFont() {
        return baseFont.deriveFont(Font.BOLD, 18f); // headline-sm (20px equivalent)
    }

    public static Font getNameFont() {
        return baseFont.deriveFont(Font.BOLD, 15f); // body-md (16px equivalent)
    }

    public static Font getSubtitleFont() {
        return baseFont.deriveFont(Font.BOLD, 10.5f); // label-caps (12px equivalent)
    }

    public static Font getCoinsFont() {
        return baseFont.deriveFont(Font.BOLD, 14f); // stat-number (18px equivalent)
    }

    public static Font getDashboardLabelFont() {
        return baseFont.deriveFont(Font.BOLD, 13f);
    }

    public static Font getAdminButtonFont() {
        return baseFont.deriveFont(Font.BOLD, 12f);
    }

    public static boolean isEmoji(int cp) {
        if (cp >= 0x1F600 && cp <= 0x1F64F) return true; // Emoticons
        if (cp >= 0x1F300 && cp <= 0x1F5FF) return true; // Misc Symbols and Pictographs
        if (cp >= 0x1F680 && cp <= 0x1F6FF) return true; // Transport and Map
        if (cp >= 0x1F1E6 && cp <= 0x1F1FF) return true; // Flags
        if (cp >= 0x1F900 && cp <= 0x1F9FF) return true; // Supplemental Symbols
        if (cp >= 0x1FA70 && cp <= 0x1FAFF) return true; // Symbols and Pictographs Extended-A
        if (cp >= 0x2600 && cp <= 0x27BF) return true;   // Misc Symbols & Dingbats
        if (cp >= 0x2B50 && cp <= 0x2B55) return true;   // Stars/shapes
        if (cp >= 0xFE00 && cp <= 0xFE0F) return true;   // Variation selectors
        if (cp == 0x200D) return true;                   // Zero Width Joiner
        if (cp >= 0xE0020 && cp <= 0xE007F) return true; // Tags
        return false;
    }

    private static final String[] FALLBACK_FONT_NAMES = {
            "Segoe UI Symbol",
            "Segoe UI Emoji",
            "Segoe UI Historic",
            "Arial Unicode MS",
            "MS Gothic",
            "SansSerif",
            "Dialog"
    };

    private static final Map<String, Font> fallbackFontCache = new ConcurrentHashMap<>();

    private static Font getFallbackFont(String fontName, Font baseFont) {
        String key = fontName + "_" + baseFont.getStyle() + "_" + baseFont.getSize();
        return fallbackFontCache.computeIfAbsent(key, k -> new Font(fontName, baseFont.getStyle(), baseFont.getSize()));
    }

    private static Font selectFontForCodePoint(int codePoint, Font baseFont) {
        if (baseFont.canDisplay(codePoint)) {
            return baseFont;
        }
        for (String fontName : FALLBACK_FONT_NAMES) {
            Font fb = getFallbackFont(fontName, baseFont);
            if (fb.canDisplay(codePoint)) {
                return fb;
            }
        }
        return baseFont;
    }

    /**
     * Draws a string, rendering emojis as high-fidelity color Twemojis inline.
     */
    public static void drawStringWithEmoji(java.awt.Graphics2D g2, String text, int x, int y, Font baseFont) {
        drawStringWithEmoji(g2, text, x, y, baseFont, null);
    }

    /**
     * Draws a string, rendering emojis as high-fidelity color Twemojis inline with async repainting support.
     */
    public static void drawStringWithEmoji(java.awt.Graphics2D g2, String text, int x, int y, Font baseFont, Runnable onLoaded) {
        if (text == null || text.isEmpty()) return;

        int fontSize = baseFont.getSize();
        FontMetrics baseFm = g2.getFontMetrics(baseFont);
        int ascent = baseFm.getAscent();

        int currentX = x;
        int len = text.length();
        StringBuilder textBuf = new StringBuilder();
        Font currentFont = null;

        for (int i = 0; i < len; ) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);

            if (isEmoji(codePoint)) {
                // 1. Draw accumulated text buffer first
                if (textBuf.length() > 0) {
                    String s = textBuf.toString();
                    Font drawFont = currentFont != null ? currentFont : baseFont;
                    g2.setFont(drawFont);
                    g2.drawString(s, currentX, y);
                    currentX += g2.getFontMetrics(drawFont).stringWidth(s);
                    textBuf.setLength(0);
                    currentFont = null;
                }

                // 2. Fetch and draw emoji image
                java.awt.image.BufferedImage emojiImg = ImageLoader.getEmojiImage(codePoint, onLoaded);
                if (emojiImg != null) {
                    // Center the emoji vertically with the font text
                    int emojiY = y - ascent + (ascent - fontSize) / 2;
                    g2.drawImage(emojiImg, currentX, emojiY, fontSize, fontSize, null);
                }
                currentX += fontSize + 2; // advance x by emoji width + gap
            } else {
                Font targetFont = selectFontForCodePoint(codePoint, baseFont);
                if (currentFont == null) {
                    currentFont = targetFont;
                } else if (!currentFont.equals(targetFont)) {
                    // Flush accumulated text
                    String s = textBuf.toString();
                    g2.setFont(currentFont);
                    g2.drawString(s, currentX, y);
                    currentX += g2.getFontMetrics(currentFont).stringWidth(s);
                    textBuf.setLength(0);
                    currentFont = targetFont;
                }
                textBuf.appendCodePoint(codePoint);
            }

            i += charCount;
        }

        // 3. Draw remaining text buffer
        if (textBuf.length() > 0) {
            String s = textBuf.toString();
            Font drawFont = currentFont != null ? currentFont : baseFont;
            g2.setFont(drawFont);
            g2.drawString(s, currentX, y);
        }
    }

    /**
     * Calculates the width of a string rendered with inline Twemoji images.
     */
    public static int getStringWithEmojiWidth(java.awt.Graphics2D g2, String text, Font baseFont) {
        if (text == null || text.isEmpty()) return 0;

        int fontSize = baseFont.getSize();
        int width = 0;
        int len = text.length();
        StringBuilder textBuf = new StringBuilder();
        Font currentFont = null;

        for (int i = 0; i < len; ) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);

            if (isEmoji(codePoint)) {
                if (textBuf.length() > 0) {
                    Font drawFont = currentFont != null ? currentFont : baseFont;
                    width += g2.getFontMetrics(drawFont).stringWidth(textBuf.toString());
                    textBuf.setLength(0);
                    currentFont = null;
                }
                width += fontSize + 2;
            } else {
                Font targetFont = selectFontForCodePoint(codePoint, baseFont);
                if (currentFont == null) {
                    currentFont = targetFont;
                } else if (!currentFont.equals(targetFont)) {
                    Font drawFont = currentFont;
                    width += g2.getFontMetrics(drawFont).stringWidth(textBuf.toString());
                    textBuf.setLength(0);
                    currentFont = targetFont;
                }
                textBuf.appendCodePoint(codePoint);
            }

            i += charCount;
        }

        if (textBuf.length() > 0) {
            Font drawFont = currentFont != null ? currentFont : baseFont;
            width += g2.getFontMetrics(drawFont).stringWidth(textBuf.toString());
        }

        return width;
    }

}

