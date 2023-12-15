package com.linuxgods.kreiger.idea.jmte;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateLanguage;

public class JmteLanguage extends Language implements TemplateLanguage {
    public static final JmteLanguage INSTANCE = new JmteLanguage();
    private JmteLanguage() {
        super("JMTE");
    }
}
