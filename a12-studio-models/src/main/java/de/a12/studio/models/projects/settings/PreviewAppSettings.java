package de.a12.studio.models.projects.settings;

/**
 * The {@code previewApp} section of a project-root {@code settings.json}.
 * Serialised/deserialised by Jackson inside {@link ProjectRootSettings}.
 */
public class PreviewAppSettings {

  public enum BrowserType {
    SYSTEM_DEFAULT,
    CHROME,
    FIREFOX,
    EDGE
  }

  private BrowserType browserType = BrowserType.SYSTEM_DEFAULT;

  private boolean autoRefreshEnabled = true;

  private int autoRefreshDelayMillis = 500;

  private String url = "http://localhost:8082/api";

  private String username = "admin";

  private String password = "a12";

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

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
