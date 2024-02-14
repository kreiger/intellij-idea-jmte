package com.linuxgods.kreiger.idea.jmte;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JmteEditorActionHandler extends EditorActionHandler {
    @Override
    protected boolean isEnabledForCaret(@NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
        System.out.println("isEnabledForCaret "+editor);
        return super.isEnabledForCaret(editor, caret, dataContext);
    }

    @Override protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        System.out.println("doExecute "+editor);
        super.doExecute(editor, caret, dataContext);
    }

    @Override public boolean executeInCommand(@NotNull Editor editor, DataContext dataContext) {
        System.out.println("executeInCommand "+editor);
        return super.executeInCommand(editor, dataContext);
    }
}
