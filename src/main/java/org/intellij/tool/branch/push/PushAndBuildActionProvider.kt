package org.intellij.tool.branch.push

import com.intellij.dvcs.push.PushDialogActionsProvider
import com.intellij.dvcs.push.ui.PushActionBase
import com.intellij.dvcs.push.ui.VcsPushUi
import com.intellij.openapi.project.Project
import org.intellij.tool.state.ToolSettingsState
import org.jetbrains.annotations.Nls

class PushAndBuildActionProvider : PushDialogActionsProvider {
    override fun getCustomActionsAboveDefault(project: Project): List<PushActionBase> {
        return listOf<PushActionBase>(PushAndBuildAction())
    }

    internal inner class PushAndBuildAction : PushActionBase() {
        override fun isEnabled(dialog: VcsPushUi): Boolean {
            return ToolSettingsState.getInstance().buildAfterPush
        }

        override fun getDescription(dialog: VcsPushUi, enabled: Boolean): @Nls String {
            return "Push And Build"
        }

        override fun actionPerformed(project: Project, dialog: VcsPushUi) {
            dialog.push(false)
        }
    }
}
