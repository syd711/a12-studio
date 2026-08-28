package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;

/**
 * This plugin's own project-scoped settings, persisted at {@code <project>/.studio/application-groups-settings.json}.
 */
public class ApplicationGroupsSettings extends JsonSettings {

  static final String SETTINGS_FILE_NAME = "application-groups-settings.json";

  private boolean useApplicationGroups = false;

  private String applicationGroupName = "";

  public boolean isUseApplicationGroups() {
    return useApplicationGroups;
  }

  public void setUseApplicationGroups(boolean useApplicationGroups) {
    this.useApplicationGroups = useApplicationGroups;
  }

  public String getApplicationGroupName() {
    return applicationGroupName;
  }

  public void setApplicationGroupName(String applicationGroupName) {
    this.applicationGroupName = applicationGroupName;
  }

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.PLUGIN;
  }

  public static ApplicationGroupsSettings load(@NonNull File projectFolder) {
    File studioFolder = JsonSettings.resolveSettingsFolder(projectFolder, ".studio");
    return JsonSettings.load(new File(studioFolder, SETTINGS_FILE_NAME), ApplicationGroupsSettings.class);
  }
}
