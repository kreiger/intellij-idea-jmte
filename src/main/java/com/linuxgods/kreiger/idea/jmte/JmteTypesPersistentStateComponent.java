package com.linuxgods.kreiger.idea.jmte;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.impl.JavaPsiFacadeEx;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@State(name = "JmteTypes", storages = @Storage("jmte-types.xml"))
public class JmteTypesPersistentStateComponent implements PersistentStateComponent<JmteTypesPersistentStateComponent.JmteTypesState> {
    private JmteTypesState jmteTypesState = new JmteTypesState();

    public static JmteTypesPersistentStateComponent getInstance(Project project) {
        return project.getService(JmteTypesPersistentStateComponent.class);
    }

    @Override
    public JmteTypesState getState() {
        return jmteTypesState;
    }

    @Override
    public void loadState(JmteTypesState jmteTypesState) {
        this.jmteTypesState = jmteTypesState;
    }

    public Optional<String> getFqn(VirtualFile file, String name) {
        return Optional.ofNullable(jmteTypesState.bindingsByFile.get(file.getUrl()))
                .map(map -> map.get(name));
    }

    public String setFqn(VirtualFile file, String name, String fqn) {
        Map<String, String> types = jmteTypesState.bindingsByFile.computeIfAbsent(file.getUrl(), k -> new HashMap<>());
        return fqn == null ? types.remove(name) : types.put(name, fqn);
    }

    public static final class JmteTypesState {
        public Map<String, Map<String, String>> bindingsByFile = new HashMap<>();

    }
}
