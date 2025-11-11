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
                ${--comment}
                <html>
                <head>
                    ${<title>,example.title,</title>}
                </head>
                <body>
                    ${<h1>,example.heading,</h1>}
                
                    ${expression}
                
                    ${expression(defaultValue)}
                
                    ${expression;format(parameters)}
                
                    ${foreach items item , }
                        ${item}
                    ${end}
                
                    ${if condition = ' test ' }
                
                    ${elseif other.condition }
                
                    ${else}
                
                    ${end}
                </body>
                </html>
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
