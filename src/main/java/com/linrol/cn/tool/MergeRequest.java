package com.linrol.cn.tool;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.linrol.cn.tool.toolwindow.ToolWindowConsole;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.linrol.cn.tool.utils.ShellUtils.init;
import static com.linrol.cn.tool.utils.ShellUtils.cmd;

public class MergeRequest extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        ToolWindowConsole.clear();
        String path = getPath(event);
        if (path == null){
            return;
        }
        init(path);
        createMergeRequest(project);
    }

    private void createMergeRequest(Project project) {
        Consumer<String> log = log(project);
        try {
            String isGitRepo = cmd("git rev-parse --is-inside-work-tree").getRet();
            if (!isGitRepo.equals("true")) {
                log.accept("你所选中的好像不是git工程目录，请重新选择");
                return;
            }
            String branch = cmd("git branch --show-current").getRet();
            if (StringUtils.isBlank(branch)) {
                log.accept("获取git工程当前所在分支失败");
                return;
            }
            boolean unCommit = StringUtils.isNotBlank(cmd("git status -s").getRet());
            if (unCommit) {
                String title = Messages.showInputDialog(project, "Input commit message:", String.format("%s create branch merge request", project.getName()), Messages.getInformationIcon());
                if (StringUtils.isBlank(title)) {
                    log.accept("您取消了操作");
                    // dialog.accept("取消操作");
                    return;
                }
                cmd(String.format("git add .;git commit -m '%s'", title));
            }
            boolean hasPush = StringUtils.isBlank(cmd(String.format("git log %s ^origin", branch)).getRet());
            if (hasPush) {
                log.accept("当前没有需要push的代码!!!");
                return;
            }
            CompletableFuture.runAsync(() -> {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    String tmp_name = sdf.format(System.currentTimeMillis());
                    String push_cmd = String.format("git push origin head:%s -o merge_request.target=%s", tmp_name, branch);
                    push_cmd += " -o merge_request.create -o merge_request.remove_source_branch -f 2>&1";
                    log.accept(cmd(push_cmd).getRet());
                } catch (Exception e){
                    log.accept(e.getMessage());
                }
            });
        } catch (Exception e) {
            log.accept(e.getMessage());
        }
    }

    private static Consumer<String> dialog(Project p) {
        return (msg) -> {
            if (StringUtils.isBlank(msg)) {
                return;
            }
            Messages.showMessageDialog(p, msg, "77tool", Messages.getInformationIcon());
        };
    }

    private void eg(Project project, AnActionEvent e) {
        Consumer<String> dialog = dialog(project);
        //显示对话框
        Messages.showMessageDialog(project, project.getName(), "77tool", Messages.getInformationIcon());
        System.out.println(project.getName());

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        //获取当前操作的类文件
        PsiElement eData = e.getData(CommonDataKeys.PSI_ELEMENT);
        Object[] data = e.getData(PlatformDataKeys.SELECTED_ITEMS);
//获取当前类文件的路径

        if (psiFile != null) {
            String classPath = psiFile.getVirtualFile().getPath();
            System.out.println(classPath);
        }
    }

    private static String getPath(AnActionEvent event) {
        PsiFileSystemItem psiElement = (PsiFileSystemItem)event.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            ToolWindowConsole.log("你所选中的好像不是git工程目录，请重新选择");
            return null;
        }
        String path = psiElement.getVirtualFile().getPath();
        return psiElement.isDirectory() ? path : new File(path).getParentFile().getPath();
    }

    private static Consumer<String> log(Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("77tool");
        if (toolWindow != null) {
            toolWindow.show();
        }
        return ToolWindowConsole::log;
    }
}
