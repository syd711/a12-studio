package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericElement extends Element {

  private final Map<String, Object> config = new LinkedHashMap<>();

  public GenericElement() {
    setType(ElementType.OTHER);
  }

  @JsonAnySetter
  public void setConfig(String name, Object value) {
    config.put(name, value);
  }

  public Map<String, Object> getConfig() {
    return config;
  }
}
