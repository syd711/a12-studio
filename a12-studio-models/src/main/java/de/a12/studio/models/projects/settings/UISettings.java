package de.a12.studio.models.projects.settings;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UISettings extends JsonSettings {

  static final String SETTINGS_FOLDER_NAME = ".a12-studio";

  static final String SETTINGS_FILE_NAME = "ui-settings.json";

  private double dividerPosition = 0.3;

  private List<String> openedFiles = new ArrayList<>();

  private String selectedFile;

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

  public static UISettings load() {
    File homeFolder = resolveSettingsFolder(new File(System.getProperty("user.home")), SETTINGS_FOLDER_NAME);
    return JsonSettings.load(new File(homeFolder, SETTINGS_FILE_NAME), UISettings.class);
  }
}
