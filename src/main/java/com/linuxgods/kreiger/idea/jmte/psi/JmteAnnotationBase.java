package com.linuxgods.kreiger.idea.jmte.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.source.tree.ChangeUtil;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class JmteAnnotationBase extends ASTWrapperPsiElement implements JmteIdentifier, PsiNameIdentifierOwner {
    public JmteAnnotationBase(@NotNull ASTNode node) {
        super(node);
    }

    @Override public @Nullable PsiElement getNameIdentifier() {
        return getIdentifier();
    }

    @Override public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
        ASTNode firstChildNode = this.getNode().getFirstChildNode();
        if (firstChildNode == null) {
            return this;
        } else if (!(firstChildNode instanceof LeafElement)) {
            return this;
        } else {
            LeafElement oldLeaf = (LeafElement)firstChildNode;
            LeafElement newLeaf = ChangeUtil.copyLeafWithText(oldLeaf, name);
            oldLeaf.getTreeParent().replaceChild(oldLeaf, newLeaf);
            return this;
        }
    }

    public abstract JmteIdentifier getIdentifier();
}
