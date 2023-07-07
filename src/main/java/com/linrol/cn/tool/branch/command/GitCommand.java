package com.linrol.cn.tool.branch.command;

import static com.linrol.cn.tool.utils.StringUtils.isBlank;
import static com.linrol.cn.tool.utils.TimeUtils.getCurrentTime;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.linrol.cn.tool.model.GitCmd;
import com.linrol.cn.tool.model.RepositoryChange;
import git4idea.GitLocalBranch;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import java.util.Collection;
import org.apache.commons.collections.CollectionUtils;

public class GitCommand {

  public static GitCommandResult createMergeRequest(Project project, RepositoryChange changes) {
    GitRepository repository = changes.getRepository();
    if (repository == null) {
      throw new RuntimeException("你所选中的好像不是git工程目录，请重新选择");
    }
    GitCmd cmd = new GitCmd(project, repository);
    Collection<GitRemote> remotes = repository.getRemotes();
    if (CollectionUtils.isEmpty(remotes)) {
      throw new RuntimeException("远程仓库未找到不存在，请检查你的git remote配置");
    }
    String url = cmd.getRemoteUrl();
    if (isBlank(url)) {
      throw new RuntimeException("远程仓库未找到不存在，请检查你的git remote配置");
    }
    GitLocalBranch currentBranch = repository.getCurrentBranch();
    if (currentBranch == null) {
      throw new RuntimeException("本地仓库当前分支获取失败");
    }
    String branch = cmd.getCurrentBranchName();
    if (isBlank(branch)) {
      throw new RuntimeException("本地仓库当前分支获取失败");
    }
    commit(changes);
    return push(cmd);
  }

  public static GitCommandResult push(GitCmd cmd) {
    String branch = cmd.getCurrentBranchName();
    String url = cmd.getRemoteUrl();
    if (!needPush(cmd, branch)) {
      return null;
    }
    String tmpBranch = "77tool_mr_" + getCurrentTime("yyyyMMddHHmmss");
    GitCommandResult ret = cmd.build(git4idea.commands.GitCommand.PUSH).config(false, false, url)
        .addParameters("origin")
        .addParameters(String.format("head:%s", tmpBranch))
        .addParameters("-o", "merge_request.create")
        .addParameters("-o", String.format("merge_request.target=%s", branch))
        .addParameters("-o", "merge_request.remove_source_branch")
        .addParameters("-f").run();
    cmd.log(ret.getErrorOutputAsJoinedString());
    return ret;
  }

  private static boolean needPush(GitCmd cmd, String branch) {
    String logRet = cmd.build(git4idea.commands.GitCommand.LOG, branch, "^origin/" + branch).run().getOutputAsJoinedString();
    boolean need = !isBlank(logRet);
    if (!need) {
      String rootName = cmd.getRoot().getName();
      cmd.log(String.format("工程【%s】分支【%s】没有需要push的代码!!!", rootName, branch));
    }
    return need;
  }

  private static void commit(RepositoryChange changes) {
    // String title = hasStash(cmd);
    String title = changes.getCommitMessage();
    if (isBlank(title)) {
      return;
    }
    CheckinEnvironment checkinEnvironment = changes.getRepository().getVcs().getCheckinEnvironment();
    if (checkinEnvironment == null) {
      throw new RuntimeException("getCheckinEnvironment null");
    }
    checkinEnvironment.commit(changes.getChangeFileList(), title);
  }

  private static String hasStash(GitCmd cmd) {
    GitCommandResult ret = cmd.build(git4idea.commands.GitCommand.STATUS, "-s", "-uno").run(); //-uno排除不受git管理的文件
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
