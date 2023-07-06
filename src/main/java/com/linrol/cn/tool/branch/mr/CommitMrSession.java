package com.linrol.cn.tool.branch.mr;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class CommitMrSession implements CommitSession {

    private Project project;

    public CommitMrSession(Project project) {
        this.project = project;
    }

    @Override
    public boolean canExecute(Collection<Change> changes, String commitMessage) {
        return changes.size() > 0;
    }

    @Override
    public void execute(@NotNull Collection<Change> changes, @Nullable @NlsSafe String commitMessage) {
        System.out.println("1");
    }


}
