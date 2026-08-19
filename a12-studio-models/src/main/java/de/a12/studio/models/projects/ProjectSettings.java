package de.a12.studio.models.projects;

import de.a12.studio.models.projects.settings.AdvancedSettings;
import de.a12.studio.models.projects.settings.AiSettings;
import de.a12.studio.models.projects.settings.annotations.AnnotationSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.models.projects.settings.UISettings;
import org.jspecify.annotations.NonNull;

import java.io.File;

public class ProjectSettings {

  static final String SETTINGS_FOLDER_NAME = ".studio";

  private final UISettings uiSettings;

  private final AiSettings jsonSettings;

  private final AnnotationSettings annotationSettings;

  private final ProjectRootSettings projectRootSettings;

  private final AdvancedSettings advancedSettings;

  private ProjectSettings(UISettings uiSettings, AiSettings jsonSettings,
                          AnnotationSettings annotationSettings, ProjectRootSettings projectRootSettings,
                          AdvancedSettings advancedSettings) {
    this.uiSettings = uiSettings;
    this.jsonSettings = jsonSettings;
    this.annotationSettings = annotationSettings;
    this.projectRootSettings = projectRootSettings;
    this.advancedSettings = advancedSettings;
  }

  public UISettings getUISettings() {
    return uiSettings;
  }

  public AiSettings getAISettings() {
    return jsonSettings;
  }

  public AnnotationSettings getAnnotationSettings() {
    return annotationSettings;
  }

  public ProjectRootSettings getProjectRootSettings() {
    return projectRootSettings;
  }

  public AdvancedSettings getAdvancedSettings() {
    return advancedSettings;
  }

  public static ProjectSettings load(@NonNull File projectFolder) {
    File settingsFolder = de.a12.studio.models.util.AiSettings.resolveSettingsFolder(projectFolder, SETTINGS_FOLDER_NAME);

    UISettings uiSettings = UISettings.load();
    AiSettings jsonSettings = AiSettings.load();
    AnnotationSettings annotationSettings = AnnotationSettings.load(settingsFolder);
    ProjectRootSettings projectRootSettings = ProjectRootSettings.load(projectFolder);
    AdvancedSettings advancedSettings = AdvancedSettings.load(settingsFolder);
    return new ProjectSettings(uiSettings, jsonSettings, annotationSettings, projectRootSettings, advancedSettings);
  }
}
