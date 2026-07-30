package de.a12.studio.models.projects.settings;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;

public class AdvancedSettings extends JsonSettings {

  static final String SETTINGS_FILE_NAME = "advanced-settings.json";

  private boolean useApplicationGroups = false;

  private String applicationGroupName = "";

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.ADVANCED;
  }

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

  public static AdvancedSettings load(@NonNull File studioFolder) {
    return JsonSettings.load(new File(studioFolder, SETTINGS_FILE_NAME), AdvancedSettings.class);
  }
}
