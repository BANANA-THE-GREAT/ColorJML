package com.example.colorjml;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;

import java.awt.*;

public class RainbowHighlighterColors {
    public static final TextAttributesKey[] RAINBOW_COLORS = {
//            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_1", new TextAttributes(JBColor.RED, null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_2", new TextAttributes(JBColor.ORANGE, null, null, null, Font.PLAIN)),
//            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_3", new TextAttributes(JBColor.YELLOW, null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_4", new TextAttributes(JBColor.GREEN, null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_5", new TextAttributes(JBColor.CYAN, null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_6", new TextAttributes(JBColor.BLUE, null, null, null, Font.PLAIN)),
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_7", new TextAttributes(JBColor.MAGENTA, null, null, null, Font.PLAIN)),
    };
    
    public static TextAttributesKey getColor(int level) {
        return RAINBOW_COLORS[level % RAINBOW_COLORS.length];
    }
}
