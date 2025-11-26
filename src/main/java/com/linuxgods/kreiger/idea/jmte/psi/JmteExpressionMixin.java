package com.linuxgods.kreiger.idea.jmte.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScopes;
import com.linuxgods.kreiger.idea.jmte.JmteTypesPersistentStateComponent;
import com.linuxgods.kreiger.idea.jmte.psi.impl.JmteReferenceExpressionImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class JmteExpressionMixin extends JmteReferenceExpressionImpl implements JmteExpression {

    public JmteExpressionMixin(@NotNull ASTNode node) {
        super(node);
    }

    public Optional<PsiClass> getPsiClass() {
        if (getQualifier() != null) {
            return Optional.empty();
        }

        Project project = getProject();
        var jmteTypes = JmteTypesPersistentStateComponent.getInstance(project);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        JmteIdentifier identifier = getIdentifier();
        String name = identifier.getText();
        return jmteTypes.getFqn(this.getContainingFile().getVirtualFile(), name)
                .map(fqn -> javaPsiFacade.findClass(fqn, GlobalSearchScopes.projectProductionScope(project)));

    }

}
