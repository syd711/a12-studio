package de.a12.studio.models.projects.settings;

import java.io.File;

public class JsonSettings extends de.a12.studio.models.util.JsonSettings {

  static final String SETTINGS_FOLDER_NAME = ".a12-studio";

  static final String SETTINGS_FILE_NAME = "ai-settings.json";

  public enum ClaudePathMode {
    OPEN_FROM_PATH,
    CONFIGURE_PATH
  }

  private boolean addClaudeConsoleButton = true;

  private ClaudePathMode claudePathMode = ClaudePathMode.OPEN_FROM_PATH;

  private String claudeExecutablePath;

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.AI;
  }

  public boolean isAddClaudeConsoleButton() {
    return addClaudeConsoleButton;
  }

  public void setAddClaudeConsoleButton(boolean addClaudeConsoleButton) {
    this.addClaudeConsoleButton = addClaudeConsoleButton;
  }

  public ClaudePathMode getClaudePathMode() {
    return claudePathMode;
  }

  public void setClaudePathMode(ClaudePathMode claudePathMode) {
    this.claudePathMode = claudePathMode;
  }

  public String getClaudeExecutablePath() {
    return claudeExecutablePath;
  }

  public void setClaudeExecutablePath(String claudeExecutablePath) {
    this.claudeExecutablePath = claudeExecutablePath;
  }

  public static JsonSettings load() {
    File homeFolder = resolveSettingsFolder(new File(System.getProperty("user.home")), SETTINGS_FOLDER_NAME);
    return de.a12.studio.models.util.JsonSettings.load(new File(homeFolder, SETTINGS_FILE_NAME), JsonSettings.class);
  }
}
