package de.a12.studio.models.projects.settings.annotations;

import java.util.Map;
import java.util.TreeMap;

public class AnnotationModelSet {
  private String modelType;
  private Map<String, NameUsage> values = new TreeMap<>();

  public String getModelType() {
    return modelType;
  }

  public void setModelType(String modelType) {
    this.modelType = modelType;
  }

  public Map<String, NameUsage> getValues() {
    return values;
  }

  public void setValues(Map<String, NameUsage> values) {
    this.values = values;
  }
}
