package com.linuxgods.kreiger.idea.jmte;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.command.undo.BasicUndoableAction;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.linuxgods.kreiger.idea.jmte.psi.JmteExpression;
import com.linuxgods.kreiger.idea.jmte.psi.JmteIdentifier;
import com.linuxgods.kreiger.idea.jmte.psi.JmteVisitor;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInspection.ProblemHighlightType.LIKE_UNKNOWN_SYMBOL;

public class JmteReferencesInspection extends LocalInspectionTool {
    @Override public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JmteVisitor() {
            @Override public void visitExpression(@NotNull JmteExpression o) {
                super.visitExpression(o);
                if (null != o.getQualifier()) return;
                JmteIdentifier identifier = o.getIdentifier();
                PsiReference reference = identifier.getReference();
                if (reference == null || reference.resolve() != null) return;
                String name = identifier.getText();
                PsiFile psiFile = identifier.getContainingFile();
                VirtualFile file = psiFile.getVirtualFile();
                holder.registerProblem(o, "Unresolved reference", LIKE_UNKNOWN_SYMBOL, new LocalQuickFix() {
                    @Override public boolean startInWriteAction() {
                        return false;
                    }

                    @Override public @IntentionFamilyName @NotNull String getFamilyName() {
                        return "Choose type of JMTE identifier";
                    }

                    @Override
                    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
                        TreeClassChooserFactory classChooserFactory = TreeClassChooserFactory.getInstance(project);
                        TreeClassChooser chooser = classChooserFactory.createProjectScopeChooser("Choose type of " + name);
                        chooser.showDialog();
                        PsiClass selected = chooser.getSelected();
                        String fqn = selected.getQualifiedName();
                        JmteTypesPersistentStateComponent jmteTypes = JmteTypesPersistentStateComponent.getInstance(project);
                        String oldFqn = jmteTypes.setFqn(file, name, fqn);
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
                                        jmteTypes.setFqn(file, name, fqn);
                                        DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
                                    }
                                }
                        );
                    }
                });
            }
        };
    }
}
