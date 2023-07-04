package com.linrol.cn.tool.branch;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import com.linrol.cn.tool.model.GitCmd;
import com.linrol.cn.tool.utils.GitLabUtil;
import git4idea.GitLocalBranch;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static com.linrol.cn.tool.utils.StringUtils.isBlank;
import static com.linrol.cn.tool.utils.TimeUtils.getCurrentTime;


public class MergeRequestAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        ProjectLevelVcsManager manager = ProjectLevelVcsManager.getInstance(project);
        if (manager == null) {
            GitCmd.log(project, "你所选中的好像不是git工程目录，请重新选择");
            // No VCS manager available
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                createMergeRequest(e);
            } catch (Exception error) {
                GitCmd.log(project, error.getMessage());
                error.printStackTrace();
            }
        });
        // createMergeRequest(project);
    }

    private void createMergeRequest(AnActionEvent event) {
        PsiFileSystemItem psiElement = (PsiFileSystemItem)event.getData(CommonDataKeys.PSI_ELEMENT);
        Project project = event.getProject();
        if (project == null || psiElement == null) {
            GitCmd.log(project, "你所选中的好像不是git工程目录，请重新选择");
            return;
        }
        VirtualFile root = psiElement.getVirtualFile();
        // Git git = ServiceManager.getService(Git.class);
        GitRepository repository = GitLabUtil.getGitRepository(project, root);
        if (repository == null) {
            GitCmd.log(project, "你所选中的好像不是git工程目录，请重新选择");
            return;
        }
        Collection<GitRemote> remotes = repository.getRemotes();
        if (CollectionUtils.isEmpty(remotes)) {
            return;
        }
        String url = repository.getRemotes().stream().flatMap(m -> m.getPushUrls().stream()).findAny().orElse(null);
        if (isBlank(url)) {
            return;
        }
        GitLocalBranch currentBranch = repository.getCurrentBranch();
        if (currentBranch == null) {
            return;
        }
        String branch = currentBranch.getName();
        if (isBlank(branch)) {
            return;
        }
        push(new GitCmd(project, repository), url, branch);
    }

    private GitCommandResult push(GitCmd cmd, String url, String branch) {
        commit(cmd);
        if (!needPush(cmd, branch)) {
            return null;
        }
        String tmpBranch = "77tool_mr_" + getCurrentTime("yyyyMMddHHmmss");
        GitCommandResult ret = cmd.build(GitCommand.PUSH).config(false, false, url)
                .addParameters("origin")
                .addParameters(String.format("head:%s", tmpBranch))
                .addParameters("-o", "merge_request.create")
                .addParameters("-o", String.format("merge_request.target=%s", branch))
                .addParameters("-o", "merge_request.remove_source_branch")
                .addParameters("-f").run();
        cmd.log(ret.getErrorOutputAsJoinedString());
        return ret;
    }

    private boolean needPush(GitCmd cmd, String branch) {
        String logRet = cmd.build(GitCommand.LOG, branch, "^origin/" + branch).run().getOutputAsJoinedString();
        boolean need = !isBlank(logRet);
        if (!need) {
            String rootName = cmd.getRoot().getName();
            cmd.log(String.format("工程【%s】分支【%s】没有需要push的代码!!!", rootName, branch));
        }
        return need;
    }

    private void commit(GitCmd cmd) {
        String title = hasStash(cmd);
        if (isBlank(title)) {
            return;
        }
        // cmd.build(GitCommand.ADD, ".").run(); 不受git管理的文件不添加为git管理，所以注释此行代码
        cmd.build(GitCommand.COMMIT, "-m", title).run();
    }

    private String hasStash(GitCmd cmd) {
        GitCommandResult ret = cmd.build(GitCommand.STATUS, "-s", "-uno").run(); //-uno排除不受git管理的文件
        if (isBlank(ret.getOutputAsJoinedString())) {
            return null;
        }
        String title = cmd.showInputDialog();
        if (isBlank(title)) {
            cmd.log("您取消了代码提交操作");
            return null;
        }
        return title;
    }

}
