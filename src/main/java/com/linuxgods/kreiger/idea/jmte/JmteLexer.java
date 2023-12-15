package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lexer.FlexAdapter;
public class JmteLexer extends FlexAdapter {
    public JmteLexer() {
        super(new _JmteLexer(null));
    }
}
