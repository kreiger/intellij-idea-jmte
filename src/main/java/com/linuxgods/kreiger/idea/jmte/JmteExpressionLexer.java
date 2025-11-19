package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.LayeredLexer;

public class JmteExpressionLexer extends LayeredLexer {
    public JmteExpressionLexer() {
        super(new FlexAdapter(new _JmteExpressionLexer(null)));
        registerLayer(new FlexAdapter(new _JmteReferenceLexer(null)), _JmteExpressionLexer.STRING_REFERENCE_TOKEN);
    }
}
