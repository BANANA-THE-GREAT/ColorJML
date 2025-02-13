package com.example.colorjml;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiComment;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import java.awt.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyAnnotator implements Annotator {
    private static final JBColor VARIABLE_ORANGE = new JBColor(new Color(0xFF4500), new Color(0xFFA500));
    private static final JBColor VARIABLE_GREEN = new JBColor(new Color(0x228B22), new Color(0x32CD32));
    private static final JBColor VARIABLE_CYAN = new JBColor(new Color(0x00B5B5), new Color(0x00CED1));
    private static final JBColor VARIABLE_BLUE = new JBColor(new Color(0x0000FF), new Color(0x1E90FF));
    private static final JBColor VARIABLE_PINK = new JBColor(new Color(0x8B008B), new Color(0xFF6666));
    
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiComment) {
            String text = element.getText();
            if (text.length() < 5) {
                return;
            }
            String header = text.substring(0,4);
            if ((!header.equals("/*@ ")) && (!header.equals("//@ "))) {
                return;
            }
            
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element.getTextRange())
                    .textAttributes(DefaultLanguageHighlighterColors.IDENTIFIER)  // 设置一下默认颜色
                    .create();
            
            highlightBrackets(text, element.getTextRange().getStartOffset(), holder);
            // highlightOperators(text, (PsiComment) element, holder);
            highlightKeywords(text, element.getTextRange().getStartOffset(), holder);
            highlightExpressions(text, element.getTextRange().getStartOffset(), holder);
            highlightFuncBehavior(text, element.getTextRange().getStartOffset(), holder);
        }
    }
    
    private void highlightFuncBehavior(String text, int startOffset, AnnotationHolder holder) {
        dye(text, startOffset, holder, "normal_behavior", VARIABLE_GREEN);
        dye(text, startOffset, holder, "assignable", VARIABLE_BLUE);
        dye(text, startOffset, holder, "modifiable", VARIABLE_BLUE);
        dye(text, startOffset, holder, "exceptional_behavior", JBColor.RED);
        dye(text, startOffset, holder, "requires", VARIABLE_GREEN);
        dye(text, startOffset, holder, "ensures", VARIABLE_CYAN);
        dye(text, startOffset, holder, "signals", JBColor.RED);
        dye(text, startOffset, holder, "signals_only", JBColor.RED);
    }
    
    private void dye(String text, int startOffset, AnnotationHolder holder, String expr, JBColor color) {
        TextAttributesKey key = TextAttributesKey.createTextAttributesKey(
                expr,new TextAttributes(color, null, null, null, Font.BOLD)
        );
        
        Pattern pattern = Pattern.compile("\\b" + expr + "\\b");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            TextRange range = TextRange.from(startOffset + matcher.start(), expr.length());
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(range)
                    .textAttributes(key)
                    .create();
        }
    }
    
    private void highlightExpressions(String text, int startOffset, AnnotationHolder holder) {
        HashSet<String> expressions = new HashSet<>();
        Collections.addAll(expressions, "forall", "exists", "sum", "product", "max", "min", "num_of",
                "result", "old", "not_assigned", "not_modified", "nonnullelements", "type", "typeof");
        
        for (String expr : expressions) {
            Pattern pattern = Pattern.compile("\\\\" + expr + "\\b");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                TextRange range = TextRange.from(startOffset + matcher.start(), expr.length() + 1);
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(DefaultLanguageHighlighterColors.STATIC_FIELD)
                        .create();
            }
        }
    }
    
    private void highlightKeywords(String text, int startOffset, AnnotationHolder holder) {
        HashSet<String> keywords = new HashSet<>();
        Collections.addAll(keywords, "public", "private", "protected",
                "instance", "model", "non_null", "invariant", "pure", "also",
                "void", "int", "String", "boolean", "null",
                "safe");
                
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                TextRange range = TextRange.from(startOffset + matcher.start(), keyword.length());
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(DefaultLanguageHighlighterColors.KEYWORD)
                        .create();
            }
        }
    }
    
    private void highlightBrackets(String text, int startOffset, AnnotationHolder holder) {
        Stack<BracketInfo> stack = new Stack<>();
        Map<Character, Character> bracketPairs = new HashMap<>();
        
        // 定义括号对
        bracketPairs.put('(', ')');
        bracketPairs.put('{', '}');
        bracketPairs.put('[', ']');
        bracketPairs.put(')', '(');
        bracketPairs.put('}', '{');
        bracketPairs.put(']', '[');
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // 判断是否是括号
            if (bracketPairs.containsKey(c)) {
                if (c == '(' || c == '{' || c == '[') {
                    // 左括号，压入栈
                    stack.push(new BracketInfo(c, startOffset + i, getColor(stack.size())));
                } else {
                    // 右括号，匹配栈中的左括号
                    if (!stack.isEmpty() && stack.peek().character == bracketPairs.get(c)) {
                        BracketInfo openBracket = stack.pop();
                        TextRange openRange = TextRange.from(openBracket.offset, 1);
                        TextRange closeRange = TextRange.from(startOffset + i, 1);
                        
                        // 高亮左括号和右括号
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                .range(openRange)
                                .textAttributes(openBracket.color)
                                .create();
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                .range(closeRange)
                                .textAttributes(openBracket.color)
                                .create();
                    }
                }
            }
        }
    }
    
    // 栈中存储括号的信息，包括字符、偏移量和颜色
    private static class BracketInfo {
        char character;
        int offset;
        TextAttributesKey color;
        
        BracketInfo(char character, int offset, TextAttributesKey color) {
            this.character = character;
            this.offset = offset;
            this.color = color;
        }
    }
    
    // 预定义的彩虹颜色，用于括号高亮
    private static final TextAttributesKey[] RAINBOW_COLORS = {
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_1", new TextAttributes(
                    VARIABLE_ORANGE, // 亮色和暗色主题的橙色
                    null, null, null, Font.PLAIN)),
            
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_2", new TextAttributes(
                    VARIABLE_GREEN, // 亮色和暗色主题的绿色
                    null, null, null, Font.PLAIN)),
            
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_3", new TextAttributes(
                    VARIABLE_CYAN, // 亮色和暗色主题的青色
                    null, null, null, Font.PLAIN)),
            
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_4", new TextAttributes(
                    VARIABLE_BLUE, // 亮色和暗色主题的蓝色
                    null, null, null, Font.PLAIN)),
            
            TextAttributesKey.createTextAttributesKey("RAINBOW_COLOR_5", new TextAttributes(
                    VARIABLE_PINK, // 亮色和暗色主题的品红色
                    null, null, null, Font.PLAIN)),
    };
    
    // 根据栈的深度返回对应的颜色
    private static TextAttributesKey getColor(int level) {
        // 确保栈深度不会超过颜色数，防止数组越界
        return RAINBOW_COLORS[level % RAINBOW_COLORS.length];
    }

}
