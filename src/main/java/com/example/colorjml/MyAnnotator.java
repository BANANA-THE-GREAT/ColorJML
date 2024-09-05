package com.example.colorjml;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiComment;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyAnnotator implements Annotator {
    
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
            
            highlightBrackets(text, element.getTextRange().getStartOffset(), holder);
            highlightOperators(text, (PsiComment) element, holder);
            highlightKeywords(text, element.getTextRange().getStartOffset(), holder);
        }
    }
    
    private void highlightKeywords(String text, int startOffset, AnnotationHolder holder) {
        HashSet<String> keywords = new HashSet<>();
        Collections.addAll(keywords, "public", "private", "protected",
                "instance", "model", "non_null", "invariant", "pure",
                "void", "int", "String", "boolean", "null",
                "\\forall", "\\exists", "\\result");
                
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                TextRange range = TextRange.from(startOffset + matcher.start(), keyword.length());
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(MyHighlighterColors.KEYWORD)
                        .create();
            }
        }
    }
    
    private void highlightOperators(String text, PsiComment element, AnnotationHolder holder) {
        HashSet<Character> equalsAndInequality = new HashSet<>();
        Collections.addAll(equalsAndInequality, '<', '>', '=');
        HashSet<Character> operator = new HashSet<>();
        Collections.addAll(operator, '+', '-', '*', '/', '&', '|', '!', '@');
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (equalsAndInequality.contains(c) || c == '!' && text.charAt(i + 1) == '=') {
                TextRange range = TextRange.from(element.getTextRange().getStartOffset() + i, 1);
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(MyHighlighterColors.EQ_NE)
                        .create();
            } else if (operator.contains(c)) {
                TextRange range = TextRange.from(element.getTextRange().getStartOffset() + i, 1);
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(MyHighlighterColors.OP)
                        .create();
            }
        }
    }
    
    private void highlightBrackets(String text, int startOffset, AnnotationHolder holder) {
        Stack<BracketInfo> stack = new Stack<>();
        Map<Character, Character> bracketPairs = new HashMap<>();
        bracketPairs.put('(', ')');
        bracketPairs.put('{', '}');
        bracketPairs.put('[', ']');
        bracketPairs.put(')', '(');
        bracketPairs.put('}', '{');
        bracketPairs.put(']', '[');
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (bracketPairs.containsKey(c)) {
                if (c == '(' || c == '{' || c == '[') {
                    // Opening bracket
                    stack.push(new BracketInfo(c, startOffset + i, RainbowHighlighterColors.getColor(stack.size())));
                } else {
                    // Closing bracket
                    if (!stack.isEmpty() && stack.peek().character == bracketPairs.get(c)) {
                        BracketInfo openBracket = stack.pop();
                        TextRange openRange = TextRange.from(openBracket.offset, 1);
                        TextRange closeRange = TextRange.from(startOffset + i, 1);
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

}
