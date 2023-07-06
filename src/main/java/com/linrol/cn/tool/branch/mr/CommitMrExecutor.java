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

public class CommitMrExecutor extends LocalCommitExecutor {

    private final Project project;

    public CommitMrExecutor(Project project) {
        this.project = project;
    }

    public static CommitMrExecutor getInstance(Project project) {
        final ExtensionPoint<LocalCommitExecutor> extPoint = project.getExtensionArea().getExtensionPoint(LOCAL_COMMIT_EXECUTOR.getName());
        return (CommitMrExecutor) extPoint.extensions().filter(e -> e.getClass().equals(CommitMrExecutor.class)).findFirst().get();
    }


    @Override
    public @Nullable @NonNls String getHelpId() {
        return null;
    }

    @Override
    public @Nls @NotNull String getActionText() {
        return "Create MR";
    }

    @Override
    public @NotNull
    CommitSession createCommitSession(@NotNull CommitContext commitContext) {
        // return CommitSession.VCS_COMMIT;
        return new CommitMrSession(project);
    }
}
