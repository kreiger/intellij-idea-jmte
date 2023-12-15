package com.linuxgods.kreiger.idea.jmte;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.TemplateLanguageFileType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class JmteFileType extends LanguageFileType implements TemplateLanguageFileType /*, FileTypeIdentifiableByVirtualFile*/ {
    public static final JmteFileType INSTANCE = new JmteFileType();

    private JmteFileType() {
        super(JmteLanguage.INSTANCE);
    }

    @Override public @NonNls @NotNull String getName() {
        return "JMTE";
    }

    @Override public @NlsContexts.Label @NotNull String getDescription() {
        return "Java Minimal Template Language";
    }

    @Override public @NlsSafe @NotNull String getDefaultExtension() {
        return "tpl";
    }

    @Override public Icon getIcon() {
        return AllIcons.Nodes.Template;
    }

    /*
    @Override public boolean isMyFileType(@NotNull VirtualFile file) {
        try (var in = file.getInputStream()) {
            int c;
            while((c = in.read()) != -1) {
                if (c == '$' && in.read() == '{') return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;

    }

     */
}
