package com.linrol.cn.tool.branch.mr;

import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.changes.CommitSession;
import com.intellij.openapi.vcs.changes.LocalCommitExecutor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GitCommitMrExecutor extends LocalCommitExecutor {

    private final Project project;

    public GitCommitMrExecutor(Project project) {
        this.project = project;
    }

    public static GitCommitMrExecutor getInstance(Project project) {
        final ExtensionPoint<LocalCommitExecutor> extPoint = project.getExtensionArea().getExtensionPoint(LOCAL_COMMIT_EXECUTOR.getName());
        return (GitCommitMrExecutor) extPoint.extensions().filter(e -> e.getClass().equals(
            GitCommitMrExecutor.class)).findFirst().get();
    }


    @Override
    public @Nullable @NonNls String getHelpId() {
        return "Commit And Merge Request";
    }

    @Override
    public @Nls @NotNull String getActionText() {
        return "Commit And Merge Request";
    }

    @Override
    public @NotNull
    CommitSession createCommitSession(@NotNull CommitContext commitContext) {
        // return CommitSession.VCS_COMMIT;
        return new GitCommitMrSession(project);
    }
}
