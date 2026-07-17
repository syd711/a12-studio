package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ModelConfig {

  private String timeZone;
  private String decimalSeparator;
  private ConditionLanguage conditionLanguage;
  private List<String> supportedCharacters = new ArrayList<>();
}
