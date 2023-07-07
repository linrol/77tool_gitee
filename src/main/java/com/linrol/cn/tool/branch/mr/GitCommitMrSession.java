package com.linrol.cn.tool.branch.mr;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitSession;
import com.intellij.openapi.vfs.VirtualFile;
import com.linrol.cn.tool.branch.command.GitCommand;
import com.linrol.cn.tool.model.GitCmd;
import com.linrol.cn.tool.utils.GitLabUtil;
import java.util.List;
import java.util.stream.Collectors;
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
            List<VirtualFile> vfs = changes.stream().map(Change::getVirtualFile).collect(Collectors.toList());
            GitLabUtil.groupByRepository(project, vfs).forEach(repoVfs -> {
                repoVfs.setCommitMessage(commitMessage);
                GitCommand.createMergeRequest(project, repoVfs);
            });
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("Git Commit execute failed", e);
            GitCmd.log(project, e.getMessage());
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
