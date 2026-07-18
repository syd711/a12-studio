package de.a12.studio.ui.projecttree;

import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.dataservices.services.documentmodel.features.validation.ElementValidationError;
import de.a12.studio.ui.util.Icons;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectItemViewModel {

  private final ProjectItem projectItem;
  private final Map<String, List<ElementValidationError>> validationErrorsByPath;

  public ProjectItemViewModel(@NonNull ProjectItem projectItem, @NonNull Map<String, List<ElementValidationError>> validationErrorsByPath) {
    this.projectItem = projectItem;
    this.validationErrorsByPath = validationErrorsByPath;
  }

  public String getName() {
    return projectItem.getName();
  }

  public String getDisplayName() {
    String name = getName();
    if (!hasModel()) {
      return name;
    }

    int dot = name.lastIndexOf('.');
    return dot > 0 ? name.substring(0, dot) : name;
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

  public String getIconPath() {
    A12Model model = projectItem.getModel();
    if (model == null) {
      return null;
    }

    return switch (model.getModelType()) {
      case DOCUMENT -> Icons.PNG_MODEL_DOCUMENT;
      case FORM -> Icons.PNG_MODEL_FORM;
      case OVERVIEW -> Icons.PNG_MODEL_OVERVIEW;
      case RELATIONSHIP -> Icons.PNG_MODEL_RELATIONSHIP;
      case APPLICATION -> Icons.PNG_MODEL_APPLICATION;
      case CONTENT -> Icons.PNG_MODEL_CONTENT;
    };
  }

  public List<ProjectItemViewModel> getChildren() {
    List<ProjectItemViewModel> children = new ArrayList<>();
    for (ProjectItem child : projectItem.getChildren()) {
      children.add(new ProjectItemViewModel(child, validationErrorsByPath));
    }
    return children;
  }

  public List<ElementValidationError> getValidationErrors() {
    return validationErrorsByPath.getOrDefault(projectItem.getPath(), List.of());
  }

  public boolean hasValidationErrors() {
    return !getValidationErrors().isEmpty();
  }

  @Override
  public String toString() {
    return getName();
  }
}
