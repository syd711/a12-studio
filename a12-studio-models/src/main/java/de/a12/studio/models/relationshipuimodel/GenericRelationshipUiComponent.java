package de.a12.studio.models.relationshipuimodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

/** Fallback for a {@code componentType} not among the known ones; mirrors {@code documentmodel.GenericFieldType}. */
public class GenericRelationshipUiComponent extends RelationshipUiComponent {

  private final Map<String, Object> config = new LinkedHashMap<>();

  @JsonAnySetter
  public void setConfig(String name, Object value) {
    config.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getConfig() {
    return config;
  }
}
