package com.linuxgods.kreiger.idea.jmte;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.file.exclude.OverrideFileTypeManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class JmteEditorNotificationProvider implements EditorNotificationProvider {


    @Override
    public @NotNull Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        if (!(file.getFileType() instanceof LanguageFileType fileType)) return CONST_NULL;
        Language language = fileType.getLanguage();
        if (!potentialTemplate(language)) return CONST_NULL;
        if (!looksLikeTemplate(file)) return CONST_NULL;
        Method addFile;
        try {
            addFile = OverrideFileTypeManager.class.getDeclaredMethod("addFile", VirtualFile.class, FileType.class);
            addFile.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return fileEditor -> {
            EditorNotificationPanel panel = new EditorNotificationPanel();
            panel.setText("This file looks like a JMTE template containing "+fileType.getDisplayName());
            panel.createActionLabel("Override type for this file", () -> {
                try {
                    addFile.invoke(OverrideFileTypeManager.getInstance(), file, JmteFileType.INSTANCE);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            });
            String ext = file.getExtension();
            if (ext == null) {
                return panel;
            }
            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            if (fileTypeManager.getFileTypeByExtension(ext) instanceof UnknownFileType) {
                panel.createActionLabel("Associate '"+ext+"' extension",
                        () -> ApplicationManager.getApplication().runWriteAction(
                                () -> fileTypeManager.associatePattern(JmteFileType.INSTANCE, "*."+ext)));
            }
            return panel;
        };
    }

    private boolean looksLikeTemplate(VirtualFile file) {
        try (InputStream in = file.getInputStream()) {
            boolean foundStart = false;
            int b;
            while ((b = in.read()) != -1) {
                if (b == '$' && in.read() == '{') foundStart = true;
                else if (b == '}' && foundStart) return true;
                else if (b == '\n') foundStart = false;
            }
        } catch (IOException ignored) {}
        return false;
    }

    private static boolean potentialTemplate(Language language) {
        if (language instanceof JsonLanguage) return true;
        if (language instanceof XMLLanguage) return true;
        if (language instanceof PlainTextLanguage) return true;
        return false;
    }
}
