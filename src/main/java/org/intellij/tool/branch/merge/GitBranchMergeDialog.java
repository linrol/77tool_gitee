package org.intellij.tool.branch.merge;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.GitLocalBranch;
import git4idea.GitReference;
import git4idea.GitRemoteBranch;
import git4idea.branch.GitBrancher;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.repo.GitRepository;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.intellij.tool.model.GitCmd;
import org.intellij.tool.swing.AutoCompletion;
import org.intellij.tool.utils.GitLabUtil;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.intellij.tool.utils.StringUtils.isBlank;

public class GitBranchMergeDialog extends JDialog {

    private static final Logger logger = Logger.getInstance(GitBranchMergeDialog.class);

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private Project project;

    private Map<String, String> branchMap;

    // 来源分支
    private JComboBox<String> source;

    // 目标分支
    private JComboBox<String> target;

    // 工程模块
    private JComboBox<String> module;

    public void setProject(Project project) {
        this.project = project;
    }

    public Map<String, String> getBranchMap() {
        return branchMap;
    }

    public void setBranchMap(Map<String, String> branchMap) {
        this.branchMap = branchMap;
    }

    public void branchBindData(Map<String, String> branchMap) {
        this.setBranchMap(branchMap);
        List<String> branchList = new ArrayList<>(getBranchMap().keySet());
        branchList.sort(Comparator.comparing(this::getSourceBranchWeight));
        branchList.forEach(name -> {
            source.addItem(name);
        });
        branchList.sort(Comparator.comparing(this::getTargetBranchWeight));
        branchList.forEach(name -> {
            target.addItem(name);
        });
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
        dialog.branchBindData(getBranchList(repos));
        dialog.moduleBindData(getModuleList(repos));
        dialog.pack();
        dialog.setVisible(true);
    }

    public static Map<String, String> getBranchList(List<GitRepository> repos) {
        return repos.stream().flatMap(p -> {
            return p.getBranches().getRemoteBranches().stream();
        }).distinct().filter(f -> {
            return !isBlank(f.getName()) && !isBlank(f.getNameForRemoteOperations());
        }).collect(Collectors.toMap(GitRemoteBranch::getNameForRemoteOperations, GitReference::getName, (b1, b2) -> b1));
    }

    public static List<String> getModuleList(List<GitRepository> repos) {
        List<String> list = repos.stream().map(p -> {
            return p.getRoot().getName();
        }).distinct().collect(Collectors.toList());
        list.add(0, "共有分支所有工程");
        return list;
    }

    private void onOK() {
        commonMerge();
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public void commonMerge() {
        withExceptionRun(() -> {
            String sourceBranch = source.getEditor().getItem().toString();
            String targetBranch = target.getEditor().getItem().toString();
            String moduleName = module.getEditor().getItem().toString();
            List<GitRepository> repositories = GitLabUtil.getCommonRepositories(project, sourceBranch, targetBranch).stream().filter(repo -> {
                if (moduleName.equals("共有分支所有工程")) {
                    return true;
                }
                return moduleName.equals(repo.getRoot().getName());
            }).collect(Collectors.toList());
            checkParams(sourceBranch, targetBranch, moduleName, repositories);

            GitBrancher brancher = GitBrancher.getInstance(project);
            Runnable callInAwtLater = () -> {
                withExceptionRun(() -> {
                    boolean checkoutRet = assertRepoBranch(repositories, targetBranch);
                    if (checkoutRet) {
                        if (pull(repositories)) {
                            brancher.merge(getBranchMap().get(sourceBranch), GitBrancher.DeleteOnMergeOption.NOTHING, repositories);
                        }
                    }
                });
            };
            brancher.checkout(targetBranch, false, repositories, callInAwtLater);
        });
    }


    public boolean assertRepoBranch(List<GitRepository> repositories, String branchName) {
        return repositories.stream().map(repo -> {
            GitLocalBranch branch = repo.getCurrentBranch();
            boolean ret = branch != null && branch.getName().equals(branchName);
            if (!ret) {
                String module = repo.getRoot().getName();
                GitCmd.log(project, String.format("工程【%s】切换分支到【%s】失败，终止合并！！！", module, branchName));
            }
            return ret;
        }).reduce((r1, r2) -> r1 && r2).orElse(false);
    }

    private void checkParams(String sourceBranch, String targetBranch, String moduleName, List<GitRepository> repositories) {
        if (isBlank(sourceBranch)) {
            throw new RuntimeException("来源分支必填");
        }
        if (isBlank(targetBranch)) {
            throw new RuntimeException("目标分支必填");
        }
        if (sourceBranch.equals(targetBranch)) {
            throw new RuntimeException("来源分支和目标分支不允许相同");
        }
        if (isBlank(moduleName)) {
            throw new RuntimeException("工程模块必填");
        }
        if (repositories.size() < 1) {
            throw new RuntimeException(String.format("根据来源【%s】和目标【%s】分支未找到交集的工程，终止合并！！！", sourceBranch, targetBranch));
        }
    }

    private boolean pull(List<GitRepository> repositories) {
        return repositories.stream().map(repo -> {
            GitCmd cmd = new GitCmd(project, repo);
            GitCommandResult result = cmd.build(GitCommand.PULL).run();
            return result.success();
        }).reduce((r1, r2) -> r1 && r2).orElse(false);
    }

    public Integer getSourceBranchWeight(String branch) {
        if (branch.equals("stage")) {
            return 0;
        }
        if (branch.equals("master")) {
            return 1;
        }
        if (branch.startsWith("sprint") || branch.startsWith("release")) {
            String date = branch.replace("sprint", "").replace("release", "");
            if (date.length() == 8) {
                return 2;
            }
        }
        if (branch.startsWith("feature")) {
            return 3;
        }
        if (branch.startsWith("stage-patch") || branch.startsWith("emergency")) {
            return 4;
        }
        return 5;
    }

    public Integer getTargetBranchWeight(String branch) {
        if (branch.startsWith("feature")) {
            return 0;
        }
        if (branch.startsWith("sprint") || branch.startsWith("release")) {
            String date = branch.replace("sprint", "").replace("release", "");
            if (date.length() == 8) {
                return 1;
            }
        }
        if (branch.equals("stage") || branch.equals("master")) {
            return 2;
        }
        if (branch.startsWith("stage-patch") || branch.startsWith("emergency")) {
            return 3;
        }
        return 4;
    }

    public void withExceptionRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException e) {
            GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e));
        } catch (Throwable e) {
            e.printStackTrace();
            GitCmd.log(project, ExceptionUtils.getRootCauseMessage(e));
            logger.error("GitMergeRequestAction execute failed", e);
        }
    }
}
