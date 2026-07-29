package de.a12.studio.models.projects;

import de.a12.studio.models.projects.settings.annotations.AnnotationFieldRegistry;
import de.a12.studio.models.projects.settings.annotations.AnnotationHeaderRegistry;

import java.io.File;

public class Project {
  private File folder;
  private ProjectItem root;
  private ProjectSettings settings;
  private final AnnotationHeaderRegistry annotationHeaderRegistry = new AnnotationHeaderRegistry();
  private final AnnotationFieldRegistry annotationFieldRegistry = new AnnotationFieldRegistry();

  public void load(File file) {
    this.folder = file;
    this.settings = ProjectSettings.load(folder);
    getRoot();
    rebuildAnnotationRegistries();
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
    getRoot();
    rebuildAnnotationRegistries();
  }

  public String getName() {
    return this.folder.getName();
  }

  public AnnotationHeaderRegistry getAnnotationHeaderRegistry() {
    return annotationHeaderRegistry;
  }

  public AnnotationFieldRegistry getAnnotationFieldRegistry() {
    return annotationFieldRegistry;
  }

  private void rebuildAnnotationRegistries() {
    annotationHeaderRegistry.rebuild(this);
    annotationFieldRegistry.rebuild(this);
  }
}
