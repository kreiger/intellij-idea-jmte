package com.linuxgods.kreiger.idea.jmte;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorBundle;
import com.intellij.openapi.file.exclude.OverrideFileTypeManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaResourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class JmteEditorNotificationProvider implements EditorNotificationProvider {
    private static final Key<Boolean> DISABLE_NOTIFICATION = Key.create("jmte.file.type.notification.disable");

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

            var panel = new EditorNotificationPanel() {
                JComponent getLinksPanel() {
                    return this.myLinksPanel;
                }
            };
            panel.setText("This file looks like a JMTE template, containing " + fileType.getDisplayName());

            panel.createActionLabel("Override type...", () -> createListPopup("Override Type", panel.getLinksPanel(), runnables -> {
                runnables.add(namedRunnable("Override type for this file", () -> overrideFileType(project, file)));
                Predicate<VirtualFile> predicate;
                if (fileType instanceof PlainTextFileType) {
                    predicate = child -> child.getExtension().equals(file.getExtension());
                } else {
                    predicate = child -> child.getFileType() == fileType;
                }
                List<VirtualFile> otherChildren = findSiblingFilesOfType(file, predicate);
                if (otherChildren.isEmpty()) {
                    return;
                }
                String displayName = fileType instanceof PlainTextFileType ? "."+file.getExtension() : fileType.getDisplayName();
                runnables.add(namedRunnable("Override type for this and "+otherChildren.size()+" other " + displayName + " files in " + file.getParent().getName(), () -> {
                    overrideFileType(project, file);
                    for (VirtualFile child : otherChildren) {
                        if (child.getFileType() == fileType) {
                            overrideFileType(project, child);
                        }
                    }
                }));
            }));

            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            panel.createActionLabel("Rename file...", () -> createListPopup("Rename File", panel.getLinksPanel(), runnables -> {
                fileTypeManager.getAssociations(JmteFileType.INSTANCE).forEach((FileNameMatcher association) -> {
                    if (association instanceof ExtensionFileNameMatcher extMatcher) {
                        String ext = "." + extMatcher.getExtension();
                        if (!file.getName().endsWith(ext)) {
                            String newName = file.getName() + ext;
                            runnables.add(namedRunnable("Rename file to " + newName, () -> WriteAction.run(() -> file.rename(JmteEditorNotificationProvider.this, newName))));
                        }
                    }
                });
            }));

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

    private static void createListPopup(String title, JComponent panel, Consumer<List<Runnable>> consumer) {
        List<Runnable> runnables = new ArrayList<>();
        consumer.accept(runnables);
        ListPopup listPopup = JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>(title, runnables) {
            @Override
            public @Nullable PopupStep<?> onChosen(Runnable selectedValue, boolean finalChoice) {
                selectedValue.run();
                return FINAL_CHOICE;
            }
        });
        listPopup.showUnderneathOf(panel);
    }

    private void overrideFileType(@NotNull Project project, @NotNull VirtualFile file) {
        //LanguageFileType fileType = (LanguageFileType) file.getFileType();
        //Language language = fileType.getLanguage();
        OverrideFileTypeManager.getInstance().addFile(file, JmteFileType.INSTANCE);
        //TemplateDataLanguageMappings.getInstance(project).setMapping(file, language);
    }

    private static @NotNull Runnable namedRunnable(@Nls @NotNull final String name, final ThrowableRunnable<?> runnable) {
        return new Runnable() {
            @Override public void run() {
                try {
                    runnable.run();
                } catch (Throwable e) {
                    if (e instanceof IOException ioe) {
                        throw new UncheckedIOException(ioe);
                    }
                    throw new RuntimeException(e);
                }
            }

            @Override public String toString() {
                return name;
            }
        };
    }

    private static @NotNull List<VirtualFile> findSiblingFilesOfType(VirtualFile file, Predicate<VirtualFile> filter) {
        List<VirtualFile> otherChildren = Stream.of(file.getParent().getChildren())
                .filter(child -> !file.equals(child))
                .filter(filter)
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

}
