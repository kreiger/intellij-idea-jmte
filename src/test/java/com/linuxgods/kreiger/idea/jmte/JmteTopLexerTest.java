package com.linuxgods.kreiger.idea.jmte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.linuxgods.kreiger.idea.jmte.JmteTopLexer.EXPRESSION_TOKEN;
import static com.linuxgods.kreiger.idea.jmte.psi.JmteTypes.*;

class JmteTopLexerTest extends LexerTestBase {

    @BeforeEach
    void setUp() {
        lexer = new JmteTopLexer();
    }

    @Test
    void test() {
        start("Hello ${name} World");
        next(TEMPLATE_DATA_TOKEN, "Hello ");
        next(START_TOKEN, "${");
        next(EXPRESSION_TOKEN, "name");
        next(END_TOKEN, "}");
        next(TEMPLATE_DATA_TOKEN, " World");
        end();
    }
    @Test
    void testUnescaped() {
        start("Hello \\\\${name} World");
        next(TEMPLATE_DATA_TOKEN, "Hello \\\\");
        next(START_TOKEN, "${");
        next(EXPRESSION_TOKEN, "name");
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
        next(EXPRESSION_TOKEN, "name\\}World");
        next(END_TOKEN, "}");
        end();
    }

    @Test
    void testUnescapedEnd() {
        start("Hello ${name\\\\} World}");
        next(TEMPLATE_DATA_TOKEN, "Hello ");
        next(START_TOKEN, "${");
        next(EXPRESSION_TOKEN, "name\\\\");
        next(END_TOKEN, "}");
        next(TEMPLATE_DATA_TOKEN, " World}");
        end();
    }

    @Test
    void testIf() {
        start("Hello ${if name} World${end}");
        next(TEMPLATE_DATA_TOKEN, "Hello ");
        next(START_TOKEN, "${");
        next(EXPRESSION_TOKEN, "if name");
        next(END_TOKEN, "}");
        next(TEMPLATE_DATA_TOKEN, " World");
        next(START_TOKEN, "${");
        next(EXPRESSION_TOKEN, "end");
        next(END_TOKEN, "}");
        end();
    }
}
