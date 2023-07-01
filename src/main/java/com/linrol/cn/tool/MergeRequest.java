package com.linrol.cn.tool;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.linrol.cn.tool.utils.Ret;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.linrol.cn.tool.utils.ShellUtils.cmd;

public class MergeRequest extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        Consumer<String> dialog = dialog(project);
        //显示对话框
        Messages.showMessageDialog(project, project.getName(), "77tool", Messages.getInformationIcon());
        System.out.println(project.getName());

        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null) {
            String classPath = psiFile.getVirtualFile().getPath();
            System.out.println(classPath);
        }
        // TODO: insert action logic here
    }

    private void createMergeRequest(Project project, Consumer<String> dialog) throws Exception {
        String branch = cmd("git branch --show-current").getRet();
        boolean unCommit = StringUtils.isNotBlank(cmd("git status -s").getRet());
        if (unCommit) {
            String title = Messages.showInputDialog(project, String.format("%s create branch merge request", project.getName()),"Input Commit Message:", Messages.getInformationIcon());
            cmd(String.format("git add .;git commit -m '%s'", title));
        }
        boolean hasPush = StringUtils.isBlank(cmd(String.format("git log %s ^origin", branch)).getRet());
        if (hasPush) {
            dialog.accept("当前没有需要push的代码!!!");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String tmp_name = sdf.format(System.currentTimeMillis());
        String push_cmd = String.format("git push origin head:%s -o merge_request.target=%s", tmp_name, branch);
        push_cmd += "-o merge_request.create -o merge_request.remove_source_branch -f";
        dialog.accept(cmd(push_cmd).getRet());
    }

    private static Consumer<String> dialog(Project p) {
        return (msg) -> {
            Messages.showMessageDialog(p, msg, "77tool", Messages.getInformationIcon());
        };
    }
}
