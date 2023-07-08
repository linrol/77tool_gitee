package org.intellij.tool.branch.merge;

import com.intellij.openapi.project.Project;
import git4idea.GitRemoteBranch;
import git4idea.branch.GitBrancher;
import git4idea.repo.GitRepository;
import org.intellij.tool.swing.AutoCompletion;
import org.intellij.tool.utils.GitLabUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

public class GitBranchMergeDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private Project project;

    // 来源分支
    private JComboBox<String> source;

    // 目标分支
    private JComboBox<String> target;

    // 工程模块
    private JComboBox<String> module;

    public void setProject(Project project) {
        this.project = project;
    }

    public void sourceBindData(List<String> sourceBranchList) {
        sourceBranchList.forEach(name -> source.addItem(name));
    }

    public void targetBindData(List<String> targetBranchList) {
        targetBranchList.forEach(name -> target.addItem(name));
    }

    public void moduleBindData(List<String> modules) {
        modules.forEach(name -> module.addItem(name));
    }

    public GitBranchMergeDialog(Project project) {
        setLocationRelativeTo(contentPane);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setProject(project);
        AutoCompletion.enable(source);
        AutoCompletion.enable(target);
        AutoCompletion.enable(module);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        // 点击 X 时调用 onCancel()
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        // 遇到 ESCAPE 时调用 onCancel()
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void build(Project project) {
        GitBranchMergeDialog dialog = new GitBranchMergeDialog(project);
        List<GitRepository> repos = GitLabUtil.getRepositories(project);
        dialog.sourceBindData(getSourceBranchList(repos));
        dialog.targetBindData(getTargetBranchList(repos));
        dialog.moduleBindData(getModuleList(repos));
        dialog.pack();
        dialog.setVisible(true);
    }

    public static List<String> getSourceBranchList(List<GitRepository> repos) {
        return repos.stream().flatMap(p -> {
            return p.getBranches().getRemoteBranches().stream();
        }).map(GitRemoteBranch::getNameForRemoteOperations).distinct().collect(Collectors.toList());
    }

    public static List<String> getTargetBranchList(List<GitRepository> repos) {
        return repos.stream().flatMap(p -> {
            return p.getBranches().getRemoteBranches().stream();
        }).map(GitRemoteBranch::getNameForRemoteOperations).distinct().collect(Collectors.toList());
    }

    public static List<String> getModuleList(List<GitRepository> repos) {
        List<String> list = repos.stream().map(p -> {
            return p.getRoot().getName();
        }).distinct().collect(Collectors.toList());
        list.add(0, "共有分支所有工程");
        return list;
    }

    private void onOK() {
        merge();
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public void merge() {
        GitBrancher brancher = GitBrancher.getInstance(project);
        String sourceBranch = source.getEditor().getItem().toString();
        String targetBranch = target.getEditor().getItem().toString();
        List<GitRepository> repositories = GitLabUtil.getRepositories(project, sourceBranch, targetBranch);
        brancher.checkout(targetBranch, false, repositories, () -> {
            brancher.merge(sourceBranch, GitBrancher.DeleteOnMergeOption.NOTHING, repositories);
        });
    }

}
