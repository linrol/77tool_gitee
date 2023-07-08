package org.intellij.tool.toolwindow;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MslToolWindowFactory implements ToolWindowFactory {

    public static JComponent createConsolePanel(ConsoleView view) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(view.getComponent(), BorderLayout.CENTER);
        return panel;
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
        ConsoleView console = consoleBuilder.getConsole();
        JComponent consolePanel = createConsolePanel(console);
        Content content = toolWindow.getContentManager().getFactory().createContent(consolePanel, "控制台", false);
        toolWindow.getContentManager().addContent(content);
        // console.print("------------after add consoleView to tool------------" + "\n", ConsoleViewContentType.LOG_INFO_OUTPUT);
        new ToolWindowConsole(toolWindow, console, project);
        //PropertiesCenter.init(project);
        //console.setOutputPaused(true);
    }


}