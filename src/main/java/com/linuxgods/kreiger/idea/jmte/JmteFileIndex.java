package com.linuxgods.kreiger.idea.jmte;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.linuxgods.kreiger.idea.jmte.psi.JmteFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.lang.Boolean.TRUE;

public class JmteFileIndex extends ScalarIndexExtension<Boolean> {
    private static final ID<Boolean, Void> NAME = ID.create("JmteFileIndex");
    public static final @NonNls String IMPLICIT_INCLUDE_MARKER = "${-- @implicitly included }\n";

    public @NotNull ID<Boolean, Void> getName() {
        return NAME;
    }

    public static @NotNull Collection<JmteFile> getImplicitlyIncludedFiles(PsiFile targetFile) {
        Module module = ModuleUtilCore.findModuleForPsiElement(targetFile);
        if (module == null || DumbService.getInstance(module.getProject()).isDumb()) {
            return Collections.emptyList();
        }
        Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, TRUE, GlobalSearchScope.moduleScope(module));
        List<JmteFile> result = new ArrayList<>(files.size());

        for (VirtualFile virtualFile : files) {
            PsiFile psiFile = targetFile.getManager().findFile(virtualFile);
            if (psiFile instanceof JmteFile jmteFile) {
                if (psiFile.equals(targetFile)) {
                    return Collections.emptyList();
                }
                result.add(jmteFile);
            }
        }
        return result;
    }

    public @NotNull DataIndexer<Boolean, Void, FileContent> getIndexer() {
        return new DataIndexer<>() {
            public @NotNull Map<Boolean, Void> map(@NotNull FileContent inputData) {

                CharSequence text = inputData.getContentAsText();
                int markerLength = IMPLICIT_INCLUDE_MARKER.length();
                if (text.length() < markerLength || !IMPLICIT_INCLUDE_MARKER.equals(text.subSequence(0, markerLength).toString())) {
                    return Collections.emptyMap();
                }
                HashMap<Boolean, Void> map = new HashMap<>();
                map.put(Boolean.TRUE, null);
                return map;
            }
        };
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
