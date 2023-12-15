package com.linuxgods.kreiger.idea.jmte.psi;

import com.linuxgods.kreiger.idea.jmte.JmteLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class JmteElementType extends IElementType {
    public JmteElementType(@NonNls @NotNull String debugName) {
        super(debugName, JmteLanguage.INSTANCE);
    }
}
