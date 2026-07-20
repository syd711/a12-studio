package de.a12.studio.models.projects;

import java.io.File;

public class Project {
  private File folder;
  private ProjectItem root;
  private ProjectSettings settings;

  public void load(File file) {
    this.folder = file;
    this.settings = ProjectSettings.load(folder);
    getRoot();
  }

  public ProjectItem getRoot() {
    if (root == null) {
      root = new ProjectItem(folder);
      root.setRoot(true);
    }
    return root;
  }

  public ProjectSettings getSettings() {
    return settings;
  }

  public File getFolder() {
    return folder;
  }

  public void reload() {
    this.root = null;
  }

  public String getName() {
    return this.folder.getName();
  }
}
