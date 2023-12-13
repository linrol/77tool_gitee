package org.intellij.tool.state

import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class ToolSettingsComponent {
    val panel: JPanel
    private val buildAfterPush = JBCheckBox("Enable ops build after successful push? ")

    init {
        panel = FormBuilder.createFormBuilder()
                .addComponent(buildAfterPush, 1)
                .addComponentFillVertically(JPanel(), 0)
                .panel
    }

    val preferredFocusedComponent: JComponent get() = buildAfterPush

    fun getBuildAfterPush(): Boolean {
        return buildAfterPush.isSelected
    }

    fun setBuildAfterPush(newStatus: Boolean) {
        buildAfterPush.isSelected = newStatus
    }
}
