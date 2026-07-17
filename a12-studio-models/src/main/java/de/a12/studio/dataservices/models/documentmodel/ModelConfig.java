package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ModelConfig {

  private String timeZone;
  private String decimalSeparator;
  private ConditionLanguage conditionLanguage;
  private String supportedCharacters;
}
