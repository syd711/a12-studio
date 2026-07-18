package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericRepeatOverviewColumn extends RepeatOverviewColumn {

  private final Map<String, Object> config = new LinkedHashMap<>();

  public GenericRepeatOverviewColumn() {
    setType(RepeatOverviewColumnType.OTHER);
  }

  @JsonAnySetter
  public void setConfig(String name, Object value) {
    config.put(name, value);
  }

  public Map<String, Object> getConfig() {
    return config;
  }
}
