package org.intellij.tool.branch.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.actions.AbstractCommitChangesAction;
import org.intellij.tool.branch.commit.extension.CommitMergeRequestExecutor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CommitMergeRequestAction extends AbstractCommitChangesAction {

    @Nullable
    @Override
    protected CommitExecutor getExecutor(@NotNull Project project) {
        return CommitMergeRequestExecutor.getInstance(project);
    }

}
