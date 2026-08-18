package de.a12.studio.ui.projecttree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectItemViewModel {

  private final ProjectItem projectItem;
  private final Map<String, List<ModelValidationError>> validationErrorsByPath;

  /** Non-null only on the root node when application groups are enabled and a group name is set. */
  @Nullable
  private final String applicationGroupName;

  public ProjectItemViewModel(@NonNull ProjectItem projectItem, @NonNull Map<String, List<ModelValidationError>> validationErrorsByPath) {
    this(projectItem, validationErrorsByPath, null);
  }

  public ProjectItemViewModel(@NonNull ProjectItem projectItem, @NonNull Map<String, List<ModelValidationError>> validationErrorsByPath,
                               @Nullable String applicationGroupName) {
    this.projectItem = projectItem;
    this.validationErrorsByPath = validationErrorsByPath;
    this.applicationGroupName = applicationGroupName;
  }

  public String getName() {
    return projectItem.getName();
  }

  public String getDisplayName() {
    String name = getName();
    if (!hasModel() && applicationGroupName == null) {
      return name;
    }
    if (!hasModel()) {
      // root folder with application group
      return name + " [" + applicationGroupName + "]";
    }

    int dot = name.lastIndexOf('.');
    return dot > 0 ? name.substring(0, dot) : name;
  }

  /**
   * Returns a tooltip text for the root node when application groups are enabled, or {@code null}
   * if no application group is active for this item.
   */
  @Nullable
  public String getApplicationGroupTooltip() {
    if (applicationGroupName == null) {
      return null;
    }
    return StudioBundle.get("project_tree.application_group_tooltip") + " " + applicationGroupName;
  }

  public boolean isFolder() {
    return projectItem.isFolder();
  }

  public boolean hasModel() {
    return projectItem.getModel() != null;
  }

  public boolean isSettings() {
    return !projectItem.isFolder()
        && "settings.json".equals(projectItem.getName())
        && projectItem.getParent() != null
        && projectItem.getParent().isRoot();
  }

  public boolean hasAuthFile() {
    return projectItem.getAuthDocument() != null;
  }

  public boolean isAuthFile() {
    return !projectItem.isFolder() && hasAuthFile();
  }

  public ProjectItem getProjectItem() {
    return projectItem;
  }

  public String getIconPath() {
    if (isSettings() || isAuthFile()) {
      return null; // handled separately in cell (FontIcon, not ImageView)
    }
    A12Model<?> model = projectItem.getModel();
    if (model == null) {
      return null;
    }

    if (model instanceof TypeDefinitionModel) {
      return Icons.forModelType(ModelType.TYPEDEFINITION);
    }

    return Icons.forModelType(model.getModelType());
  }

  public List<ProjectItemViewModel> getChildren() {
    List<ProjectItemViewModel> children = new ArrayList<>();
    for (ProjectItem child : projectItem.getChildren()) {
      children.add(new ProjectItemViewModel(child, validationErrorsByPath));
    }
    return children;
  }

  public List<ModelValidationError> getValidationErrors() {
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
