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
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.templateLanguages.TemplateDataLanguageConfigurable;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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

            var panel = new EditorNotificationPanel();
            panel.setText("This file looks like a JMTE template containing " + fileType.getDisplayName());


            panel.createActionLabel("Override type...", () -> {
                Runnable overrideType = new Runnable() {
                    @Override public void run() {
                        OverrideFileTypeManager.getInstance().addFile(file, JmteFileType.INSTANCE);
                        TemplateDataLanguageMappings.getInstance(project).setMapping(file, fileType.getLanguage());
                    }

                    @Override public String toString() {
                        return "Override type for this file";
                    }
                };
                Runnable overrideTypeForall = new Runnable() {
                    @Override public void run() {
                        for (VirtualFile child : file.getParent().getChildren()) {
                            if (child.getFileType() == fileType) {
                                OverrideFileTypeManager.getInstance().addFile(child, JmteFileType.INSTANCE);
                                TemplateDataLanguageMappings.getInstance(project).setMapping(child, fileType.getLanguage());
                            }
                        }
                    }

                    @Override public String toString() {
                        return "Override type for all " + fileType.getDisplayName() + " files in " + file.getParent().getName();
                    }
                };
                List<Runnable> runnables = new ArrayList<>();
                runnables.add(overrideType);
                List<VirtualFile> otherChildren = findSiblingFilesOfType(parent, file, fileType);
                if (!otherChildren.isEmpty()) {
                    runnables.add(overrideTypeForall);
                }

                ListPopup listPopup = JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>("Override type", runnables) {
                    @Override public @Nullable PopupStep<?> onChosen(Runnable selectedValue, boolean finalChoice) {
                        selectedValue.run();
                        return FINAL_CHOICE;
                    }
                });
                listPopup.showUnderneathOf(panel);
            });

            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            panel.createActionLabel("Rename file...", () -> {
                List<Runnable>  runnables = new ArrayList<>();
                fileTypeManager.getAssociations(JmteFileType.INSTANCE).forEach((FileNameMatcher association) -> {
                    if (association instanceof ExtensionFileNameMatcher extMatcher) {
                        String ext = "." + extMatcher.getExtension();
                        if (!file.getName().endsWith(ext)) {
                            String newName = file.getName() + ext;
                            runnables.add(new Runnable() {
                                @Override public void run() {
                                    try {
                                        WriteAction.run(() -> {
                                            file.rename(JmteEditorNotificationProvider.this, newName);
                                        });
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                @Override public String toString() {
                                    return "Rename file to " + newName;
                                }
                            });
                        }
                    }
                });
                ListPopup listPopup = JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>(null, runnables) {
                    @Override public @Nullable PopupStep<?> onChosen(Runnable selectedValue, boolean finalChoice) {
                        selectedValue.run();
                        return FINAL_CHOICE;
                    }
                });
                listPopup.showUnderneathOf(panel);

            });

            String ext = file.getExtension();
            if (ext != null) {
                if (fileTypeManager.getFileTypeByExtension(ext) instanceof UnknownFileType) {
                    panel.createActionLabel("Associate '" + ext + "' extension",
                            () -> ApplicationManager.getApplication().runWriteAction(
                                    () -> fileTypeManager.associatePattern(JmteFileType.INSTANCE, "*." + ext)));
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
        JpsModuleSourceRootType<?> sourceRootType = projectFileIndex.getContainingSourceRootType(file);
        return sourceRootType instanceof JavaResourceRootType;
    }

    private boolean looksLikeTemplate(VirtualFile file) {
        try (InputStream in = file.getInputStream()) {
            boolean foundStart = false;
            int b;
            while ((b = in.read()) != -1) {
                if (!foundStart && b == '$' && in.read() == '{') foundStart = true;
                else if (b == '}' && foundStart) return true;
                else if (b == '\n') foundStart = false;
            }
        } catch (IOException ignored) {
        }
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
