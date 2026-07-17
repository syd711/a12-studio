package de.a12.studio.dataservices.projects;

import de.a12.studio.commons.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UISettings extends JsonSettings {

  static final String SETTINGS_FILE_NAME = "ui-settings.json";

  private double dividerPosition = 0.3;

  private List<String> openedFiles = new ArrayList<>();

  private String selectedFile;

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
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

  static UISettings load(@NonNull File studioFolder) {
    return JsonSettings.load(new File(studioFolder, SETTINGS_FILE_NAME), UISettings.class);
  }
}
