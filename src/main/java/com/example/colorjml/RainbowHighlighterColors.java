package com.example.colorjml;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;

import java.awt.*;

public class RainbowHighlighterColors {
    public static final TextAttributesKey[] RAINBOW_COLORS = {
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_1", new TextAttributes(new Color(255, 0, 0), null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_2", new TextAttributes(new Color(255, 127, 0), null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_3", new TextAttributes(new Color(255, 255, 0), null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_4", new TextAttributes(new Color(0, 255, 0), null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_5", new TextAttributes(new Color(0, 0, 255), null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_6", new TextAttributes(new Color(75, 0, 130), null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_7", new TextAttributes(new Color(148, 0, 211), null, null, null, Font.PLAIN)),
    };
    
    public static TextAttributesKey getColor(int level) {
        return RAINBOW_COLORS[level % RAINBOW_COLORS.length];
    }
}
