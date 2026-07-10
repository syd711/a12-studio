package de.a12.studio.dataservices.projects;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectItem {

  private File file;
  private ProjectItem parent;
  private List<ProjectItem> children;
  private boolean root = false;

  public ProjectItem(File listFile) {
    this.file = listFile;
  }

  public void setRoot(boolean root) {
    this.root = root;
  }

  public boolean isFolder() {
    return file.isDirectory();
  }

  public String getName() {
    return file.getName();
  }

  public List<ProjectItem> getChildren() {
    if (!this.isFolder()) {
      return Collections.emptyList();
    }

    if (this.children == null) {
      this.children = new ArrayList<>();

      File[] files = file.listFiles(new ProjectFileFilter());
      for (File listFile : files) {
        ProjectItem item = new ProjectItem(listFile);
        children.add(item);
      }
    }
    return children;
  }

  public String getPath() {
    return this.file.getAbsolutePath();
  }

  public boolean isRoot() {
    return root;
  }
}
