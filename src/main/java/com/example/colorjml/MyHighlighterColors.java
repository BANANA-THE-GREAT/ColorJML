package com.example.colorjml;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;

import java.awt.*;

public class MyHighlighterColors {
    public static final TextAttributesKey EQ_NE = TextAttributesKey.createTextAttributesKey(
            "EQ_NE",
            new TextAttributes(JBColor.BLUE, null, null, null, Font.PLAIN)
    );
    public static final TextAttributesKey OP = TextAttributesKey.createTextAttributesKey(
            "OP",
            new TextAttributes(JBColor.PINK, null, null, null, Font.PLAIN)
    );
    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey(
            "KEYWORD",
            new TextAttributes(JBColor.ORANGE, null, null, null, Font.BOLD)
    );
}
