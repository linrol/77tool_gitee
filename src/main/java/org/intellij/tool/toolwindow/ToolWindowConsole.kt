package org.intellij.tool.toolwindow

import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import org.intellij.tool.utils.TimeUtils

object ToolWindowConsole {
    private lateinit var project: Project
    private lateinit var console: ConsoleView
    private var isShow: Boolean = false

    fun register(console: ConsoleView, project: Project) {
        ToolWindowConsole.console = console
        ToolWindowConsole.project = project
    }

    fun show() {
        isShow = true
    }

    fun clear() {
        console.clear()
    }

    fun log(s: String) {
        if (console.isOutputPaused) {
            console.isOutputPaused = false
        }
        val time = TimeUtils.getCurrentTime(null)
        console.print("$time $s\n", ConsoleViewContentType.NORMAL_OUTPUT)
    }
}
