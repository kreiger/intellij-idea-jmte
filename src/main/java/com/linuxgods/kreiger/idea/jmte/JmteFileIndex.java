package com.linuxgods.kreiger.idea.jmte;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.linuxgods.kreiger.idea.jmte.psi.JmteFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;

public class JmteFileIndex extends ScalarIndexExtension<Boolean> {
    private static final ID<Boolean, Void> NAME = ID.create("JmteFileIndex");
    public static final @NonNls String IMPLICIT_INCLUDE_MARKER = "${-- @implicitly included }\n";

    public @NotNull ID<Boolean, Void> getName() {
        return NAME;
    }

    public static @NotNull Collection<JmteFile> getImplicitlyIncludedFiles(PsiFile targetFile) {
        Module module = ModuleUtilCore.findModuleForPsiElement(targetFile);
        if (module == null || DumbService.getInstance(module.getProject()).isDumb()) {
            return emptyList();
        }
        Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, TRUE, GlobalSearchScope.moduleScope(module));
        if (files.contains(targetFile.getVirtualFile())) {
            return emptyList();
        }

        PsiManager psiManager = targetFile.getManager();
        return files.stream()
                .map(psiManager::findFile)
                .filter(JmteFile.class::isInstance)
                .map(JmteFile.class::cast)
                .toList();
    }

    public @NotNull DataIndexer<Boolean, Void, FileContent> getIndexer() {
        return inputData -> {
            if (startsWith(inputData.getContentAsText(), IMPLICIT_INCLUDE_MARKER)) {
                return Collections.emptyMap();
            }
            HashMap<Boolean, Void> map = new HashMap<>();
            map.put(Boolean.TRUE, null);
            return map;
        };
    }

    private static boolean startsWith(CharSequence text, @NonNls String prefix) {
        int markerLength = prefix.length();
        return text.length() < markerLength || !prefix.equals(text.subSequence(0, markerLength).toString());
    }

    public @NotNull KeyDescriptor<Boolean> getKeyDescriptor() {
        return BooleanDataDescriptor.INSTANCE;
    }

    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(JmteFileType.INSTANCE);
    }

    public boolean dependsOnFileContent() {
        return true;
    }

    public int getVersion() {
        return 1;
    }

}
