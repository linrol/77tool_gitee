package org.intellij.tool.state;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import javax.swing.*;

public class ToolSettingsComponent {

    private final JPanel myMainPanel;
    private final JBCheckBox buildAfterPush = new JBCheckBox("Enable ops build after successful push? ");

    public ToolSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(buildAfterPush, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JComponent getPreferredFocusedComponent() {
        return buildAfterPush;
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public Boolean getBuildAfterPush() {
       return buildAfterPush.isSelected();
    }

    public void setBuildAfterPush(boolean newStatus) {
        buildAfterPush.setSelected(newStatus);
    }
}
