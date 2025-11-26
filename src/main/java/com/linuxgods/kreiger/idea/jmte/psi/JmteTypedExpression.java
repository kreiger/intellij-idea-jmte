package com.linuxgods.kreiger.idea.jmte.psi;

import com.intellij.psi.PsiClass;

import java.util.Optional;

public interface JmteTypedExpression {

    Optional<PsiClass> getPsiClass();
}
