package de.a12.studio.dataservices.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class MatchCondition {

  private String key;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String mustEqual;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean isSet;
}
