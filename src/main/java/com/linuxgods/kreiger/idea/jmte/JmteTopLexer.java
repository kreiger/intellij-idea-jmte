package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JmteTopLexer extends LexerBase {
    public static final IElementType EXPRESSION_TOKEN = new IElementType("EXPRESSION_TOKEN", JmteLanguage.INSTANCE);

    public static final int TEMPLATE_CONTENT_STATE = 0;
    public static final int EXPRESSION_STATE = 1;
    private final String startToken;
    private final String endToken;
    private final boolean useEscaping;

    private CharSequence buffer;
    private int state;
    private IElementType tokenType;
    private int tokenStart;
    private int tokenEnd;
    private int bufferEnd;
    private int startTokenIndex = -1;
    private int endTokenIndex = -1;

    public JmteTopLexer() {
        this("${", "}", true);
    }

    public JmteTopLexer(String startToken, String endToken, boolean useEscaping) {
        this.startToken = startToken;
        this.endToken = endToken;
        this.useEscaping = useEscaping;
    }

    @Override public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferEnd = endOffset;
        this.tokenStart = startOffset;
        this.tokenEnd = startOffset;
        this.state = initialState;
        advance();
    }

    @Override public void advance() {
        tokenStart = tokenEnd;
        if (tokenStart >= bufferEnd) {
            tokenType = null;
            return;
        }
        switch (state) {
            case TEMPLATE_CONTENT_STATE:
                if (startTokenIndex == -1) {
                    this.startTokenIndex = indexOf(startToken, tokenStart);
                    if (startTokenIndex == -1) {
                        tokenType = JmteTypes.TEMPLATE_DATA_TOKEN;
                        tokenEnd = bufferEnd;
                        return;
                    }
                }
                if (endTokenIndex == -1) {
                    this.endTokenIndex = indexOf(endToken, startTokenIndex + startToken.length());
                    if (endTokenIndex == -1) {
                        tokenType = JmteTypes.TEMPLATE_DATA_TOKEN;
                        tokenEnd = bufferEnd;
                        return;
                    }
                }
                if (startTokenIndex == tokenStart) {
                    tokenType = JmteTypes.START_TOKEN;
                    tokenEnd = startTokenIndex + startToken.length();
                    startTokenIndex = -1;
                    state = EXPRESSION_STATE;
                } else {
                    tokenType = JmteTypes.TEMPLATE_DATA_TOKEN;
                    tokenEnd = startTokenIndex;
                }
                return;
            case EXPRESSION_STATE:
                if (endTokenIndex == tokenStart) {
                    tokenType = JmteTypes.END_TOKEN;
                    tokenEnd = endTokenIndex + endToken.length();
                    endTokenIndex = -1;
                    state = TEMPLATE_CONTENT_STATE;
                } else {
                    tokenType = EXPRESSION_TOKEN;
                    tokenEnd = endTokenIndex;
                }

        }
    }

    private int indexOf(String token, int fromIndex) {
        if (!useEscaping) return indexOf(buffer, token, fromIndex, bufferEnd);
        return indexOfUnescaped(token, fromIndex);
    }

    private int indexOfUnescaped(String token, int fromIndex) {
        int next = fromIndex;
        int index;
        do {
            index = indexOf(buffer, token, next, bufferEnd);
            next = index + 1;
        } while (index != -1 && isEscaped(index, fromIndex));
        return index;
    }

    private boolean isEscaped(int index, int fromIndex) {
        return switch (index - fromIndex) {
            case 0 -> false;
            case 1 -> buffer.charAt(index - 1) == '\\';
            default -> buffer.charAt(index - 1) == '\\' && buffer.charAt(index - 2) != '\\';
        };
    }

    private static int indexOf(CharSequence buffer, String token, int fromIndex, int toIndex) {
        next: for (int i = fromIndex; i <= toIndex - token.length(); i++) {
            for (int j = 0; j < token.length(); j++) {
                if (buffer.charAt(i + j) != token.charAt(j)) {
                    continue next;
                }
            }
            return i;
        }
        return -1;
    }

    @Override public int getState() {
        return state;
    }

    @Override public @Nullable IElementType getTokenType() {
        return tokenType;
    }

    @Override public int getTokenStart() {
        return tokenStart;
    }

    @Override public int getTokenEnd() {
        return tokenEnd;
    }

    @Override public @NotNull CharSequence getBufferSequence() {
        return buffer;
    }

    @Override public int getBufferEnd() {
        return bufferEnd;
    }
}
