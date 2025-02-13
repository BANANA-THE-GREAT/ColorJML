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
    private static final Map<String, Pattern> KEYWORD_PATTERN = new HashMap<>();
    private static final Map<String, Pattern> EXPRESSION_PATTERN = new HashMap<>();
    private static final Map<String, Pattern> FUNC_BEHAVIOR_PATTERN = new HashMap<>();
    private static final Map<String, TextAttributesKey> FUNC_BEHAVIOR_COLOR = new HashMap<>();
    private static final Map<Character, Character> bracketPairs = new HashMap<>();
    
    static {
        HashSet<String> keywords = new HashSet<>();
        Collections.addAll(keywords, "public", "private", "protected",
                "instance", "model", "non_null", "invariant", "pure", "also",
                "void", "int", "String", "boolean", "null",
                "safe");
        for (String keyword : keywords) {
            KEYWORD_PATTERN.put(keyword, Pattern.compile("\\b" + keyword + "\\b"));
        }
        
        HashSet<String> expressions = new HashSet<>();
        Collections.addAll(expressions, "forall", "exists", "sum", "product", "max", "min", "num_of",
                "result", "old", "not_assigned", "not_modified", "nonnullelements", "type", "typeof");
        for (String expression : expressions) {
            EXPRESSION_PATTERN.put(expression, Pattern.compile("\\\\" + expression + "\\b"));
        }
        
        Map<String, JBColor> name2color = new HashMap<>();
        name2color.put("normal_behavior", VARIABLE_GREEN);
        name2color.put("assignable", VARIABLE_BLUE);
        name2color.put("modifiable", VARIABLE_BLUE);
        name2color.put("exceptional_behavior", JBColor.RED);
        name2color.put("requires", VARIABLE_GREEN);
        name2color.put("ensures", VARIABLE_CYAN);
        name2color.put("signals", JBColor.RED);
        name2color.put("signals_only", JBColor.RED);
        for (String name : name2color.keySet()) {
            FUNC_BEHAVIOR_PATTERN.put(name, Pattern.compile("\\b" + name + "\\b"));
            FUNC_BEHAVIOR_COLOR.put(
                    name,
                    TextAttributesKey.createTextAttributesKey(
                            name, new TextAttributes(
                                    name2color.get(name), null, null, null, Font.BOLD
                            )
                    )
            );
        }
        
        // 定义括号对
        bracketPairs.put('(', ')');
        bracketPairs.put('{', '}');
        bracketPairs.put('[', ']');
        bracketPairs.put(')', '(');
        bracketPairs.put('}', '{');
        bracketPairs.put(']', '[');
    }
    
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
        for (String funcBehavior : FUNC_BEHAVIOR_PATTERN.keySet()) {
            Pattern pattern = FUNC_BEHAVIOR_PATTERN.get(funcBehavior);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                TextRange range = TextRange.from(startOffset + matcher.start(), funcBehavior.length());
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(FUNC_BEHAVIOR_COLOR.get(funcBehavior))
                        .create();
            }
        }
    }
    
    private void highlightExpressions(String text, int startOffset, AnnotationHolder holder) {
        for (String expr : EXPRESSION_PATTERN.keySet()) {
            Pattern pattern = EXPRESSION_PATTERN.get(expr);
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
        for (String keyword : KEYWORD_PATTERN.keySet()) {
            Pattern pattern = KEYWORD_PATTERN.get(keyword);
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
