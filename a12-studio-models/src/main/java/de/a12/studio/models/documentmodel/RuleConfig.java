package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RuleConfig {

  private String errorEntityRelPath;
  private String errorCode;
  private String errorCondition;
  private String severity;
  private List<Label> errorMessage = new ArrayList<>();
}
