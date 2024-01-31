package org.intellij.tool.branch.push

import com.google.gson.JsonParser
import com.intellij.dvcs.push.ui.PushActionBase
import com.intellij.dvcs.push.ui.VcsPushDialog
import com.intellij.dvcs.push.ui.VcsPushUi
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import git4idea.repo.GitRepository
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.lang3.exception.ExceptionUtils
import org.intellij.tool.model.GitCmd
import org.intellij.tool.state.ToolSettingsState

class PushAndBuildAction: PushActionBase("Push And Build") {
    override fun actionPerformed(project: Project, dialog: VcsPushUi) {
        try {
            GitCmd.clear()
            val repos = dialog.selectedPushSpecs.values.flatMap { it.map { obj -> obj.repository } }
            dialog.push(false)
            if (dialog.canPush()) {
                GitCmd.log(project, "size:${repos.size}")
                repos.forEach {
                    // 实现逻辑
                    val gitRepository = it as GitRepository
                    val name = gitRepository.root.name
                    val branch = gitRepository.currentBranchName.toString()
                    opsBuild(project, name, branch)
                }
            }
            close(dialog, DialogWrapper.OK_EXIT_CODE)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e))
        } catch (e: Throwable) {
            close(dialog, DialogWrapper.OK_EXIT_CODE)
            e.printStackTrace()
            GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e))
            logger.error("PushAndBuildAction execute failed", e)
        }
    }

    override fun isEnabled(dialog: VcsPushUi): Boolean {
        return ToolSettingsState.instance.buildAfterPush
    }

    override fun getDescription(dialog: VcsPushUi, enabled: Boolean): String {
        return "Push And Build"
    }

    private fun close(dialog: VcsPushUi, exitCode: Int) {
        if (dialog is VcsPushDialog) {
            // 关闭push窗口
            dialog.close(exitCode)
        }
    }

    private fun opsBuild(project: Project, name: String, branch: String) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
                .add("projects", name)
                .add("branch", branch)
                .add("byCaller", ToolSettingsState.instance.buildUser)
                .build()
        val request = Request.Builder().url(ToolSettingsState.instance.buildUrl).post(body).build()
        client.newCall(request).execute().use {

            if (it.isSuccessful) {
                val jsonResponse = JsonParser.parseString(it.body?.string()).asJsonObject
                val responseData = jsonResponse.get("data")
                if (responseData != null) {
                    val taskId = responseData.asJsonObject.getAsJsonPrimitive("taskid").asString
                    GitCmd.log(project,"项目:${name}触发独立编译成功，编译任务ID:${taskId}")
                }
            } else {
                logger.info("Response Error: ${it.code} - ${it.message}")
                GitCmd.log(project,"Response Error: ${it.code} - ${it.message}")
            }
        }
    }

    companion object {
        private val logger = logger<PushAndBuildAction>()
    }

}