package com.linuxgods.kreiger.idea.jmte;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.linuxgods.kreiger.idea.jmte.psi.JmteTypes.*;
import static java.util.stream.Collectors.joining;

public class JmteExpressionLexerTest extends LexerTestBase {

    @BeforeEach
    void setUp() {
        lexer = new JmteLexer();
    }

    @Test
    void testIf() {
        start("if test ", "end");
        next(IF_KEYWORD_TOKEN, "if");
        next(IDENTIFIER_TOKEN, "test");
        next(END_KEYWORD_TOKEN, "end");
        end();
    }

    @Test
    void testComment() {
        start("-- ");
        next(COMMENT_KEYWORD_TOKEN, "--");
        next(COMMENT_TOKEN, " ");
        end();
    }

    @Test
    void testWhiteSpaceComment() {
        start(" ");
        end();
    }

    protected void start(String template) {
        start(new String[] {template});
    }
    protected void start(String... template) {
        super.start(Stream.of(template).map(t -> "${"+t+"}").collect(joining()));
        next(START_TOKEN, "${");
    }

    @Override protected void next(IElementType type, String text) {
        super.next(type, text);
        while(lexer.getTokenType() == START_TOKEN || lexer.getTokenType() == END_TOKEN || lexer.getTokenType() == WHITE_SPACE) {
            lexer.advance();
        }
    }
}
