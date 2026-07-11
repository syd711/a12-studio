package de.a12.studio.commons.util;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
public abstract class JsonSettings {
  public final static ObjectMapper objectMapper;

  static {
    Separators separators = Separators.createDefaultInstance()
        .withObjectNameValueSpacing(Separators.Spacing.AFTER)
        .withArrayEmptySeparator("")
        .withObjectEmptySeparator("");
    DefaultIndenter indenter = new DefaultIndenter("  ", "\n");
    DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter(separators)
        .withObjectIndenter(indenter)
        .withArrayIndenter(indenter);

    objectMapper = JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
        .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
        // Jackson 3 defaults this to true, which reorders every property alphabetically instead of
        // preserving declaration order, silently reformatting any file it re-saves.
        .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        // Match the "key": value / one-array-item-per-line / LF style the files on disk already use,
        // instead of Jackson's classic "key" : value with CRLF that would reformat every saved file.
        .defaultPrettyPrinter(prettyPrinter)
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
