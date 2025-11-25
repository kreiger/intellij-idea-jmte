package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lang.impl.TokenSequence;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.TokenList;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import org.jetbrains.annotations.NotNull;

import static com.linuxgods.kreiger.idea.jmte.JmteFileElementTypes.OUTER_ELEMENT_TYPE;
import static com.linuxgods.kreiger.idea.jmte.psi.JmteTypes.TEMPLATE_DATA_TOKEN;

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
                rangeCollector.addOuterRange(tokens.getTokenRange(i));
                //result.append(StringUtils.repeat(' ', tokens.getTokenText(i).length()));
                continue;
            }
            CharSequence tokenText = tokens.getTokenText(i);
            boolean escaped = false;
            for (int j = 0; j < tokenText.length(); j++) {
                char c = tokenText.charAt(j);
                if (c == '\\' && !escaped) {
                    rangeCollector.addOuterRange(TextRange.from(tokens.getTokenStart(i)+j, 1));
                    //result.append(' ');
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
