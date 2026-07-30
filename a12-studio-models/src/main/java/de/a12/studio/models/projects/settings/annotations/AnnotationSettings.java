package de.a12.studio.models.projects.settings.annotations;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnnotationSettings extends JsonSettings {

  static final String SETTINGS_FILE_NAME = "annotation-settings.json";

  private List<AnnotationDataSet> dataSets = new ArrayList<>();

  @Override
  public String getSettingsName() {
    return SETTINGS_FILE_NAME;
  }

  @Override
  public SettingsType getSettingsType() {
    return SettingsType.ANNOTATION;
  }

  public List<AnnotationDataSet> getDataSets() {
    return dataSets;
  }

  public void setDataSets(List<AnnotationDataSet> dataSets) {
    this.dataSets = dataSets;
  }

  public static AnnotationSettings load(@NonNull File studioFolder) {
    return JsonSettings.load(new File(studioFolder, SETTINGS_FILE_NAME), AnnotationSettings.class);
  }
}
