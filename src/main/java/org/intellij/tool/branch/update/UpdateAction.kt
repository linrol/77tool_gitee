package org.intellij.tool.branch.update;

import com.intellij.openapi.vcs.update.CommonUpdateProjectAction;

public class UpdateAction extends CommonUpdateProjectAction {

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
