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

%%

<YYINITIAL> {
    {WHITE_SPACE}                             { return TokenType.BAD_CHARACTER; }
    "."                                       { return JmteTypes.DOT_TOKEN; }
    "("                                       { yypushstate(DEFAULT_VALUE); return JmteTypes.LEFT_PAREN_TOKEN; }
    (\\[\\.,(\s]|[^.,(\s])+                   { return JmteTypes.IDENTIFIER_TOKEN; }
}

<DEFAULT_VALUE> {
    ")"                                       { yypopstate(); return JmteTypes.RIGHT_PAREN_TOKEN; }
    (\\[\\)]|[^)])+                           { return JmteTypes.TEMPLATE_DATA_TOKEN; }
}

[^]                                           {
                                                System.out.println(yystate()+" "+yyline+" "+yycolumn+">"+yytext()+"<"+HexFormat.of().formatHex(yytext().toString().getBytes())+"\n");
                                                return TokenType.BAD_CHARACTER;
                                              }