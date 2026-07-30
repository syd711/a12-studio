package de.a12.studio.models.projects.settings.annotations;

import java.util.HashMap;
import java.util.Map;

/**
 * The header annotation names used across a project, grouped by model type via {@link AnnotationModelSet}.
 */
public class AnnotationHeaderSet {
  private Map<String, AnnotationModelSet> modelTypes = new HashMap<>();

  public Map<String, AnnotationModelSet> getModelTypes() {
    return modelTypes;
  }

  public void setModelTypes(Map<String, AnnotationModelSet> modelTypes) {
    this.modelTypes = modelTypes;
  }
}
