package org.intellij.tool.state

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nullable
import javax.swing.JComponent

class ToolSettingsConfigurable : Configurable {
    private var toolSettingsComponent: ToolSettingsComponent = ToolSettingsComponent()

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): String {
        return "77tool Settings"
    }

    @Nullable
    override fun createComponent(): JComponent {
        return toolSettingsComponent.panel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return toolSettingsComponent.preferredFocusedComponent
    }

    override fun isModified(): Boolean {
        return toolSettingsComponent.isModified();
    }

    override fun apply() {
        ToolSettingsState.instance.buildAfterPush = toolSettingsComponent.getBuildAfterPush()
        ToolSettingsState.instance.buildUrl = toolSettingsComponent.getBuildUrl()
        ToolSettingsState.instance.buildUser = toolSettingsComponent.getBuildUser()
    }

    override fun reset() {
        toolSettingsComponent.setBuildAfterPush(ToolSettingsState.instance.buildAfterPush)
        toolSettingsComponent.setBuildUrl(ToolSettingsState.instance.buildUrl)
        toolSettingsComponent.setBuildUser(ToolSettingsState.instance.buildUser)
    }

    override fun disposeUIResources() {
    }
}
