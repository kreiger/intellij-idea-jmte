package com.linuxgods.kreiger.idea.jmte;

import com.floreysoft.jmte.Engine;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.undo.UndoUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorImpl;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        Document previewDocument = FileDocumentManager.getInstance().getDocument(previewFile, project);
        UndoUtil.disableUndoFor(previewDocument);
        EditorFactory editorFactory = EditorFactory.getInstance();
        EditorImpl viewer = (EditorImpl) editorFactory.createViewer(previewDocument, project, EditorKind.UNTYPED);

        TextEditor previewEditor = new TextEditorImpl(project, file, textEditorProvider, viewer);
        Document document = editor.getEditor().getDocument();
        DocumentListener documentListener = new BulkAwareDocumentListener.Simple () {
            @Override public void afterDocumentChange(@NotNull Document document) {
                previewDocument.setText(getTransformed(document.getText()));
            }
        };
        document.addDocumentListener(documentListener);
        return new TextEditorWithPreview(editor, previewEditor, "JmteEditor", TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW, true);
    }

    @Nullable private static String getTransformed(String template) {
        try {
            Engine engine = new Engine();
            Map<String, Object> model = new HashMap<>() {
                @Override public Object get(Object key) {
                    return List.of(key,key);
                }
            };
            return Objects.requireNonNull(engine.transform(template, model));
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
