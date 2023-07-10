package org.intellij.tool.branch.merge;

import com.intellij.openapi.vcs.update.CommonUpdateProjectAction;

public class GitBranchUpdateAction extends CommonUpdateProjectAction {

    private Runnable runnable;

    @Override
    protected boolean filterRootsBeforeAction() {
        return false;
    }

    public void setSuccess(Runnable success) {
        this.runnable = success;
    }

    @Override
    protected void onSuccess() {
        runnable.run();
    }
}
