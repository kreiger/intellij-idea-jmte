package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lang.impl.TokenSequence;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.TokenList;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateDataModifications;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.OuterLanguageElementType;
import com.intellij.velocity.psi.VtlElementTypes;
import com.linuxgods.kreiger.idea.jmte.psi.JmteElementType;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTokenSets;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTokenType;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.linuxgods.kreiger.idea.jmte.JmteFileElementTypes.OUTER_ELEMENT_TYPE;

class JmteTemplateDataElementType extends TemplateDataElementType {

    public JmteTemplateDataElementType() {
        super("JMTE_TEMPLATE_DATA", JmteLanguage.INSTANCE, JmteTypes.TEMPLATE_DATA_TOKEN, OUTER_ELEMENT_TYPE);
    }

    protected @NotNull TemplateDataModifications collectTemplateModifications(@NotNull CharSequence sourceCode, @NotNull Lexer baseLexer) {
        TemplateDataModifications modifications = new TemplateDataModifications();
        TokenList tokens = TokenSequence.performLexing(sourceCode, baseLexer);

        for(int i = 0; i < tokens.getTokenCount(); ++i) {
            if (!tokens.hasType(i, JmteFileElementTypes.TEMPLATE_DATA)) {
                boolean isInjectionStart = tokens.hasType(i - 1, null, JmteFileElementTypes.TEMPLATE_DATA)
                        && tokens.hasType(i, JmteTypes.);
                modifications.addOuterRange(tokens.getTokenRange(i), isInjectionStart);
            }
        }

        return modifications;
    }
}
