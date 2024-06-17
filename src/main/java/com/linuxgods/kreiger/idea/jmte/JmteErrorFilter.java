package com.linuxgods.kreiger.idea.jmte;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoFilter;
import com.intellij.codeInsight.highlighting.TemplateLanguageErrorFilter;
import com.intellij.lang.Language;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTokenSets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiUtilCore.getElementType;
import static com.linuxgods.kreiger.idea.jmte.psi.JmteTypes.END_TOKEN;
import static com.linuxgods.kreiger.idea.jmte.psi.JmteTypes.START_TOKEN;

public class JmteErrorFilter extends TemplateLanguageErrorFilter implements HighlightInfoFilter {
    public JmteErrorFilter() {
        super(JmteTokenSets.MARKUP, JmteFileViewProvider.class);
    }

    @Override protected boolean isKnownSubLanguage(@NotNull Language language) {
        return !language.isKindOf(JmteLanguage.INSTANCE);
    }

    @Override public boolean shouldHighlightErrorElement(@NotNull PsiErrorElement element) {
        return super.shouldHighlightErrorElement(element);
    }

    @Override public boolean accept(@NotNull HighlightInfo highlightInfo, @Nullable PsiFile file) {
        if ("SpellCheckingInspection".equals(highlightInfo.getInspectionToolId())) {
            return true;
        }
        return file == null || !isNearTemplateExpressions(highlightInfo, file);
    }

    private boolean isNearTemplateExpressions(@NotNull HighlightInfo highlightInfo, @NotNull PsiFile file) {
        if (this.isNearTemplateExpressions(file, highlightInfo.startOffset, highlightInfo.endOffset)) {
            return true;
        }
        FileViewProvider viewProvider = file.getViewProvider();
        return START_TOKEN == getElementType(viewProvider.findElementAt(highlightInfo.startOffset, viewProvider.getBaseLanguage()))
            || END_TOKEN == getElementType(viewProvider.findElementAt(highlightInfo.endOffset - 1, viewProvider.getBaseLanguage()));
    }

    @Override protected boolean isTemplateViewProvider(FileViewProvider viewProvider) {
        return viewProvider.getBaseLanguage().isKindOf(JmteLanguage.INSTANCE);
    }
}
