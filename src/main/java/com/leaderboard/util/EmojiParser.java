package com.leaderboard.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class EmojiParser {
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

    public static TextFlow createEmojiTextFlow(String text, double fontSize, Color textColor, Font font) {
        TextFlow flow = new TextFlow();
        if (text == null || text.isEmpty()) return flow;

        int len = text.length();
        StringBuilder textBuf = new StringBuilder();

        for (int i = 0; i < len; ) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);

            if (isEmoji(codePoint)) {
                // 1. Flush regular text buffer first
                if (textBuf.length() > 0) {
                    Text txt = new Text(textBuf.toString());
                    txt.setFont(font);
                    txt.setFill(textColor);
                    flow.getChildren().add(txt);
                    textBuf.setLength(0);
                }

                // 2. Add Twemoji image
                String hex = Integer.toHexString(codePoint).toLowerCase();
                String urlStr = "https://cdnjs.cloudflare.com/ajax/libs/twemoji/14.0.2/72x72/" + hex + ".png";
                
                // Asynchronously load the Twemoji image natively with background thread & memory caching enabled!
                Image img = new Image(urlStr, fontSize + 4, fontSize + 4, true, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(fontSize + 4);
                iv.setFitHeight(fontSize + 4);
                
                // Align the emoji image slightly vertically to match the text flow perfectly
                iv.setTranslateY(3); 

                flow.getChildren().add(iv);
            } else {
                textBuf.appendCodePoint(codePoint);
            }

            i += charCount;
        }

        // 3. Flush remaining text buffer
        if (textBuf.length() > 0) {
            Text txt = new Text(textBuf.toString());
            txt.setFont(font);
            txt.setFill(textColor);
            flow.getChildren().add(txt);
        }

        return flow;
    }
}
