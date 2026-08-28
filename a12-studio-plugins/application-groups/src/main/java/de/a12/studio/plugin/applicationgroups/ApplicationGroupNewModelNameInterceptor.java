package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.plugin.manager.INewModelNameInterceptor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ApplicationGroupNewModelNameInterceptor implements INewModelNameInterceptor {

  @Override
  @NonNull
  public String adjustName(@NonNull ProjectItem targetFolder, @Nullable ModelType modelType, @NonNull String proposedName) {
    ApplicationGroupsSettings settings = ApplicationGroupsSettings.load(targetFolder.getProjectFolder());
    if (!settings.isUseApplicationGroups()) {
      return proposedName;
    }
    String groupName = settings.getApplicationGroupName();
    if (!ApplicationGroupFeature.isValidGroupName(groupName)) {
      return proposedName;
    }

    String expectedPrefix = groupName + "_";
    if (proposedName.startsWith(expectedPrefix)) {
      return proposedName;
    }
    return expectedPrefix + proposedName;
  }
}
