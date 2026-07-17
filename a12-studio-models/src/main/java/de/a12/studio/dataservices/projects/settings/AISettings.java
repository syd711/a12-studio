package de.a12.studio.dataservices.projects.settings;

import de.a12.studio.commons.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;

public class AISettings extends JsonSettings {

  static final String SETTINGS_FILE_NAME = "ai-settings.json";

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  static AISettings load(@NonNull File studioFolder) {
    return JsonSettings.load(new File(studioFolder, SETTINGS_FILE_NAME), AISettings.class);
  }
}
