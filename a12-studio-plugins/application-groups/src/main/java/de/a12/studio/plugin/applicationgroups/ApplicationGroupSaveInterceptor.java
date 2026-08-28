package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.plugin.manager.IModelSaveInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

/**
 * Whenever a model is saved and the Application Groups feature is enabled for its project, ensures
 * the file is named with the current application group prefix - renaming it in place if it isn't.
 */
@Slf4j
public class ApplicationGroupSaveInterceptor implements IModelSaveInterceptor {

  @Override
  public void beforeSave(@NonNull ProjectItem item) {
    if (item.isFolder() || item.getModel() == null) {
      return;
    }

    ApplicationGroupsSettings settings = ApplicationGroupsSettings.load(item.getProjectFolder());
    if (!settings.isUseApplicationGroups()) {
      return;
    }
    String groupName = settings.getApplicationGroupName();
    if (!ApplicationGroupFeature.isValidGroupName(groupName)) {
      return;
    }

    A12Model<?> model = item.getModel();
    String expectedId = groupName + "_" + ApplicationGroupFeature.stripExistingGroupPrefix(model);
    ApplicationGroupFeature.setApplicationGroupAnnotation(model, groupName);

    if (!expectedId.equals(model.getId())) {
      try {
        item.renameTo(expectedId + ".json");
      }
      catch (IOException e) {
        log.warn("Failed to rename '{}' to '{}.json' for application group '{}': {}",
            item.getName(), expectedId, groupName, e.getMessage(), e);
      }
    }
  }
}
