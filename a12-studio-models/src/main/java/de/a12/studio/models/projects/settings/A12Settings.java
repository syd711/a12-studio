package de.a12.studio.models.projects.settings;

import de.a12.studio.models.util.JsonSettings;

import java.io.File;

public class A12Settings extends JsonSettings {

  static final String SETTINGS_FOLDER_NAME = ".a12-studio";

  static final String SETTINGS_FILE_NAME = "a12-installation-settings.json";

  private static final String SIMPLE_MODEL_EDITOR_PREFIX = "Simple Model Editor";

  private String installationPath;

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.A12_INSTALLATION;
  }

  public String getInstallationPath() {
    return installationPath;
  }

  public void setInstallationPath(String installationPath) {
    this.installationPath = installationPath;
  }

  public static boolean isValidInstallationFolder(File folder) {
    if (folder == null || !folder.isDirectory()) {
      return false;
    }
    if (!new File(folder, "bin").isDirectory() || !new File(folder, "licenses").isDirectory()) {
      return false;
    }
    String[] entries = folder.list();
    if (entries == null) {
      return false;
    }
    return java.util.Arrays.stream(entries).anyMatch(name -> name.startsWith(SIMPLE_MODEL_EDITOR_PREFIX));
  }

  public static A12Settings load() {
    File homeFolder = resolveSettingsFolder(new File(System.getProperty("user.home")), SETTINGS_FOLDER_NAME);
    return JsonSettings.load(new File(homeFolder, SETTINGS_FILE_NAME), A12Settings.class);
  }
}
