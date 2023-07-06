package com.linrol.cn.tool.model;

import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import java.util.List;
import java.util.stream.Collectors;

public class RepositoryVirtualFiles {

  private GitRepository repository;

  private String commitMessage;

  private List<VirtualFile> virtualFileList;

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

  public List<VirtualFile> getVirtualFileList() {
    return virtualFileList;
  }

  public void setVirtualFileList(List<VirtualFile> virtualFileList) {
    this.virtualFileList = virtualFileList;
  }

  public static RepositoryVirtualFiles of(GitRepository repository, String commitMessage, List<VirtualFile> virtualFileList) {
    RepositoryVirtualFiles files = new RepositoryVirtualFiles();
    files.setRepository(repository);
    files.setCommitMessage(commitMessage);
    files.setVirtualFileList(virtualFileList);
    return files;
  }

  public List<String> getFilePaths() {
    return virtualFileList.stream().map(VirtualFile::getPath).collect(Collectors.toList());
  }

  public boolean isEmptyFile() {
    return this.virtualFileList.size() < 1;
  }
}
