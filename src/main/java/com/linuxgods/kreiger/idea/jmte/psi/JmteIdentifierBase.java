package com.linuxgods.kreiger.idea.jmte.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.linuxgods.kreiger.idea.jmte.JmteTypesPersistentStateComponent;
import groovyjarjarantlr4.v4.misc.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

import static com.intellij.psi.PsiModifier.PUBLIC;

public class JmteIdentifierBase extends ASTWrapperPsiElement implements JmteIdentifier {
    public JmteIdentifierBase(@NotNull ASTNode node) {
        super(node);
    }

    public Optional<PsiClass> getPsiClass() {
        if (!(getParent() instanceof JmteExpression e) || e.getExpression() != null) {
            return Optional.empty();
        }

        Project project = getProject();
        var jmteTypes = JmteTypesPersistentStateComponent.getInstance(project);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        return jmteTypes.getFqn(this.getContainingFile().getVirtualFile(), getText())
                        .map(fqn -> javaPsiFacade.findClass(fqn, GlobalSearchScopes.projectProductionScope(project)));

    }

    @Override public PsiReference getReference() {
        if (getParent() instanceof JmteExpression e && e.getExpression() == null) {
            return new PsiReferenceBase<>(this, TextRange.from(0, getTextLength())) {
                @Override public PsiElement resolve() {
                    return getPsiClass().orElse(null);
                }
            };
        }

        return new PsiPolyVariantReferenceBase<>(this, TextRange.from(0, getTextLength()), true) {

            @Override public @NotNull TextRange getAbsoluteRange() {
                return super.getAbsoluteRange();
            }

            @Override public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
                Project project = getProject();
                PsiShortNamesCache shortNamesCache = PsiShortNamesCache.getInstance(project);
                String name = getText();
                String suffix = Utils.capitalize(name);
                GlobalSearchScope scope = GlobalSearchScopes.projectProductionScope(project);
                PsiMethod[] getters = shortNamesCache.getMethodsByName("get" + suffix, scope);
                PsiMethod[] isGetters = shortNamesCache.getMethodsByName("is" + suffix, scope);

                Stream<PsiField> fields = Stream.of(shortNamesCache.getFieldsByName(name, scope))
                        .filter(f -> f.hasModifierProperty(PUBLIC));
                Stream<PsiMethod> methods = Stream.concat(Stream.of(getters), Stream.of(isGetters))
                        .filter(m -> m.getModifierList().hasModifierProperty(PUBLIC))
                        .filter(m -> m.getParameterList().isEmpty());

                return Stream.concat(fields, methods)
                        .map(PsiElementResolveResult::new)
                        .toArray(ResolveResult[]::new);
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
