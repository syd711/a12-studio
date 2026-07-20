package de.a12.studio.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericDirective extends Directive {

  private final Map<String, Object> config = new LinkedHashMap<>();

  public GenericDirective() {
    setType(DirectiveType.OTHER);
  }

  @JsonAnySetter
  public void setConfig(String name, Object value) {
    config.put(name, value);
  }

  public Map<String, Object> getConfig() {
    return config;
  }
}
