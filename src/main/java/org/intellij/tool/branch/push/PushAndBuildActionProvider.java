package org.intellij.tool.branch.push;

import com.intellij.dvcs.push.PushDialogActionsProvider;
import com.intellij.dvcs.push.ui.PushActionBase;
import com.intellij.dvcs.push.ui.VcsPushUi;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.intellij.tool.state.ToolSettingsState;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class PushAndBuildActionProvider implements PushDialogActionsProvider {
    @NotNull
    @Override
    public List<PushActionBase> getCustomActionsAboveDefault(@NotNull Project project) {
        return Arrays.asList(new PushAndBuildAction());
    }

    class PushAndBuildAction extends PushActionBase{

        @Override
        protected boolean isEnabled(@NotNull VcsPushUi dialog) {
            return ToolSettingsState.getInstance().buildAfterPush;
        }

        @Override
        protected @Nls @Nullable String getDescription(@NotNull VcsPushUi dialog, boolean enabled) {
            return "Push And Build";
        }

        @Override
        protected void actionPerformed(@NotNull Project project, @NotNull VcsPushUi dialog) {
            dialog.push(false);
        }

    }
}
