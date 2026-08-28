package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.plugin.manager.IProjectOpenedListener;
import org.jspecify.annotations.NonNull;

/**
 * Scans all models in the project tree for the "applicationGroup" annotation on open. If found and
 * the feature is not yet enabled, it is activated and persisted automatically.
 */
public class ApplicationGroupProjectOpenedListener implements IProjectOpenedListener {

  @Override
  public void onProjectOpened(@NonNull Project project) {
    ApplicationGroupsSettings settings = ApplicationGroupsSettings.load(project.getFolder());
    if (settings.isUseApplicationGroups()) {
      return;
    }
    if (hasApplicationGroupAnnotation(project.getRoot())) {
      settings.setUseApplicationGroups(true);
      settings.save();
    }
  }

  private boolean hasApplicationGroupAnnotation(@NonNull ProjectItem item) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        if (hasApplicationGroupAnnotation(child)) {
          return true;
        }
      }
    }
    else if (item.getModel() != null) {
      for (Annotation annotation : item.getModel().getAnnotations()) {
        if (ApplicationGroupFeature.ANNOTATION_NAME.equals(annotation.getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
