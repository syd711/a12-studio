package de.a12.studio.models.util;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

public abstract class YamlSettings {

  public final static ObjectMapper objectMapper;

  static {
    objectMapper = YAMLMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
  }
}
