package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutors;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.PsiPlainTextFileImpl;
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import com.intellij.psi.templateLanguages.TemplateLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

public class JmteFileViewProvider extends MultiplePsiFilesPerDocumentFileViewProvider implements ConfigurableTemplateLanguageFileViewProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmteFileViewProvider.class);
    private final Language templateDataLanguage;
    private final Set<Language> languages;

    public JmteFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled) {
        this(manager, virtualFile, eventSystemEnabled, getSubstitutedLanguage(virtualFile, manager.getProject()));
    }

    private JmteFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled, Language templateDataLanguage) {
        super(manager, virtualFile, eventSystemEnabled);
        this.templateDataLanguage = templateDataLanguage;
        this.languages = Set.of(JmteLanguage.INSTANCE, templateDataLanguage);
    }

    @Override
    public @NotNull Language getBaseLanguage() {
        return JmteLanguage.INSTANCE;
    }

    @Override
    public @NotNull Set<Language> getLanguages() {
        return languages;
    }

    @Override
    public @NotNull Language getTemplateDataLanguage() {
        return templateDataLanguage;
    }

    @Override
    protected @NotNull MultiplePsiFilesPerDocumentFileViewProvider cloneInner(@NotNull VirtualFile fileCopy) {
        return new JmteFileViewProvider(this.getManager(), fileCopy, false, this.templateDataLanguage);
    }

    @Override
    protected @Nullable PsiFile createFile(@NotNull Language lang) {
        if (lang == this.getBaseLanguage()) {
            return this.createFileInner(lang);
        } else if (lang == this.getTemplateDataLanguage()) {
            PsiFileImpl file = (PsiFileImpl)this.createFileInner(lang);
            file.setContentElementType(JmteFileElementTypes.TEMPLATE_DATA);
            return file;
        } else {
            return null;
        }

    }

    private PsiFile createFileInner(Language lang) {
        ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(lang);
        //LOGGER.warn("Language: {}, ParserDefinition: {}", lang, parserDefinition);
        //return new PsiPlainTextFileImpl(this);
        if (parserDefinition == null) return new PsiPlainTextFileImpl(this);
        return parserDefinition.createFile(this);
    }

    static @NotNull Language getSubstitutedLanguage(VirtualFile virtualFile, @NotNull Project project) {
        Language language = getTemplateDataLanguage(virtualFile, project);

        return language instanceof TemplateLanguage
                ? PlainTextLanguage.INSTANCE
                : LanguageSubstitutors.getInstance().substituteLanguage(language, virtualFile, project);
    }


    static @NotNull Language getTemplateDataLanguage(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        Language language = TemplateDataLanguageMappings.getInstance(project).getMapping(virtualFile);
        return language == null ? getTemplateDataLanguageByExtension(virtualFile.getName()) : language;
    }

    private static Language getTemplateDataLanguageByExtension(String name) {
        String extension = substringAfterLast(name, ".");
        if (!name.equals(extension)) {
            FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(extension);
            if (fileType instanceof JmteFileType) {
                return getTemplateDataLanguageByExtension(substringBeforeLast(name, "."));
            }
            if (fileType instanceof LanguageFileType languageFileType) return languageFileType.getLanguage();
        }

        return PlainTextLanguage.INSTANCE;
    }

    @Override public @Nullable IElementType getContentElementType(@NotNull Language language) {
        return language.is(this.getTemplateDataLanguage()) ? JmteFileElementTypes.TEMPLATE_DATA : null;
    }
}
