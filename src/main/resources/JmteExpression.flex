package com.linuxgods.kreiger.idea.jmte;

import com.linuxgods.kreiger.idea.jmte.psi.JmteTypes;
import com.floreysoft.jmte.util.Util;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.HexFormat;
import java.util.List;
%%

%{
    private final static Logger LOGGER = LoggerFactory.getLogger("com.linuxgods.kreiger.idea.jmte._JmteLexer");

    private final Deque<Integer> states = new ArrayDeque();

    private void yypushstate(int state) {
        states.addFirst(yystate());
        yybegin(state);
    }
    private void yypopstate() {
        final int state = states.removeFirst();
        yybegin(state);
    }
%}

%class _JmteExpressionLexer
%implements FlexLexer
%unicode
%line
%column
%function advance
%type IElementType
%eof{  return;
%eof}

EOL_TOKEN=\R
LINE_WS_TOKEN=[\ \t\f]
WHITE_SPACE=({LINE_WS_TOKEN}|{EOL_TOKEN})+

%state EXPRESSION_END
%state EXPRESSION_ENDED
%state STRING_EXPRESSION
%state FORMAT
%state PREFIX
%state INFIX
%state SUFFIX
%state UNAFFIXED
%state IDENTIFIER
%state FOREACH
%state FOREACH_REFERENCE
%state FOREACH_ITEM
%state FOREACH_SEPARATOR
%state IF
%state IF_NEGATED
%state IF_CONDITION
%state IF_CONDITION_VALUE
%state IF_CONDITION_STRING
%state ANNOTATION
%state ANNOTATION_ARGUMENTS
%state COMMENT
%state NONCOMMENT
%state PARAM

%%

<YYINITIAL> {
    "--"                                     { yybegin(COMMENT); return JmteTypes.COMMENT_KEYWORD_TOKEN; }
    [^]                                      { yybegin(NONCOMMENT); yypushback(1); }
}

<NONCOMMENT> {
    "@"                                      { yybegin(ANNOTATION); return JmteTypes.ANNOTATION_KEYWORD_TOKEN; }
    "foreach"                                { yybegin(FOREACH); return JmteTypes.FOREACH_KEYWORD_TOKEN; }
    "if"                                     { yybegin(IF); return JmteTypes.IF_KEYWORD_TOKEN; }
    "elseif"                                 { yybegin(IF); return JmteTypes.ELSEIF_KEYWORD_TOKEN; }
    "else"                                   { yybegin(EXPRESSION_END); return JmteTypes.ELSE_KEYWORD_TOKEN; }
    "end"                                    { yybegin(EXPRESSION_END); return JmteTypes.END_KEYWORD_TOKEN; }
    {WHITE_SPACE}                            { return TokenType.WHITE_SPACE; }
    \w+                                      { yybegin(STRING_EXPRESSION); yypushback(yylength()); }
    \S                                       { yybegin(STRING_EXPRESSION); yypushback(1); }
}

<ANNOTATION> {
    \S+                                      { yybegin(ANNOTATION_ARGUMENTS); return JmteTypes.IDENTIFIER_TOKEN; }
}

<ANNOTATION_ARGUMENTS> {
    \s+                                      { return TokenType.WHITE_SPACE; }
    \S[^]*                                   { return JmteTypes.STRING_TOKEN; }
}

<COMMENT> {
    [^]+                                     { return JmteTypes.COMMENT_TOKEN; }
}

<EXPRESSION_END> {
    {WHITE_SPACE}                            { return TokenType.WHITE_SPACE; }
    [^]                                      { return TokenType.BAD_CHARACTER; }
}

<EXPRESSION_ENDED> {
    [^]                                      { return TokenType.BAD_CHARACTER; }
}

<IF> {
    {WHITE_SPACE}                             { return TokenType.WHITE_SPACE; }
    "!"                                       { yybegin(IF_NEGATED); return JmteTypes.NOT_TOKEN; }
    \S                                        { yybegin(IF_CONDITION); yypushback(yylength()); }
}

<IF_NEGATED> {
    {WHITE_SPACE}                             { return TokenType.BAD_CHARACTER; }
    \S                                        { yybegin(IF_CONDITION); yypushback(yylength()); }
}

