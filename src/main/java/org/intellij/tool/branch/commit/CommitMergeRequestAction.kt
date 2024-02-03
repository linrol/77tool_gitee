package org.intellij.tool.branch.commit

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.openapi.vcs.changes.actions.AbstractCommitChangesAction
import org.intellij.tool.branch.commit.extension.CommitMergeRequestExecutor.Companion.getInstance

class CommitMergeRequestAction : AbstractCommitChangesAction() {
    override fun getExecutor(project: Project): CommitExecutor {
        return getInstance()
    }
}
