package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lang.Language;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.templateLanguages.TemplateLanguage;
import org.jetbrains.annotations.NotNull;

public class JmteLanguage extends Language implements TemplateLanguage {
    public static final JmteLanguage INSTANCE = new JmteLanguage();
    private JmteLanguage() {
        super("JMTE");
    }

    @Override public @NotNull @NlsSafe String getDisplayName() {
        return "Java Minimal Template Engine";
    }
}
