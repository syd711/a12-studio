package de.a12.studio.models.projects.settings.annotations;

import java.util.HashMap;
import java.util.Map;

/**
 * The annotation names used across a project, grouped by field type (see
 * {@link AnnotationFieldRegistry#resolveFieldType}), each further grouped by model type via {@link AnnotationSet}.
 */
public class AnnotationFieldSet {
  private Map<String, AnnotationSet> fieldTypes = new HashMap<>();

  public Map<String, AnnotationSet> getFieldTypes() {
    return fieldTypes;
  }

  public void setFieldTypes(Map<String, AnnotationSet> fieldTypes) {
    this.fieldTypes = fieldTypes;
  }
}
