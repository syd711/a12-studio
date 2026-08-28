package de.a12.studio.plugin.projectpackager;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;

/**
 * This plugin's own project-scoped settings, persisted at {@code <project>/.studio/project-packager-settings.json}.
 */
public class ProjectPackagerSettings extends JsonSettings {

  static final String SETTINGS_FILE_NAME = "project-packager-settings.json";

  private String targetFolder = "";

  public String getTargetFolder() {
    return targetFolder;
  }

  public void setTargetFolder(String targetFolder) {
    this.targetFolder = targetFolder;
  }

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.PLUGIN;
  }

  public static ProjectPackagerSettings load(@NonNull File projectFolder) {
    File studioFolder = JsonSettings.resolveSettingsFolder(projectFolder, ".studio");
    return JsonSettings.load(new File(studioFolder, SETTINGS_FILE_NAME), ProjectPackagerSettings.class);
  }
}
