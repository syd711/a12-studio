package de.a12.studio.models.projects.settings;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UISettings extends JsonSettings {

  static final String SETTINGS_FOLDER_NAME = ".a12-studio";

  static final String SETTINGS_FILE_NAME = "ui-settings.json";

  /** Cap for {@link #recentFiles}; see {@link #addRecentFile(String)}. */
  public static final int MAX_RECENT_FILES = 20;

  private double dividerPosition = 0.3;

  private List<String> openedFiles = new ArrayList<>();

  private String selectedFile;

  /** Most-recently-edited files, most recent first, capped at {@link #MAX_RECENT_FILES}. Backs the "Recent Files" palette (Ctrl+E). */
  private List<String> recentFiles = new ArrayList<>();

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.UI;
  }

  public double getDividerPosition() {
    return dividerPosition;
  }

  public void setDividerPosition(double dividerPosition) {
    this.dividerPosition = dividerPosition;
  }

  public List<String> getOpenedFiles() {
    return openedFiles;
  }

  public void setOpenedFiles(List<String> openedFiles) {
    this.openedFiles = openedFiles;
  }

  public void addOpenedFile(@NonNull String path) {
    openedFiles.remove(path);
    openedFiles.add(path);
  }

  public void removeOpenedFile(@NonNull String path) {
    openedFiles.remove(path);
  }

  public String getSelectedFile() {
    return selectedFile;
  }

  public void setSelectedFile(String selectedFile) {
    this.selectedFile = selectedFile;
  }

  public List<String> getRecentFiles() {
    return recentFiles;
  }

  public void setRecentFiles(List<String> recentFiles) {
    this.recentFiles = recentFiles;
  }

  public void addRecentFile(@NonNull String path) {
    recentFiles.remove(path);
    recentFiles.add(0, path);
    while (recentFiles.size() > MAX_RECENT_FILES) {
      recentFiles.remove(recentFiles.size() - 1);
    }
  }

  public void removeRecentFile(@NonNull String path) {
    recentFiles.remove(path);
  }

  public static UISettings load() {
    File homeFolder = resolveSettingsFolder(new File(System.getProperty("user.home")), SETTINGS_FOLDER_NAME);
    return JsonSettings.load(new File(homeFolder, SETTINGS_FILE_NAME), UISettings.class);
  }
}
