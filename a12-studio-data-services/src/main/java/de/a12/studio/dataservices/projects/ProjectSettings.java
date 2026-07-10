package de.a12.studio.dataservices.projects;

import de.a12.studio.commons.util.JsonSettings;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ProjectSettings extends JsonSettings {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private double dividerPosition = 0.3;

  private List<String> openedFiles = new ArrayList<>();

  private File settingsFile;

  @Override
  public String getSettingsName() {
    return ".studio.settings";
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

  public static ProjectSettings load(@NonNull File projectFolder) {
    File settingsFile = new File(projectFolder, new ProjectSettings().getSettingsName());
    ProjectSettings settings = null;

    if (settingsFile.exists()) {
      try {
        String json = Files.readString(settingsFile.toPath(), StandardCharsets.UTF_8);
        settings = fromJson(ProjectSettings.class, json);
      }
      catch (Exception e) {
        LOG.warn("Failed to read project settings from {}: {}", settingsFile.getAbsolutePath(), e.getMessage(), e);
      }
    }

    if (settings == null) {
      settings = new ProjectSettings();
    }

    settings.settingsFile = settingsFile;
    if (!settingsFile.exists()) {
      settings.save();
    }
    return settings;
  }

  public void save() {
    try {
      Files.writeString(settingsFile.toPath(), toJson(), StandardCharsets.UTF_8);
    }
    catch (Exception e) {
      LOG.error("Failed to write project settings to {}: {}", settingsFile.getAbsolutePath(), e.getMessage(), e);
    }
  }
}
