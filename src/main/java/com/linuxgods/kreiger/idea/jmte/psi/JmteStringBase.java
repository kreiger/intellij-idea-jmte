package com.linuxgods.kreiger.idea.jmte.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.ChangeUtil;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.linuxgods.kreiger.idea.jmte.psi.JmteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JmteStringBase extends ASTWrapperPsiElement implements PsiLanguageInjectionHost, JmteString {
    public JmteStringBase(@NotNull ASTNode node) {
        super(node);
    }

    @Override public boolean isValidHost() {
        return true;
    }

    @Override public PsiLanguageInjectionHost updateText(@NotNull String text) {
        ASTNode firstChildNode = this.getNode().getFirstChildNode();
        if (firstChildNode == null) {
            return this;
        } else if (!(firstChildNode instanceof LeafElement)) {
            return this;
        } else {
            LeafElement oldLeaf = (LeafElement)firstChildNode;
            LeafElement newLeaf = ChangeUtil.copyLeafWithText(oldLeaf, text);
            oldLeaf.getTreeParent().replaceChild(oldLeaf, newLeaf);
            return this;
        }
    }

    @Override public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return LiteralTextEscaper.createSimple(this, false);
    }
}
