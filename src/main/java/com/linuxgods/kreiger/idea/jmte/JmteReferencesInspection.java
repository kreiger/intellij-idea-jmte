package com.linuxgods.kreiger.idea.jmte;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.linuxgods.kreiger.idea.jmte.psi.JmteReferenceExpression;
import com.linuxgods.kreiger.idea.jmte.psi.JmteVisitor;
import org.jetbrains.annotations.NotNull;

import static com.intellij.codeInspection.ProblemHighlightType.*;

public class JmteReferencesInspection extends LocalInspectionTool {
    @Override public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JmteVisitor() {
            @Override public void visitReferenceExpression(@NotNull JmteReferenceExpression o) {
                super.visitReferenceExpression(o);
                if (null != o.getQualifierExpression()) return;
                PsiReference reference = o.getIdentifier().getReference();
                if (reference == null || reference.resolve() != null) return;
                holder.registerProblem(o, "Unresolved reference", LIKE_UNKNOWN_SYMBOL);
            }
        };
    }
}
