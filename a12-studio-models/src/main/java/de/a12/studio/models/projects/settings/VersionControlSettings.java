package de.a12.studio.models.projects.settings;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class VersionControlSettings extends de.a12.studio.models.util.JsonSettings {

  static final String SETTINGS_FOLDER_NAME = ".a12-studio";

  static final String SETTINGS_FILE_NAME = "versioncontrol-settings.json";

  private boolean enabled = true;

  /** Last commit message typed/used per project, keyed by the project folder's absolute path. */
  private Map<String, String> lastCommitMessages = new LinkedHashMap<>();

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.VERSION_CONTROL;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Map<String, String> getLastCommitMessages() {
    return lastCommitMessages;
  }

  public void setLastCommitMessages(Map<String, String> lastCommitMessages) {
    this.lastCommitMessages = lastCommitMessages;
  }

  public static VersionControlSettings load() {
    File homeFolder = resolveSettingsFolder(new File(System.getProperty("user.home")), SETTINGS_FOLDER_NAME);
    return de.a12.studio.models.util.JsonSettings.load(new File(homeFolder, SETTINGS_FILE_NAME), VersionControlSettings.class);
  }
}
