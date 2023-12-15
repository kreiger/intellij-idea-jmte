package com.linuxgods.kreiger.idea.jmte.psi;

import com.linuxgods.kreiger.idea.jmte.JmteFileType;
import com.linuxgods.kreiger.idea.jmte.JmteLanguage;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

public class JmteFile extends PsiFileBase {
    public JmteFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, JmteLanguage.INSTANCE);
    }

    @Override public @NotNull FileType getFileType() {
        return JmteFileType.INSTANCE;
    }

    @Override public String toString() {
        return "JMTE file: "+getName();
    }
}
