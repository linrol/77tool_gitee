package org.intellij.tool.branch.commit.extension

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.CommitSession
import git4idea.GitVcs
import org.apache.commons.lang3.exception.ExceptionUtils
import org.intellij.tool.branch.command.GitCommand.push
import org.intellij.tool.model.GitCmd
import org.intellij.tool.utils.GitLabUtil

class CommitMergeRequestSession(private val project: Project) : CommitSession {
    companion object {
        private val logger = logger<CommitMergeRequestSession>()
    }

    override fun canExecute(changes: Collection<Change>, commitMessage: String): Boolean {
        return changes.isNotEmpty()
    }

    override fun execute(changes: Collection<Change>, commitMessage: @NlsSafe String?) {
        try {
            GitCmd.clear()
            val changeList = changes.toList()
            val checkinEnvironment = GitVcs.getInstance(project).checkinEnvironment ?: throw RuntimeException("getCheckinEnvironment null")
            if (commitMessage.isNullOrBlank()) {
                throw RuntimeException("请输入提交消息")
            }
            checkinEnvironment.commit(changeList, commitMessage)
            GitLabUtil.getRepositories(project, changeList).forEach {
                push(GitCmd(project, it))
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
            GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e))
        } catch (e: Throwable) {
            e.printStackTrace()
            GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e))
            logger.error("GitCommitMrSession execute failed", e)
        }
    }

}
