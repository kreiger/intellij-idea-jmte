package com.linuxgods.kreiger.idea.jmte;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class JmteColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Strings", JmteSyntaxHighlighter.STRING_KEYS[1]),
            new AttributesDescriptor("Markup", JmteSyntaxHighlighter.MARKUP_KEYS[1]),
            new AttributesDescriptor("Keyword", JmteSyntaxHighlighter.KEYWORD_KEYS[1]),
            new AttributesDescriptor("Identifier", JmteSyntaxHighlighter.IDENTIFIER_KEYS[1]),
            new AttributesDescriptor("Comment", JmteSyntaxHighlighter.COMMENT_KEYS[1])
    };

    @Override public @Nullable Icon getIcon() {
        return AllIcons.Nodes.Template;
    }

    @Override public @NotNull SyntaxHighlighter getHighlighter() {
        return new JmteSyntaxHighlighter();
    }

    @Override public @NonNls @NotNull String getDemoText() {
        return """
                ${@annotation parameters}
                ${-- comment}
                ${
                
                }
                
                ${example.expression}
                
                ${example.expression(defaultValue)}
                
                ${example.expression;format(parameters)}

                ${prefix,example.expression,suffix}
                
                ${prefix,example.expression(default value),suffix;format(parameters)}
                
                ${example.expression.items[0]}

                ${foreach example.expression.items item  separator }
                    ${item}
                    ${index_item} ${first_item} ${last_item} ${odd_item} ${even_item}
                ${end}
                
                ${foreach example.expression.items}
                    ${_it}
                    ${index__it} ${first__it} ${last__it} ${odd__it} ${even__it}
                ${end}
                
                ${if example.expression} ${else} ${end}
                ${if example.expression = 'string'} ${else} ${end}
                ${if example.expression = "string"} ${else} ${end}
                ${if example.expression = other.expression} ${else} ${end}
                
                """;
    }

    @Override public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return Map.of();
    }

    @Override public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override public @NlsContexts.ConfigurableName @NotNull String getDisplayName() {
        return "JMTE Java Minimal Template Engine";
    }
}
