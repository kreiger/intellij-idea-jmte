package com.linuxgods.kreiger.idea.jmte;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.template.Template;
import com.intellij.diff.util.FileEditorBase;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.undo.UndoUtil;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorImpl;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static java.util.stream.Collectors.toSet;

public class JmteFileEditorProvider implements FileEditorProvider, DumbAware {
    private static final String EDITOR_TYPE_ID = "jmte";
    public static final Logger LOGGER = LoggerFactory.getLogger(JmteFileEditorProvider.class);

    @Override public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return file.getFileType() instanceof JmteFileType;
    }

    @Override public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        TextEditorProvider textEditorProvider = TextEditorProvider.getInstance();
        TextEditor editor = (TextEditor) textEditorProvider.createEditor(project, file);
        Engine engine = new Engine();
        LanguageFileType fileType = JmteFileViewProvider.getTemplateDataLanguage(file, project).getAssociatedFileType();
        LightVirtualFile previewFile = new LightVirtualFile(file.getName(), fileType, "");
        Document previewDocument = FileDocumentManager.getInstance().getDocument(previewFile);
        UndoUtil.disableUndoFor(previewDocument);
        TextEditorImpl previewTextEditor = (TextEditorImpl) textEditorProvider.createEditor(project, previewFile);
        EditorEx viewer = previewTextEditor.getEditor();
        viewer.setRendererMode(true);
        DefaultTableModel tableModel = new DefaultTableModel(0, 2) {
            @Override public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        JBTable table = new JBTable(tableModel) {
            @Override public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                if (extend) {
                    super.changeSelection(rowIndex, columnIndex, toggle, extend);
                }
                // Check if the current column is the key column
                if (columnIndex == 0) {
                    // Move to the value column of the same row
                    super.changeSelection(rowIndex, 1, toggle, extend);
                } else {
                    // Move to the next row, value column
                    super.changeSelection(rowIndex + 1, 1, toggle, extend);
                }
            }
        };
        Document document = editor.getEditor().getDocument();
        Map<String, Object> model = new HashMap<>();

        BulkAwareDocumentListener.Simple documentListener = new BulkAwareDocumentListener.Simple() {
            @Override public void afterDocumentChange(@NotNull Document document) {
                try {
                    Template template = engine.getTemplate(document.getText()+" }");
                    updateTable(table, template);
                    previewDocument.setText(getTransformed(template, model));
                } catch (Exception e) {
                    StringWriter stringWriter = new StringWriter();
                    e.printStackTrace(new PrintWriter(stringWriter));
                    previewDocument.setText(stringWriter.toString());
                    LOGGER.error("Error in JMTE template", e);
                }
            }
        };
        WriteAction.run(() -> {
            documentListener.afterDocumentChange(document);
        });
        table.getModel().addTableModelListener(e -> {
            updateModel(tableModel, model);
            WriteAction.run(() -> {
                documentListener.afterDocumentChange(document);
            });
        });
        JBPanel panel = new JBPanel(new BorderLayout());
        panel.add(table, BorderLayout.NORTH);
        panel.add(previewTextEditor.getComponent(), BorderLayout.CENTER);
        document.addDocumentListener(documentListener);
        FileEditor otherPreview = new FileEditorBase() {

            @Override public @NotNull JComponent getComponent() {
                return panel;
            }

            @Override public @Nullable JComponent getPreferredFocusedComponent() {
                return previewTextEditor.getPreferredFocusedComponent();
            }

            @Override public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
                return previewTextEditor.getName();
            }
        };
        return new TextEditorWithPreview(editor, otherPreview, "JmteEditor", TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW, true);
    }

    private void updateModel(DefaultTableModel tableModel, Map<String, Object> model) {
        model.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = (String) tableModel.getValueAt(i, 0);
            String value = (String) tableModel.getValueAt(i, 1);
            model.put(name, value);
        }
    }

    private static void updateTable(JBTable table, Template template) {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        Set<String> usedVariableNames = template.getUsedVariableDescriptions().stream().map(v -> v.name).collect(toSet());
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = (String) tableModel.getValueAt(i, 0);
            if (!usedVariableNames.contains(name) && "".equals(tableModel.getValueAt(i, 1))) {
                tableModel.removeRow(i);
                i--;
            }
        }
        for (String usedVariableName : usedVariableNames) {
            if (!tableModelContains(tableModel, usedVariableName)) {
                tableModel.addRow(new Object[]{usedVariableName, ""});
            }
        }
        table.setModel(tableModel);
    }

    private static boolean tableModelContains(TableModel tableModel, String name) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Nullable private static String getTransformed(Template template, Map<String, Object> model) {
        try {
            String s = Objects.requireNonNull(template.transform(model, Locale.getDefault()));
            if (s.length() >= 2) { // Remove trailing " }" added to template to work around bug in JMTE
                return s.substring(0, s.length() - 2);
            }
            return s;
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
