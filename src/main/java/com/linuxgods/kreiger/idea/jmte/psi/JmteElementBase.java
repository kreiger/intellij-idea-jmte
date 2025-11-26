package com.linuxgods.kreiger.idea.jmte.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class JmteElementBase extends ASTWrapperPsiElement {
    public JmteElementBase(@NotNull ASTNode node) {
        super(node);
    }
}
