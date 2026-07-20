package de.a12.studio.models.projects.settings;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;

public class AnnotationSettings extends JsonSettings {

  static final String SETTINGS_FILE_NAME = "annotation-settings.json";

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.ANNOTATION;
  }

  public static AnnotationSettings load(@NonNull File studioFolder) {
    return JsonSettings.load(new File(studioFolder, SETTINGS_FILE_NAME), AnnotationSettings.class);
  }
}
