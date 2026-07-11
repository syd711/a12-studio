package de.a12.studio.ui.projecttree;

import de.a12.studio.dataservices.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ProjectItemViewModel {

  private final ProjectItem projectItem;

  public ProjectItemViewModel(@NonNull ProjectItem projectItem) {
    this.projectItem = projectItem;
  }

  public String getName() {
    return projectItem.getName();
  }

  public boolean isFolder() {
    return projectItem.isFolder();
  }

  public ProjectItem getProjectItem() {
    return projectItem;
  }

  public List<ProjectItemViewModel> getChildren() {
    List<ProjectItemViewModel> children = new ArrayList<>();
    for (ProjectItem child : projectItem.getChildren()) {
      children.add(new ProjectItemViewModel(child));
    }
    return children;
  }

  @Override
  public String toString() {
    return getName();
  }
}
