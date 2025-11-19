package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LayeredLexer;
import com.linuxgods.kreiger.idea.jmte.psi.JmteExpression;

public class JmteLexer extends LayeredLexer {
    public JmteLexer() {
        super(new JmteTopLexer("${", "}", true));
        registerLayer(new JmteExpressionLexer(), JmteTopLexer.EXPRESSION_TOKEN);
    }
}
