package com.linrol.cn.tool.model;

import com.intellij.openapi.vcs.changes.Change;
import git4idea.repo.GitRepository;
import java.util.List;

public class RepositoryChange {

  private GitRepository repository;

  private String commitMessage;

  private List<Change> changeFileList;

  public GitRepository getRepository() {
    return repository;
  }

  public void setRepository(GitRepository repository) {
    this.repository = repository;
  }

  public String getCommitMessage() {
    return commitMessage;
  }

  public void setCommitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
  }

  public List<Change> getChangeFileList() {
    return changeFileList;
  }

  public void setChangeFileList(List<Change> changeFileList) {
    this.changeFileList = changeFileList;
  }

  public static RepositoryChange of(GitRepository repository, List<Change> changeFileList) {
    RepositoryChange files = new RepositoryChange();
    files.setRepository(repository);
    files.setChangeFileList(changeFileList);
    return files;
  }

  public static RepositoryChange of(GitRepository repository, String commitMessage, List<Change> changeFileList) {
    RepositoryChange repoFiles = of(repository, changeFileList);
    repoFiles.setCommitMessage(commitMessage);
    return repoFiles;
  }
}
