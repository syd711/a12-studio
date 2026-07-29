package de.a12.studio.models.projects.settings.annotations;

import java.util.HashMap;
import java.util.Map;

public class AnnotationSet {
  private String name;
  private Map<String, AnnotationModelSet> modelSets = new HashMap<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, AnnotationModelSet> getModelSets() {
    return modelSets;
  }

  public void setModelSets(Map<String, AnnotationModelSet> modelSets) {
    this.modelSets = modelSets;
  }
}
