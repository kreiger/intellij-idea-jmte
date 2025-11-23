package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lang.impl.TokenSequence;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.TokenList;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateDataModifications;
import com.intellij.psi.tree.IElementType;
import com.intellij.velocity.psi.VtlElementTypes;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTypes;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.linuxgods.kreiger.idea.jmte.JmteFileElementTypes.OUTER_ELEMENT_TYPE;
import static com.linuxgods.kreiger.idea.jmte.psi.JmteTypes.*;

class JmteTemplateDataElementType extends TemplateDataElementType {

    public JmteTemplateDataElementType() {
        super("JMTE_TEMPLATE_DATA", JmteLanguage.INSTANCE, TEMPLATE_DATA_TOKEN, OUTER_ELEMENT_TYPE);
    }

    @Override
    protected CharSequence createTemplateText(@NotNull CharSequence sourceCode, @NotNull Lexer baseLexer, @NotNull RangeCollector rangeCollector) {
        TokenList tokens = TokenSequence.performLexing(sourceCode, baseLexer);
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < tokens.getTokenCount(); ++i) {
            if (!tokens.hasType(i, TEMPLATE_DATA_TOKEN)) {
                result.append(StringUtils.repeat(' ', tokens.getTokenText(i).length()));
                continue;
            }
            CharSequence tokenText = tokens.getTokenText(i);
            boolean escaped = false;
            for (int j = 0; j < tokenText.length(); j++) {
                char c = tokenText.charAt(j);
                if (c == '\\' && !escaped) {
                    result.append(' ');
                    escaped = true;
                } else {
                    result.append(c);
                    escaped = false;
                }
            }
        }
        return result;
    }
}
