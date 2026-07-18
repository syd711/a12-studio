package de.a12.studio.dataservices.projects;

import de.a12.studio.dataservices.projects.settings.JsonSettings;
import de.a12.studio.dataservices.projects.settings.AnnotationSettings;
import de.a12.studio.dataservices.projects.settings.UISettings;
import org.jspecify.annotations.NonNull;

import java.io.File;

public class ProjectSettings {

  static final String SETTINGS_FOLDER_NAME = ".studio";

  private final UISettings uiSettings;

  private final JsonSettings jsonSettings;

  private final AnnotationSettings annotationSettings;

  private ProjectSettings(UISettings uiSettings, JsonSettings jsonSettings, AnnotationSettings annotationSettings) {
    this.uiSettings = uiSettings;
    this.jsonSettings = jsonSettings;
    this.annotationSettings = annotationSettings;
  }

  public UISettings getUISettings() {
    return uiSettings;
  }

  public JsonSettings getAISettings() {
    return jsonSettings;
  }

  public AnnotationSettings getAnnotationSettings() {
    return annotationSettings;
  }

  public static ProjectSettings load(@NonNull File projectFolder) {
    File settingsFolder = de.a12.studio.dataservices.util.JsonSettings.resolveSettingsFolder(projectFolder, SETTINGS_FOLDER_NAME);

    UISettings uiSettings = UISettings.load();
    JsonSettings jsonSettings = JsonSettings.load();
    AnnotationSettings annotationSettings = AnnotationSettings.load(settingsFolder);
    return new ProjectSettings(uiSettings, jsonSettings, annotationSettings);
  }
}
