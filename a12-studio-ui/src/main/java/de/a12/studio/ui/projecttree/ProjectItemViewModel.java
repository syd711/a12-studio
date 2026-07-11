package de.a12.studio.ui.projecttree;

import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.util.Icons;
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

  public boolean hasModel() {
    return projectItem.getModel() != null;
  }

  public ProjectItem getProjectItem() {
    return projectItem;
  }

  public String getIcon() {
    A12Model model = projectItem.getModel();
    if (model == null) {
      return Icons.FILE_OUTLINE;
    }

    return switch (model.getModelType()) {
      case DOCUMENT -> Icons.FILE_TABLE_OUTLINE;
      default -> Icons.FILE_OUTLINE;
    };
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
