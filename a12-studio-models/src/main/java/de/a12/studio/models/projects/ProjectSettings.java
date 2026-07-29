package de.a12.studio.models.projects;

import de.a12.studio.models.projects.settings.JsonSettings;
import de.a12.studio.models.projects.settings.annotations.AnnotationSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.models.projects.settings.UISettings;
import org.jspecify.annotations.NonNull;

import java.io.File;

public class ProjectSettings {

  static final String SETTINGS_FOLDER_NAME = ".studio";

  private final UISettings uiSettings;

  private final JsonSettings jsonSettings;

  private final AnnotationSettings annotationSettings;

  private final ProjectRootSettings projectRootSettings;

  private ProjectSettings(UISettings uiSettings, JsonSettings jsonSettings,
                          AnnotationSettings annotationSettings, ProjectRootSettings projectRootSettings) {
    this.uiSettings = uiSettings;
    this.jsonSettings = jsonSettings;
    this.annotationSettings = annotationSettings;
    this.projectRootSettings = projectRootSettings;
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

  public ProjectRootSettings getProjectRootSettings() {
    return projectRootSettings;
  }

  public static ProjectSettings load(@NonNull File projectFolder) {
    File settingsFolder = de.a12.studio.models.util.JsonSettings.resolveSettingsFolder(projectFolder, SETTINGS_FOLDER_NAME);

    UISettings uiSettings = UISettings.load();
    JsonSettings jsonSettings = JsonSettings.load();
    AnnotationSettings annotationSettings = AnnotationSettings.load(settingsFolder);
    ProjectRootSettings projectRootSettings = ProjectRootSettings.load(projectFolder);
    return new ProjectSettings(uiSettings, jsonSettings, annotationSettings, projectRootSettings);
  }
}
