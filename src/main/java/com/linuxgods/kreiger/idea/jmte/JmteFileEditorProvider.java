package com.linuxgods.kreiger.idea.jmte;

import com.floreysoft.jmte.Engine;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JmteFileEditorProvider implements FileEditorProvider, DumbAware {
    private static final String EDITOR_TYPE_ID = "jmte";

    @Override public boolean accept(@NotNull Project project, @NotNull VirtualFile   file) {
        return file.getFileType() instanceof JmteFileType;
    }

    @Override public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        TextEditorProvider textEditorProvider = TextEditorProvider.getInstance();
        TextEditor editor = (TextEditor) textEditorProvider.createEditor(project, file);
        String template;
        try {
            template = new String(file.getInputStream().readAllBytes(), file.getCharset());
        } catch (IOException e) {
            return editor;
        }
        String transformed = getTransformed(template);
        LanguageFileType fileType = JmteFileViewProvider.getTemplateDataLanguage(file, project).getAssociatedFileType();
        LightVirtualFile previewFile = new LightVirtualFile(file.getName(), fileType, transformed);
        TextEditor previewEditor = (TextEditor) textEditorProvider.createEditor(project, previewFile);
        Document document = editor.getEditor().getDocument();
        DocumentListener documentListener = new BulkAwareDocumentListener.Simple () {
            @Override public void afterDocumentChange(@NotNull Document document) {
                String transformed = getTransformed(document.getText());
                Document previewDocument = previewEditor.getEditor().getDocument();
                WriteAction.run(() -> previewDocument.setText(transformed));
            }
        };
        document.addDocumentListener(documentListener);
        return new TextEditorWithPreview(editor, previewEditor, "JmteEditor", TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW, true);
    }

    @Nullable private static String getTransformed(String template) {
        String transformed;
        try {
            Engine engine = new Engine();
            Map<String, Object> model = new HashMap<>() {
                @Override public Object get(Object key) {
                    return List.of(key,key);
                }
            };
            return engine.transform(template, model);
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            return stringWriter.toString();
        }
    }

    @Override public @NotNull @NonNls String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

}
