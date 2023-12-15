package com.linuxgods.kreiger.idea.jmte;

import com.linuxgods.kreiger.idea.jmte.psi.JmteTokenSets;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.codeInsight.highlighting.TemplateLanguageErrorFilter;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JmteErrorFilter extends TemplateLanguageErrorFilter /* implements HighlightInfoFilter */ {
    public JmteErrorFilter() {
        super(JmteTokenSets.MARKUP, JmteFileViewProvider.class);
    }

    @Override protected boolean isKnownSubLanguage(@NotNull Language language) {
        return !language.isKindOf(JmteLanguage.INSTANCE);
    }
/*
    @Override public boolean accept(@NotNull HighlightInfo highlightInfo, @Nullable PsiFile file) {
        if ("SpellCheckingInspection".equals(highlightInfo.getInspectionToolId())) {
            return true;
        }
        return file == null || !this.isNearTemplateExpressions(file, highlightInfo.startOffset, highlightInfo.endOffset);
    }
*/
}
