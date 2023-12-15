package com.linuxgods.kreiger.idea.jmte;

import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.OuterLanguageElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JmteFileElementTypes {
    public static final IElementType OUTER_ELEMENT_TYPE = new OuterLanguageElementType("JMTE_FRAGMENT", JmteLanguage.INSTANCE);
    public static final TemplateDataElementType TEMPLATE_DATA = new JmteTemplateDataElementType();

}
