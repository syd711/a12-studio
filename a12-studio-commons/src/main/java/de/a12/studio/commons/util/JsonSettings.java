package de.a12.studio.commons.util;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
public abstract class JsonSettings {
  public final static ObjectMapper objectMapper;

  static {
    objectMapper = JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
        .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
        .build();
  }

  public static <T> T fromJson(Class<T> clazz, String json) throws Exception {
    try {
      T t = objectMapper.readValue(json, clazz);
      if (t != null) {
        return t;
      }
    } catch (Exception e) {
      log.warn("Error parsing settings json \"{}\" for class \"{}\": {}. Creating a plain new instance instead.", json, clazz, e.getMessage());
    }
    return clazz.getDeclaredConstructor().newInstance();
  }

  public String toJson() throws JacksonException { // Updated throws clause
    return objectMapper.writeValueAsString(this);
  }

  public abstract String getSettingsName();
}
