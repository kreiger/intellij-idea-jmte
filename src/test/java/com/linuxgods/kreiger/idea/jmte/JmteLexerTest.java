package com.linuxgods.kreiger.idea.jmte;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.message.ErrorEntry;
import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static com.linuxgods.kreiger.idea.jmte.JmteLexerTest.Token.token;
import static com.linuxgods.kreiger.idea.jmte.psi.JmteTypes.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JmteLexerTest {

    private JmteLexer lexer;

    @BeforeEach
    void setUp() {
        lexer = new JmteLexer();
    }

    @Test
    void test() {
        start("Hello ${name} World");
        next(TEMPLATE_DATA_TOKEN, "Hello ");
        next(START_TOKEN, "${");
        next(IDENTIFIER_TOKEN, "name");
        next(END_TOKEN, "}");
        next(TEMPLATE_DATA_TOKEN, " World");
        end();
    }
    @Test
    void testUnescaped() {
        start("Hello \\\\${name} World");
        next(TEMPLATE_DATA_TOKEN, "Hello \\\\");
        next(START_TOKEN, "${");
        next(IDENTIFIER_TOKEN, "name");
        next(END_TOKEN, "}");
        next(TEMPLATE_DATA_TOKEN, " World");
        end();
    }

    @Test
    void testEscapedStart() {
        start("Hello \\${name} World");
        next(TEMPLATE_DATA_TOKEN, "Hello \\${name} World");
        end();
    }

    @Test
    void testEscapedEnd() {
        start("Hello ${name\\}World}");
        next(TEMPLATE_DATA_TOKEN, "Hello ");
        next(START_TOKEN, "${");
        next(IDENTIFIER_TOKEN, "name\\}World");
        next(END_TOKEN, "}");
        end();
    }

    @Test
    void testUnescapedEnd() {
        start("Hello ${name\\\\} World}");
        next(TEMPLATE_DATA_TOKEN, "Hello ");
        next(START_TOKEN, "${");
        next(IDENTIFIER_TOKEN, "name\\\\");
        next(END_TOKEN, "}");
        next(TEMPLATE_DATA_TOKEN, " World}");
        end();
    }

    private void end() {
        assertEquals("", lexer.getTokenText());
        assertNull(lexer.getTokenType());
    }

    private void start(String template) {
        List<ErrorEntry> staticErrors = new Engine().getTemplate(template).getStaticErrors();
        if (!staticErrors.isEmpty()) throw new IllegalArgumentException(staticErrors.get(0).formattedMessage.format());
        lexer.start(template);
    }

    private void next(IElementType type, String text) {
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
