package com.linrol.cn.tool.branch.mr;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.actions.AbstractCommitChangesAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class GitCommitMrAction extends AbstractCommitChangesAction {

    @Nullable
    @Override
    protected CommitExecutor getExecutor(@NotNull Project project) {
        return GitCommitMrExecutor.getInstance(project);
    }

}
