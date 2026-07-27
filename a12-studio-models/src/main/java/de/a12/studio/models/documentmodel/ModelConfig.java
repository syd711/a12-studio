package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ModelConfig {

  private String timeZone;
  private String decimalSeparator;
  private ConditionLanguage conditionLanguage;
  // null = key absent in the file; must not be written back as an empty array in that case.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> supportedCharacters;
}
