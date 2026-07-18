package de.a12.studio.dataservices.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericBoxElement extends BoxElement {

  private final Map<String, Object> config = new LinkedHashMap<>();

  public GenericBoxElement() {
    setType(BoxElementType.OTHER);
  }

  @JsonAnySetter
  public void setConfig(String name, Object value) {
    config.put(name, value);
  }

  public Map<String, Object> getConfig() {
    return config;
  }
}
