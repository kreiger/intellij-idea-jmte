package com.linuxgods.kreiger.idea.jmte;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.ProcessingContext;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class JmteCompletionContributor extends CompletionContributor {
    public JmteCompletionContributor() {
        extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement(JmteTypes.START_TOKEN)), new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                completionResultSet.addElement(LookupElementBuilder.create("--").withTailText("}"));
                completionResultSet.addElement(LookupElementBuilder.create("if").withTailText("}"));
                completionResultSet.addElement(LookupElementBuilder.create("foreach").withTailText("}"));

                PsiElement position = completionParameters.getPosition();
                if (position instanceof LeafPsiElement leaf) {
                    completionResultSet.addElement(LookupElementBuilder.create(leaf.getElementType()));
                }
            }

        });
    }
}
