package com.linuxgods.kreiger.idea.jmte;

import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
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
        next(WHITE_SPACE, " ");
        next(IDENTIFIER_TOKEN, "test");
        next(WHITE_SPACE, " ");
        next(END_KEYWORD_TOKEN, "end");
        end();
    }

    @Test
    void testIfNegated() {
        start("if ! test ", "end");
        next(IF_KEYWORD_TOKEN, "if");
        next(WHITE_SPACE, " ");
        next(NOT_TOKEN, "!");
        next(BAD_CHARACTER, " ");
        next(IDENTIFIER_TOKEN, "test");
        next(WHITE_SPACE, " ");
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
    void testWhitespaceNonComment() {
        start(" -- ");
        next(WHITE_SPACE, " ");
        next(IDENTIFIER_TOKEN, "--");
        next(WHITE_SPACE, " ");
        end();
    }

    @Test
    void testForeach() {
        start("foreach item items  , ", "end");
        next(FOREACH_KEYWORD_TOKEN, "foreach");
        next(WHITE_SPACE, " ");
        next(IDENTIFIER_TOKEN, "item");
        next(WHITE_SPACE, " ");
        next(IDENTIFIER_TOKEN, "items");
        next(WHITE_SPACE, " ");
        next(STRING_TOKEN, " , ");
        next(END_KEYWORD_TOKEN, "end");
        end();
    }

    @Test
    void testAnnotation() {
        start("@receiver arg0\narg1");
        next(ANNOTATION_KEYWORD_TOKEN, "@");
        next(IDENTIFIER_TOKEN, "receiver");
        next(WHITE_SPACE, " ");
        next(STRING_TOKEN, "arg0\narg1");
        end();
    }

    @Test
    void testWhiteSpaceComment() {
        start(" ");
        next(WHITE_SPACE, " ");
        end();
    }

    @Test
    void testNewlineComment() {
        start("\n");
        next(WHITE_SPACE, "\n");
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
        while(lexer.getTokenType() == START_TOKEN || lexer.getTokenType() == END_TOKEN) {
            lexer.advance();
        }
    }
}