<IF_CONDITION> {
    {WHITE_SPACE}                             { return TokenType.WHITE_SPACE; }
    "="                                       { yybegin(IF_CONDITION_VALUE); return JmteTypes.EQUALS_TOKEN; }
    "."                                       { return JmteTypes.DOT_TOKEN; }
    (\\[\\=.\s]|[^=.\s])+                     { return JmteTypes.IDENTIFIER_TOKEN; }
}

<IF_CONDITION_VALUE> {
    {WHITE_SPACE}                             { return TokenType.WHITE_SPACE; }
    [\"\']                                    { yybegin(IF_CONDITION_STRING); }
    [^\s][^]*                                 { return JmteTypes.STRING_TOKEN; }
}

<IF_CONDITION_STRING> {
    {WHITE_SPACE}                             { return TokenType.WHITE_SPACE; }
    [^\s][^]*                                 { return JmteTypes.STRING_TOKEN; }
}

<FOREACH> {
    {WHITE_SPACE}                             { yybegin(FOREACH_REFERENCE); return TokenType.WHITE_SPACE; }
}

<FOREACH_REFERENCE> {
    "."                                       { return JmteTypes.DOT_TOKEN; }
    (\\[\\.\s]|[^.\s])+                       { return JmteTypes.IDENTIFIER_TOKEN; }
    {WHITE_SPACE}                             { yybegin(FOREACH_ITEM); return TokenType.WHITE_SPACE; }
}

<FOREACH_ITEM> {
    (\\\s|[^\s])+                             { return JmteTypes.IDENTIFIER_TOKEN; }
    \s                                        { yybegin(FOREACH_SEPARATOR); return TokenType.WHITE_SPACE; }   
}

<FOREACH_SEPARATOR> {
    [^]+                                      { return JmteTypes.STRING_TOKEN; }
}


<STRING_EXPRESSION> {
    [^]+                                      {
                String text = yytext().toString();
                List<String> semicolon = Util.RAW_OUTPUT_MINI_PARSER.split(text, ';', 2);
                List<String> comma = Util.RAW_OUTPUT_MINI_PARSER.split(semicolon.get(0), ',', 3);
                if (comma.size() == 3) {
                    yybegin(PREFIX); yypushback(yylength());
                } else {
                    yybegin(UNAFFIXED); yypushback(yylength());
                }
           }
}

<PREFIX> {
    ","                                       { yybegin(INFIX); return JmteTypes.COMMA_TOKEN; }
    (\\[\\,]|[^,])+                           { return JmteTypes.STRING_TOKEN; }
}

<INFIX> {
    {WHITE_SPACE}                             { return TokenType.BAD_CHARACTER; }
    "."                                       { return JmteTypes.DOT_TOKEN; }
    ","                                       { yybegin(SUFFIX); return JmteTypes.COMMA_TOKEN; }
    "("                                       { yypushstate(PARAM); return JmteTypes.LEFT_PAREN_TOKEN; }
    (\\[\\.,(\s]|[^.,(\s])+                   { return JmteTypes.IDENTIFIER_TOKEN; }
}

<SUFFIX> {
    ";"                                       { yybegin(FORMAT); return JmteTypes.SEMI_COLON_TOKEN; }
    (\\[\\;]|[^;])+                           { return JmteTypes.STRING_TOKEN; }
}

<FORMAT> {
    [^(]+                                     { return JmteTypes.IDENTIFIER_TOKEN; }
    "("                                       { yypushstate(PARAM); return JmteTypes.LEFT_PAREN_TOKEN; }
}

<PARAM> {
    ")"                                       { yypopstate(); return JmteTypes.RIGHT_PAREN_TOKEN; }
    (\\[\\)]|[^)])+                           { return JmteTypes.STRING_TOKEN; }
}

<UNAFFIXED> {
    {WHITE_SPACE}                             { return TokenType.WHITE_SPACE; }
    "("                                       { yypushstate(PARAM); return JmteTypes.LEFT_PAREN_TOKEN; }
    "."                                       { return JmteTypes.DOT_TOKEN; }
    ";"                                       { yypushstate(FORMAT); return JmteTypes.SEMI_COLON_TOKEN; }
    (\\[\\(.;\s]|[^\\(.;\s])+                 { return JmteTypes.IDENTIFIER_TOKEN; }
}

[^]                                           {
                                                System.out.println(yystate()+" "+yyline+" "+yycolumn+">"+yytext()+"<"+HexFormat.of().formatHex(yytext().toString().getBytes())+"\n");
                                                return TokenType.BAD_CHARACTER;
                                              }