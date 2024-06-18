package com.linuxgods.kreiger.idea.jmte;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorBundle;
import com.intellij.openapi.file.exclude.OverrideFileTypeManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.fileTypes.ex.FileTypeManagerEx;
import com.intellij.openapi.fileTypes.impl.FileTypeAssocTable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.templateLanguages.TemplateDataLanguageConfigurable;
import com.intellij.psi.templateLanguages.TemplateDataLanguagePatterns;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import com.linuxgods.kreiger.idea.jmte.psi.JmteFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.jetbrains.jps.model.java.JavaResourceRootType.*;

public class JmteEditorNotificationProvider implements EditorNotificationProvider {
    private static final Key<Boolean> DISABLE_NOTIFICATION = Key.create("jmte.file.type.notification.disable");
    public static final Logger LOGGER = LoggerFactory.getLogger(JmteEditorNotificationProvider.class);

    @Override
    public @NotNull Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        if (PropertiesComponent.getInstance().isTrueValue(DISABLE_NOTIFICATION.toString())) {
            return CONST_NULL;
        }
        if (file.getFileType() instanceof JmteFileType) return CONST_NULL;
        if (!(file.getFileType() instanceof LanguageFileType fileType)) return CONST_NULL;
        VirtualFile parent = file.getParent();
        if (parent == null) return CONST_NULL;
        if (!underResourceRoot(project, parent)) return CONST_NULL;
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
                TemplateDataLanguagePatterns templateDataLanguagePatterns = TemplateDataLanguagePatterns.getInstance();
                FileTypeAssocTable<Language> assocTable = templateDataLanguagePatterns.getAssocTable();
                LOGGER.warn("assocTable {}", assocTable.findAssociatedFileType(file.getName()));
                assocTable.addAssociation(new ExactFileNameMatcher(file.getName()), fileType.getLanguage());
                templateDataLanguagePatterns.setAssocTable(assocTable);
                TemplateDataLanguageConfigurable configurable = new TemplateDataLanguageConfigurable(project);
                ShowSettingsUtil.getInstance().editConfigurable(project, configurable, () -> {
                    configurable.selectFile(file);
                });

            });
            List<VirtualFile> otherChildren = findSiblingFilesOfType(parent, file, fileType);
            if (!otherChildren.isEmpty()) {
                panel.createActionLabel("Override type for all " + fileType.getDisplayName() + " files in " + file.getParent().getName(), () -> {
                    for (VirtualFile child : file.getParent().getChildren()) {
                        if (child.getFileType() == fileType) {
                            OverrideFileTypeManager.getInstance().addFile(child, JmteFileType.INSTANCE);
                        }
                    }
                });
            }

            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            fileTypeManager.getAssociations(JmteFileType.INSTANCE).forEach((FileNameMatcher association) -> {
                if (association instanceof ExtensionFileNameMatcher extMatcher) {
                    String ext = "."+extMatcher.getExtension();
                    if (!file.getName().endsWith(ext)) {
                        String newName = file.getName() + ext;
                        panel.createActionLabel("Rename file to "+newName, () -> {
                            try {
                                WriteAction.run(() -> {
                                    file.rename(JmteEditorNotificationProvider.this, newName);
                                });
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            });
            String ext = file.getExtension();
            if (ext != null) {
                if (fileTypeManager.getFileTypeByExtension(ext) instanceof UnknownFileType) {
                    panel.createActionLabel("Associate '"+ext+"' extension",
                            () -> ApplicationManager.getApplication().runWriteAction(
                                    () -> fileTypeManager.associatePattern(JmteFileType.INSTANCE, "*."+ext)));
                }
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

    private static @NotNull List<VirtualFile> findSiblingFilesOfType(VirtualFile parent, VirtualFile file, LanguageFileType fileType) {
        List<VirtualFile> otherChildren = Stream.of(parent.getChildren())
                .filter(child -> !file.equals(child))
                .filter(child -> child.getFileType() == fileType)
                .toList();
        return otherChildren;
    }

    private static boolean underResourceRoot(@NotNull Project project, @NotNull VirtualFile file) {
        ProjectFileIndex projectFileIndex = ProjectFileIndex.getInstance(project);
        Module module = projectFileIndex.getModuleForFile(file);
        if (module == null) return false;
        ContentEntry[] contentEntries = ModuleRootManager.getInstance(module).getContentEntries();
        for (ContentEntry contentEntry : contentEntries) {
            for (SourceFolder sourceFolder : contentEntry.getSourceFolders(Set.of(RESOURCE, TEST_RESOURCE))) {
                VirtualFile resourcesRoot = sourceFolder.getFile();
                if (resourcesRoot == null) continue;
                if (VfsUtilCore.isAncestor(resourcesRoot, file, true)) {
                    return true;
                }
            }
        }
        return false;
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
        if (language == XMLLanguage.INSTANCE) return true;
        if (language == HTMLLanguage.INSTANCE) return true;
        if (language instanceof JsonLanguage) return true;
        if (language instanceof PlainTextLanguage) return true;
        return false;
    }
}
