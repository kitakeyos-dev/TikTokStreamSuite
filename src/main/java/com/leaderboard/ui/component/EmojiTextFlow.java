package com.leaderboard.ui.component;

import com.leaderboard.util.EmojiParser;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;

public class EmojiTextFlow extends TextFlow {

    public EmojiTextFlow(String text, double fontSize, Color textColor, boolean bold) {
        super();
        Font font = bold
                ? Font.font("Segoe UI", FontWeight.BOLD, fontSize)
                : Font.font("Segoe UI", fontSize);

        TextFlow flow = EmojiParser.createEmojiTextFlow(text, fontSize, textColor, font);
        this.getChildren().addAll(flow.getChildren());
    }
}
