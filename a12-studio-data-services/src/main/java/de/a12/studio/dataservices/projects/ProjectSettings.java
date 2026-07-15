package de.a12.studio.dataservices.projects;

import org.jspecify.annotations.NonNull;

import java.io.File;

public class ProjectSettings {

  static final String SETTINGS_FOLDER_NAME = ".studio";

  private final UISettings uiSettings;

  private final AISettings aiSettings;

  private ProjectSettings(UISettings uiSettings, AISettings aiSettings) {
    this.uiSettings = uiSettings;
    this.aiSettings = aiSettings;
  }

  public UISettings getUISettings() {
    return uiSettings;
  }

  public AISettings getAISettings() {
    return aiSettings;
  }

  public static ProjectSettings load(@NonNull File projectFolder) {
    File settingsFolder = new File(projectFolder, SETTINGS_FOLDER_NAME);
    if (!settingsFolder.exists()) {
      settingsFolder.mkdirs();
    }

    UISettings uiSettings = UISettings.load(settingsFolder);
    AISettings aiSettings = AISettings.load(settingsFolder);
    return new ProjectSettings(uiSettings, aiSettings);
  }
}
