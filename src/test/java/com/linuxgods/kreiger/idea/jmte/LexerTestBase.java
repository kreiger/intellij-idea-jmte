package com.linuxgods.kreiger.idea.jmte;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.message.ErrorEntry;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;

import java.util.List;
import java.util.Objects;

import static com.linuxgods.kreiger.idea.jmte.LexerTestBase.Token.token;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LexerTestBase {

    protected Lexer lexer;

    protected void end() {
        assertEquals("", lexer.getTokenText());
        assertNull(lexer.getTokenType());
    }

    protected void start(String template) {
        List<ErrorEntry> staticErrors = new Engine().getTemplate(template).getStaticErrors();
        if (!staticErrors.isEmpty()) throw new IllegalArgumentException(staticErrors.get(0).formattedMessage.format());
        lexer.start(template);
    }

    protected void next(IElementType type, String text) {
        assertEquals(token(type, text), token(lexer.getTokenType(), lexer.getTokenText()));
        int offset = lexer.getTokenStart();
        lexer.advance();
        if (lexer.getTokenStart() <= offset) throw new IllegalStateException();
    }

    static class Token {
        private final IElementType type;
        private final String text;
        public Token(IElementType type, String text) {
            this.type = type;
            this.text = text;
        }

        static Token token(IElementType type, String text) {
            return new Token(type, text);
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Token token1 = (Token) o;
            return Objects.equals(type, token1.type) && Objects.equals(text, token1.text);
        }

        @Override public int hashCode() {
            return Objects.hash(type, text);
        }

        @Override public String toString() {
            return type+": <"+text+">";
        }
    }
}
