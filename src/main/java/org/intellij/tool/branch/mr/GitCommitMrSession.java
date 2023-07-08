package org.intellij.tool.branch.mr;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitSession;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import org.intellij.tool.branch.command.GitCommand;
import org.intellij.tool.model.GitCmd;
import org.intellij.tool.utils.GitLabUtil;
import org.intellij.tool.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import git4idea.GitVcs;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class GitCommitMrSession implements CommitSession {

    private static final Logger logger = Logger.getInstance(GitCommitMrSession.class);

    private final Project project;

    public GitCommitMrSession(Project project) {
        this.project = project;
    }

    @Override
    public boolean canExecute(Collection<Change> changes, String commitMessage) {
        return changes.size() > 0;
    }

    @Override
    public void execute(@NotNull Collection<Change> changes, @Nullable @NlsSafe String commitMessage) {
        try {
            GitCmd.clear();
            List<Change> changeList = new ArrayList<>(changes);
            CheckinEnvironment checkinEnvironment = GitVcs.getInstance(project).getCheckinEnvironment();
            if (checkinEnvironment == null) {
                throw new RuntimeException("getCheckinEnvironment null");
            }
            if (commitMessage == null || StringUtils.isBlank(commitMessage)) {
                throw new RuntimeException("请输入提交消息");
            }
            // GitLabUtil.groupByRepository(project, changeList);
            checkinEnvironment.commit(changeList, commitMessage);
            GitLabUtil.getRepositories(project, changeList).forEach(repo -> {
                GitCommand.push(new GitCmd(project, repo));
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
            GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e));
        } catch (Throwable e) {
            e.printStackTrace();
            GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e));
            logger.error("GitCommitMrSession execute failed", e);
        }
    }

    /** private void commit(Collection<Change> changes) {
        // AbstractVcsHelper helper = AbstractVcsHelper.getInstance(project);
        // helper.commitChanges()
        ChangeListManager manager = ChangeListManager.getInstance(project);
        LocalChangeList changeList = manager.getChangeList(changes.stream().findFirst().get());
        if (changeList == null) {
            return;
        }
        manager.commitChanges(changeList, new ArrayList<>(changes));
    } **/

}
