package de.a12.studio.models.projects.settings;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;

/**
 * Reads and writes the {@code settings.json} file located at the root of a project folder.
 *
 * <p>File structure (example):
 * <pre>
 * {
 *   "version": "1.0.0",
 *   "general": { ... },
 *   "previewApp": { "url": "http://localhost:8082/api", "username": "admin", "password": "a12", ... }
 * }
 * </pre>
 *
 * <p>Use {@link #load(File)} to obtain an instance, then call {@link #save()} after mutations.
 */
public class ProjectRootSettings extends JsonSettings {

  static final String SETTINGS_FILE_NAME = "settings.json";

  private String version = "1.0.0";

  private GeneralSettings general = new GeneralSettings();

  private PreviewAppSettings previewApp = new PreviewAppSettings();

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.PROJECT_ROOT;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public GeneralSettings getGeneral() {
    return general;
  }

  public void setGeneral(GeneralSettings general) {
    this.general = general;
  }

  public PreviewAppSettings getPreviewApp() {
    return previewApp;
  }

  public void setPreviewApp(PreviewAppSettings previewApp) {
    this.previewApp = previewApp;
  }

  /**
   * Loads (or creates) the {@code settings.json} at the root of the given project folder.
   *
   * @param projectFolder the root folder of the open project
   * @return a fully initialised {@link ProjectRootSettings} instance
   */
  public static ProjectRootSettings load(@NonNull File projectFolder) {
    return JsonSettings.load(new File(projectFolder, SETTINGS_FILE_NAME), ProjectRootSettings.class);
  }
}
