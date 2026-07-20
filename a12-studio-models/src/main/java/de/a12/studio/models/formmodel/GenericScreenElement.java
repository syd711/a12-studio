package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericScreenElement extends ScreenElement {

  private final Map<String, Object> config = new LinkedHashMap<>();

  public GenericScreenElement() {
    setType(ScreenElementType.OTHER);
  }

  @JsonAnySetter
  public void setConfig(String name, Object value) {
    config.put(name, value);
  }

  public Map<String, Object> getConfig() {
    return config;
  }
}
