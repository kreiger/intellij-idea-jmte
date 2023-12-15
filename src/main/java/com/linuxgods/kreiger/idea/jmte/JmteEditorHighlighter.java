package com.linuxgods.kreiger.idea.jmte;

import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmteEditorHighlighter extends LayeredLexerEditorHighlighter {

    public static final Logger LOGGER = LoggerFactory.getLogger(JmteEditorHighlighter.class);

    public JmteEditorHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile, @NotNull EditorColorsScheme colors) {
        super(new JmteSyntaxHighlighter(), colors);
        SyntaxHighlighter highlighter = getTemplateDataLanguageHighlighter(project, virtualFile);
        this.registerLayer(JmteTypes.TEMPLATE_DATA_TOKEN, new LayerDescriptor(highlighter, ""));
    }

    private static @NotNull SyntaxHighlighter getTemplateDataLanguageHighlighter(Project project, VirtualFile virtualFile) {
        FileType type = project != null && virtualFile != null ? JmteFileViewProvider.getTemplateDataLanguage(virtualFile, project).getAssociatedFileType() : null;
        FileType fileType = type == null ? FileTypes.PLAIN_TEXT : type;

        SyntaxHighlighter highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(fileType, project, virtualFile);
        assert highlighter != null;
        return highlighter;
    }

}
