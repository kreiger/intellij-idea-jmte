package com.linuxgods.kreiger.idea.jmte;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorBundle;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.file.exclude.OverrideFileTypeManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class JmteEditorNotificationProvider implements EditorNotificationProvider {
    private static final Key<Boolean> DISABLE_NOTIFICATION = Key.create("jmte.file.type.notification.disable");

    @Override
    public @NotNull Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        if (PropertiesComponent.getInstance().isTrueValue(DISABLE_NOTIFICATION.toString())) {
            return CONST_NULL;
        }
        if (!(file.getFileType() instanceof LanguageFileType fileType)) return CONST_NULL;
        Language language = fileType.getLanguage();
        if (!potentialTemplate(language)) return CONST_NULL;
        VirtualFile contentRoot = ProjectFileIndex.getInstance(project).getContentRootForFile(file);
        if (contentRoot == null || contentRoot.equals(file.getParent())) return CONST_NULL;
        if (!looksLikeTemplate(file)) return CONST_NULL;

        return fileEditor -> {
            if (!(fileEditor instanceof TextEditor textEditor)) {
                return null;
            }
            Editor editor = textEditor.getEditor();
            if (Boolean.TRUE.equals(editor.getUserData(DISABLE_NOTIFICATION))) {
                return null;
            }

            EditorNotificationPanel panel = new EditorNotificationPanel();
            panel.setText("This file looks like a JMTE template containing "+fileType.getDisplayName());
            panel.createActionLabel("Override type for this file", () -> {
                OverrideFileTypeManager.getInstance().addFile(file, JmteFileType.INSTANCE);
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
            panel.createActionLabel(EditorBundle.message("notification.hide.message"), () -> {
                editor.putUserData(DISABLE_NOTIFICATION, Boolean.TRUE);
                EditorNotifications.getInstance(project).updateNotifications(file);
            });
            panel.createActionLabel(EditorBundle.message("notification.dont.show.again.message"), () -> {
                PropertiesComponent.getInstance().setValue(DISABLE_NOTIFICATION.toString(), "true");
                EditorNotifications.getInstance(project).updateAllNotifications();
            });
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
