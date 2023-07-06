package com.linrol.cn.tool.branch.mr;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import com.linrol.cn.tool.branch.command.GitCommand;
import com.linrol.cn.tool.model.GitCmd;
import com.linrol.cn.tool.model.RepositoryVirtualFiles;
import com.linrol.cn.tool.utils.GitLabUtil;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;


public class CommitMrAction extends AnAction {

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
    PsiFileSystemItem psiElement = (PsiFileSystemItem) event.getData(CommonDataKeys.PSI_ELEMENT);
    Project project = event.getProject();
    if (project == null || psiElement == null) {
      throw new RuntimeException("你所选中的好像不是git工程目录，请重新选择");
    }
    VirtualFile root = psiElement.getVirtualFile();
    RepositoryVirtualFiles repoVfs = RepositoryVirtualFiles.of(GitLabUtil.getGitRepository(project, root), "null", Collections.singletonList(root));
    GitCommand.createMergeRequest(project, repoVfs);
  }

}