package com.linuxgods.kreiger.idea.jmte.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.intellij.psi.search.GlobalSearchScope.allScope;
import static com.intellij.psi.search.GlobalSearchScopes.*;

public class JmteIdentifierBase extends ASTWrapperPsiElement implements JmteIdentifier {
    public JmteIdentifierBase(@NotNull ASTNode node) {
        super(node);
    }

    @Override public PsiReference getReference() {
        return new PsiReferenceBase<>(this, TextRange.from(0, getTextLength()), true) {

            @Override public @NotNull TextRange getAbsoluteRange() {
                return super.getAbsoluteRange();
            }

            @Override public @Nullable PsiElement resolve() {
                Project project = getProject();
                JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
                // find getters in project by name

                PsiClass engineClass = CachedValuesManager.getManager(project).createCachedValue(() -> {
                    PsiClass aClass = javaPsiFacade.findClass("com.floreysoft.jmte.Engine", allScope(project));
                    return new CachedValueProvider.Result<>(aClass, aClass);
                }).getValue();
                if (engineClass == null) throw new RuntimeException("Could not find com.floreysoft.jmte.Engine");

                ProgressManager progressManager = ProgressManager.getInstance();

                return progressManager.runProcess(() ->
                        Arrays.stream(engineClass.findMethodsByName("transform", false))
                                .flatMap(transformMethod -> {
                                    Query<PsiReference> query = MethodReferencesSearch.search(transformMethod, projectProductionScope(project), true);
                                    return StreamSupport.stream(query.spliterator(), false);
                                })
                                .map(PsiReference::getElement)
                                .map(element -> PsiTreeUtil.findFirstParent(element, p -> p instanceof PsiMethodCallExpression))
                                .filter(Objects::nonNull)
                                .map(PsiMethodCallExpression.class::cast)
                                .flatMap(mce -> {
                                    return Stream.of(mce.getArgumentList().getExpressions())
                                            .filter(e -> e instanceof PsiReferenceExpression)
                                            .filter(e -> isMapOfStringToObject(e.getType()))
                                            .map(e -> ((PsiReferenceExpression) e).resolve())
                                            ;
                                })
                                .findFirst()
                                .orElse(null),
                        new EmptyProgressIndicator());
            }

            @Override public Object @NotNull [] getVariants() {
                return super.getVariants();
            }
        };
    }

    public static boolean isMapOfStringToObject(PsiType type) {
        if (!(type instanceof PsiClassType)) {
            return false;
        }

        PsiClassType classType = (PsiClassType) type;
        PsiClass resolvedClass = classType.resolve();

        if (resolvedClass == null || !CommonClassNames.JAVA_UTIL_MAP.equals(resolvedClass.getQualifiedName())) {
            return false;
        }

        PsiType[] typeParameters = classType.getParameters();

        if (typeParameters.length != 2) {
            return false;
        }

        PsiType keyType = typeParameters[0];
        PsiType valueType = typeParameters[1];
        return PsiType.getJavaLangString(resolvedClass.getManager(), GlobalSearchScope.allScope(resolvedClass.getProject())).equals(keyType)
                && PsiType.getJavaLangObject(resolvedClass.getManager(), GlobalSearchScope.allScope(resolvedClass.getProject())).equals(valueType);
    }
}
