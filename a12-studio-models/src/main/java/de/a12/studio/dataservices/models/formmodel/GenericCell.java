package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericCell extends Cell {

  private final Map<String, Object> config = new LinkedHashMap<>();

  public GenericCell() {
    setType(CellType.OTHER);
  }

  @JsonAnySetter
  public void setConfig(String name, Object value) {
    config.put(name, value);
  }

  public Map<String, Object> getConfig() {
    return config;
  }
}
