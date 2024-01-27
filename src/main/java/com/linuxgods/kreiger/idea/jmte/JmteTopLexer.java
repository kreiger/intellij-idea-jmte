package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JmteTopLexer extends LexerBase {
    public JmteTopLexer() {

    }

    @Override public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {

    }

    @Override public int getState() {
        return 0;
    }

    @Override public @Nullable IElementType getTokenType() {
        return null;
    }

    @Override public int getTokenStart() {
        return 0;
    }

    @Override public int getTokenEnd() {
        return 0;
    }

    @Override public void advance() {

    }

    @Override public @NotNull CharSequence getBufferSequence() {
        return null;
    }

    @Override public int getBufferEnd() {
        return 0;
    }
}
