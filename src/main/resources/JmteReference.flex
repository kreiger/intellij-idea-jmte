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
    private final static Logger LOGGER = LoggerFactory.getLogger("com.linuxgods.kreiger.idea.jmte._JmteReferenceLexer");

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

%class _JmteReferenceLexer
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

%state DEFAULT_VALUE
%state ARRAY_INDEX

%%

<YYINITIAL> {
    {WHITE_SPACE}                             { return TokenType.BAD_CHARACTER; }
    "."                                       { return JmteTypes.DOT_TOKEN; }
    "["                                       { yybegin(ARRAY_INDEX); return JmteTypes.LEFT_BRACKET_TOKEN; }
    "("                                       { yybegin(DEFAULT_VALUE); return JmteTypes.LEFT_PAREN_TOKEN; }
    (\\[^]|[^\[.,(\s])+                       { return JmteTypes.IDENTIFIER_TOKEN; }
}

<ARRAY_INDEX> {
    [lL][aA][sS][tT]                          { return JmteTypes.LAST_INDEX_KEYWORD_TOKEN; }
    \d+                                       { return JmteTypes.INTEGER_LITERAL_TOKEN; }
    ","                                       { return JmteTypes.COMMA_TOKEN; }
    "]"                                       { yybegin(YYINITIAL); return JmteTypes.RIGHT_BRACKET_TOKEN; }
}

<DEFAULT_VALUE> {
    ")"                                       { yybegin(YYINITIAL); return JmteTypes.RIGHT_PAREN_TOKEN; }
    (\\[^]|[^)])+                             { return JmteTypes.TEMPLATE_DATA_TOKEN; }
}

[^]                                           {
                                                System.out.println(yystate()+" "+yyline+" "+yycolumn+">"+yytext()+"<"+HexFormat.of().formatHex(yytext().toString().getBytes())+"\n");
                                                return TokenType.BAD_CHARACTER;
                                              }