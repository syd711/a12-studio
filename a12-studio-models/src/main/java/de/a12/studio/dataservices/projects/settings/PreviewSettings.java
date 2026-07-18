package de.a12.studio.dataservices.projects.settings;

import de.a12.studio.commons.util.JsonSettings;

import java.io.File;

public class PreviewSettings extends JsonSettings {

  static final String SETTINGS_FOLDER_NAME = ".a12-studio";

  static final String SETTINGS_FILE_NAME = "preview-settings.json";

  public enum BrowserType {
    SYSTEM_DEFAULT,
    CHROME,
    FIREFOX,
    EDGE
  }

  private BrowserType browserType = BrowserType.SYSTEM_DEFAULT;

  private boolean autoRefreshEnabled = true;

  private int autoRefreshDelayMillis = 500;

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.PREVIEW;
  }

  public BrowserType getBrowserType() {
    return browserType;
  }

  public void setBrowserType(BrowserType browserType) {
    this.browserType = browserType;
  }

  public boolean isAutoRefreshEnabled() {
    return autoRefreshEnabled;
  }

  public void setAutoRefreshEnabled(boolean autoRefreshEnabled) {
    this.autoRefreshEnabled = autoRefreshEnabled;
  }

  public int getAutoRefreshDelayMillis() {
    return autoRefreshDelayMillis;
  }

  public void setAutoRefreshDelayMillis(int autoRefreshDelayMillis) {
    this.autoRefreshDelayMillis = autoRefreshDelayMillis;
  }

  public static PreviewSettings load() {
    File homeFolder = resolveSettingsFolder(new File(System.getProperty("user.home")), SETTINGS_FOLDER_NAME);
    return JsonSettings.load(new File(homeFolder, SETTINGS_FILE_NAME), PreviewSettings.class);
  }
}
