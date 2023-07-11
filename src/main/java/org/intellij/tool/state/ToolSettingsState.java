package org.intellij.tool.state;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "org.intellij.tool.state.ToolSettingsStat", storages = @Storage("77toolSettingsPlugin.xml"))
public class ToolSettingsState implements PersistentStateComponent<ToolSettingsState> {

    // push后触发编译
    public boolean buildAfterPush = false;

    public static ToolSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(ToolSettingsState.class);
    }


    @Override
    public @Nullable ToolSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ToolSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
