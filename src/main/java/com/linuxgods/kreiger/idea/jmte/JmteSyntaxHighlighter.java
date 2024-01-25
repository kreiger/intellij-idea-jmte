package com.linuxgods.kreiger.idea.jmte;

import com.linuxgods.kreiger.idea.jmte.psi.JmteTokenSets;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.*;
import static com.intellij.openapi.editor.HighlighterColors.BAD_CHARACTER;
import static com.intellij.openapi.editor.HighlighterColors.TEXT;
import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class JmteSyntaxHighlighter extends SyntaxHighlighterBase {

    private static final TextAttributesKey[] BAD_CHAR_KEYS = keys("JMTE_BAD_CHARACTER", BAD_CHARACTER);
    private static final TextAttributesKey[] MARKUP_KEYS = keys("JMTE_MARKUP", KEYWORD);
    private static final TextAttributesKey[] OPERATION_KEYS = keys("JMTE_OPERATION", OPERATION_SIGN);
    private static final TextAttributesKey[] COMMA_KEYS = keys("JMTE_COMMA", COMMA);
    private static final TextAttributesKey[] SEMICOLON_KEYS = keys("JMTE_SEMICOLON", SEMICOLON);
    private static final TextAttributesKey[] PAREN_KEYS = keys("JMTE_PARENTHESES", PARENTHESES);
    private static final TextAttributesKey[] DOT_KEYS = keys("JMTE_DOT", DOT);
    private static final TextAttributesKey[] KEYWORD_KEYS = keys("JMTE_KEYWORD", KEYWORD);
    private static final TextAttributesKey[] IDENTIFIER_KEYS = keys("JMTE_IDENTIFIER", IDENTIFIER);
    private static final TextAttributesKey[] STRING_KEYS = keys("JMTE_STRING", STRING);
    private static final TextAttributesKey[] COMMENT_KEYS = keys("JMTE_COMMENT", BLOCK_COMMENT);
    private static final TextAttributesKey[] WHITESPACE_KEYS = keys("JMTE_WHITESPACE", TEXT);
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new JmteLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        TextAttributesKey[] parenKeys = getTextAttributesKeys(tokenType);
        if (parenKeys != null) return parenKeys;
        return EMPTY_KEYS;
    }

    @Nullable private static TextAttributesKey[] getTextAttributesKeys(IElementType tokenType) {
        if (tokenType.equals(TokenType.WHITE_SPACE)) {
            return WHITESPACE_KEYS;
        }
        if (tokenType.equals(JmteTypes.LEFT_PAREN_TOKEN) || tokenType.equals(JmteTypes.RIGHT_PAREN_TOKEN)) {
            return PAREN_KEYS;
        }
        if (tokenType.equals(JmteTypes.DOT_TOKEN)) {
            return DOT_KEYS;
        }
        if (tokenType.equals(JmteTypes.COMMA_TOKEN)) {
            return COMMA_KEYS;
        }
        if (tokenType.equals(JmteTypes.SEMI_COLON_TOKEN)) {
            return SEMICOLON_KEYS;
        }
        if (tokenType.equals(JmteTypes.EQUALS_TOKEN)) {
            return OPERATION_KEYS;
        }
        if (JmteTokenSets.MARKUP.contains(tokenType)) {
            return MARKUP_KEYS;
        }
        if (JmteTokenSets.COMMENTS.contains(tokenType)) {
            return COMMENT_KEYS;
        }
        if (JmteTokenSets.KEYWORDS.contains(tokenType)) {
            return KEYWORD_KEYS;
        }
        if (tokenType.equals(JmteTypes.IDENTIFIER_TOKEN)) {
            return IDENTIFIER_KEYS;
        }
        if (tokenType.equals(JmteTypes.STRING_TOKEN)) {
            return STRING_KEYS;
        }
        if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        }
        return EMPTY_KEYS;
    }

    private static TextAttributesKey[] keys(String externalName, TextAttributesKey textAttributesKey) {
        return new TextAttributesKey[]{TEMPLATE_LANGUAGE_COLOR, createTextAttributesKey(externalName, textAttributesKey)};
    }

}
