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
import org.apache.commons.lang3.exception.ExceptionUtils;
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
      GitCmd.clear();
      dialog.getSelectedPushSpecs().values().stream().flatMap(m -> m.stream().map(PushInfo::getRepository)).forEach(repo -> {
        // 实现逻辑
        GitCmd cmd = new GitCmd(project, (GitRepository) repo);
        GitCommand.push(cmd);
      });
      close(dialog, OK_EXIT_CODE);
    } catch (RuntimeException e) {
      e.printStackTrace();
      GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e));
    }  catch (Throwable e) {
      close(dialog, OK_EXIT_CODE);
      e.printStackTrace();
      GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e));
      logger.error("GitMergeRequestAction execute failed", e);
    }

  }

  private void close(VcsPushUi dialog, int exitCode) {
    if (dialog instanceof VcsPushDialog) {
      // 关闭push窗口
      VcsPushDialog vcsPushDialog = (VcsPushDialog) dialog;
      vcsPushDialog.close(exitCode);
    }
  }
}
