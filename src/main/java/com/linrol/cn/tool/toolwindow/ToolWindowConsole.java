package com.linrol.cn.tool.toolwindow;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ToolWindowConsole {
    private static Project project;
    private static ConsoleView console;
    public static boolean isShow;


    public ToolWindowConsole(ToolWindow toolWindow, ConsoleView console, Project project) {
        this.console = console;
        this.project = project;
    }

    public static void show() {
        isShow = true;
    }

    public static void clear() {
        if (console != null) {
            console.clear();
        }
    }

    public static void log(String s) {
        if(project == null && console == null){
            return;
        }
        if (console == null) {
            console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        }
        if (console.isOutputPaused()) {
            console.setOutputPaused(false);
        }
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        console.print(time + " " + s + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
    }

    public static void log(Project p, String s) {
        if (project == null) {
            project = p;
        }
        log(s);
    }
}
