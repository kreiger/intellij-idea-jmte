package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LayeredLexer;

public class JmteLexer extends LayeredLexer {
    public JmteLexer() {
        super(new JmteTopLexer("${", "}", true));
        registerLayer(new FlexAdapter(new _JmteExpressionLexer(null)), JmteTopLexer.EXPRESSION_TOKEN);
    }
}
