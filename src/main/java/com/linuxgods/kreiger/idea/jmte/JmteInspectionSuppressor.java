package com.linuxgods.kreiger.idea.jmte;

import com.intellij.codeInspection.InspectionSuppressor;
import com.intellij.codeInspection.SuppressQuickFix;
import com.intellij.json.psi.JsonReferenceExpression;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmteInspectionSuppressor implements InspectionSuppressor {

    public static final Logger LOGGER = LoggerFactory.getLogger(JmteInspectionSuppressor.class);

    @Override public boolean isSuppressedFor(@NotNull PsiElement element, @NotNull String toolId) {
        //LOGGER.warn("isSuppressedFor {} {}", toolId, element);
        return "JsonDuplicatePropertyKeys".equals(toolId)
                || element instanceof JsonReferenceExpression && "JsonStandardCompliance".equals(toolId);
    }

    @Override
    public SuppressQuickFix @NotNull [] getSuppressActions(@Nullable PsiElement element, @NotNull String toolId) {
        return new SuppressQuickFix[0];

    }
}