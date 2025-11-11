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
import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class JmteSyntaxHighlighter extends SyntaxHighlighterBase {

    static final TextAttributesKey[] BAD_CHAR_KEYS = keys("JMTE_BAD_CHARACTER", BAD_CHARACTER);
    static final TextAttributesKey[] MARKUP_KEYS = keys("JMTE_MARKUP", KEYWORD);
    static final TextAttributesKey[] OPERATION_KEYS = keys("JMTE_OPERATION", OPERATION_SIGN);
    static final TextAttributesKey[] DOT_KEYS = keys("JMTE_DOT", DOT);
    static final TextAttributesKey[] KEYWORD_KEYS = keys("JMTE_KEYWORD", KEYWORD);
    static final TextAttributesKey[] IDENTIFIER_KEYS = keys("JMTE_IDENTIFIER", IDENTIFIER);
    static final TextAttributesKey[] STRING_KEYS = keys("JMTE_STRING", STRING);
    static final TextAttributesKey[] USER_DEFINED_KEYS = keys("JMTE_USER_DEFINED", MARKUP_ENTITY);
    static final TextAttributesKey[] COMMENT_KEYS = keys("JMTE_COMMENT", BLOCK_COMMENT);
    static final TextAttributesKey[] WHITESPACE_KEYS = keys("JMTE_WHITESPACE", TEMPLATE_LANGUAGE_COLOR);
    static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new JmteLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        TextAttributesKey[] keys = getTextAttributesKeys(tokenType);
        if (keys != null) return keys;
        return EMPTY_KEYS;
    }

    @Nullable private static TextAttributesKey[] getTextAttributesKeys(IElementType tokenType) {
        if (tokenType.equals(TokenType.WHITE_SPACE)) {
            return WHITESPACE_KEYS;
        }
        if (tokenType.equals(JmteTypes.DOT_TOKEN)) {
            return DOT_KEYS;
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
        if (JmteTokenSets.STRING_LITERALS.contains(tokenType)) {
            return STRING_KEYS;
        }
        if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        }
        if (tokenType.equals(JmteTypes.USER_DEFINED_TOKEN)) {
            return USER_DEFINED_KEYS;
        }

        return EMPTY_KEYS;
    }

    private static TextAttributesKey[] keys(String externalName, TextAttributesKey textAttributesKey) {
        return new TextAttributesKey[]{TEMPLATE_LANGUAGE_COLOR, createTextAttributesKey(externalName, textAttributesKey)};
    }

}
