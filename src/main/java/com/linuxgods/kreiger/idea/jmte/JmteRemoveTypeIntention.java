package com.linuxgods.kreiger.idea.jmte;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.command.undo.BasicUndoableAction;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.linuxgods.kreiger.idea.jmte.psi.JmteIdentifier;
import com.linuxgods.kreiger.idea.jmte.psi.JmteIdentifierBase;
import org.jetbrains.annotations.NotNull;

public class JmteRemoveTypeIntention implements IntentionAction {
    @Override public @IntentionName @NotNull String getText() {
        return "Remove type";
    }

    @Override public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psi) {
        if (!(psi.getViewProvider() instanceof JmteFileViewProvider viewProvider)) {
            return false;
        }

        PsiFile jmteFile = viewProvider.getCachedPsi(JmteLanguage.INSTANCE);

        PsiElement atCaret = jmteFile.findElementAt(editor.getCaretModel().getOffset());
        JmteIdentifier id = PsiTreeUtil.getParentOfType(atCaret, JmteIdentifier.class);


        return id != null && ((JmteIdentifierBase)id).getPsiClass().isPresent();
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psi) throws IncorrectOperationException {
        if (!(psi.getViewProvider() instanceof JmteFileViewProvider viewProvider)) {
            return;
        }
        PsiFile psiFile = viewProvider.getPsi(JmteLanguage.INSTANCE);

        PsiElement atCaret = psiFile.findElementAt(editor.getCaretModel().getOffset());
        JmteIdentifierBase id = PsiTreeUtil.getParentOfType(atCaret, JmteIdentifierBase.class);
        JmteTypesPersistentStateComponent jmteTypes = JmteTypesPersistentStateComponent.getInstance(project);
        VirtualFile file = psiFile.getVirtualFile();
        String name = id.getText();
        String oldFqn = jmteTypes.setFqn(file, name, null);
        DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
        UndoManager.getInstance(project).undoableActionPerformed(
                new BasicUndoableAction(file) {

                    @Override
                    public void undo() {
                        jmteTypes.setFqn(file, name, oldFqn);
                        DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
                    }

                    @Override
                    public void redo() {
                        jmteTypes.setFqn(file, name, null);
                        DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
                    }
                }
        );
    }

    @Override public boolean startInWriteAction() {
        return false;
    }

    @Override public @NotNull @IntentionFamilyName String getFamilyName() {
        return "JMTE";
    }
}
