package com.linuxgods.kreiger.idea.jmte;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulator;
import com.intellij.util.IncorrectOperationException;
import com.linuxgods.kreiger.idea.jmte.psi.JmteStringBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JmteStringElementManipulator implements ElementManipulator<JmteStringBase> {
    @Override
    public @Nullable JmteStringBase handleContentChange(@NotNull JmteStringBase element, @NotNull TextRange range, String newContent) throws IncorrectOperationException {
        return handleContentChange(element, newContent);
    }

    @Override
    public @Nullable JmteStringBase handleContentChange(@NotNull JmteStringBase element, String newContent) throws IncorrectOperationException {
        element.updateText(newContent);
        return element;
    }

    @Override public @NotNull TextRange getRangeInElement(@NotNull JmteStringBase element) {
        return new TextRange(0, element.getTextLength());
    }
}
