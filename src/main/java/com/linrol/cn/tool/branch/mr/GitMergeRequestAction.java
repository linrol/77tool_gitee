package com.linrol.cn.tool.branch.mr;

import static com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE;

import com.intellij.dvcs.push.PushInfo;
import com.intellij.dvcs.push.ui.PushActionBase;
import com.intellij.dvcs.push.ui.VcsPushDialog;
import com.intellij.dvcs.push.ui.VcsPushUi;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.linrol.cn.tool.branch.command.GitCommand;
import com.linrol.cn.tool.model.GitCmd;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GitMergeRequestAction extends PushActionBase {

  private static final Logger logger = Logger.getInstance(GitMergeRequestAction.class);

  GitMergeRequestAction() {
    super("Merge Request");
  }

  @Override
  protected boolean isEnabled(@NotNull VcsPushUi dialog) {
    return true;
  }

  @Override
  protected @Nls @Nullable String getDescription(@NotNull VcsPushUi dialog, boolean enabled) {
    return "Create Merge Request";
  }

  @Override
  protected void actionPerformed(@NotNull Project project, @NotNull VcsPushUi dialog) {
    try {
      dialog.getSelectedPushSpecs().values().stream().flatMap(m -> {
        return m.stream().map(PushInfo::getRepository);
      }).forEach(repo -> {
        // 实现逻辑
        GitCmd cmd = new GitCmd(project, (GitRepository) repo);
        GitCommand.push(cmd);
      });
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Git Commit execute failed", e);
      GitCmd.log(project, e.getMessage());
    }
    if (dialog instanceof VcsPushDialog) {
      // 关闭push窗口
      VcsPushDialog vcsPushDialog = (VcsPushDialog) dialog;
      vcsPushDialog.close(OK_EXIT_CODE);
    }
  }

}
