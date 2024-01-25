package com.linuxgods.kreiger.idea.jmte.psi;

import com.intellij.psi.tree.TokenSet;

import static com.linuxgods.kreiger.idea.jmte.psi.JmteTypes.*;

public interface JmteTokenSets {
    TokenSet COMMENTS = TokenSet.create(COMMENT_KEYWORD_TOKEN, COMMENT_TOKEN);
    TokenSet KEYWORDS = TokenSet.create(COMMENT_KEYWORD_TOKEN,FOREACH_KEYWORD_TOKEN,IF_KEYWORD_TOKEN,ELSEIF_KEYWORD_TOKEN,ELSE_KEYWORD_TOKEN,END_KEYWORD_TOKEN);
    TokenSet MARKUP = TokenSet.create(START_TOKEN, END_TOKEN);
}
