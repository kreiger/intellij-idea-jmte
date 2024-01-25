package com.linuxgods.kreiger.idea.jmte;

import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.linuxgods.kreiger.idea.jmte.psi.JmteTypes;

import static com.linuxgods.kreiger.idea.jmte.JmteFileElementTypes.OUTER_ELEMENT_TYPE;

class JmteTemplateDataElementType extends TemplateDataElementType {

    public JmteTemplateDataElementType() {
        super("JMTE_TEMPLATE_DATA", JmteLanguage.INSTANCE, JmteTypes.TEMPLATE_DATA_TOKEN, OUTER_ELEMENT_TYPE);
    }
}
