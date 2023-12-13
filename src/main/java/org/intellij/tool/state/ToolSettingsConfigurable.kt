package org.intellij.tool.state;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import javax.annotation.Nullable;
import javax.swing.*;

public class ToolSettingsConfigurable implements Configurable {

    private ToolSettingsComponent toolSettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "77tool Settings";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return toolSettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        toolSettingsComponent = new ToolSettingsComponent();
        return toolSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        ToolSettingsState settings = ToolSettingsState.getInstance();
        return toolSettingsComponent.getBuildAfterPush() != settings.buildAfterPush;
    }

    @Override
    public void apply() {
        ToolSettingsState settings = ToolSettingsState.getInstance();
        settings.buildAfterPush = toolSettingsComponent.getBuildAfterPush();
    }

    @Override
    public void reset() {
        ToolSettingsState settings = ToolSettingsState.getInstance();
        toolSettingsComponent.setBuildAfterPush(settings.buildAfterPush);
    }

    @Override
    public void disposeUIResources() {
        toolSettingsComponent = null;
    }
}
