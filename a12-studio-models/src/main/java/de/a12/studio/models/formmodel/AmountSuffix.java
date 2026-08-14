package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// A global suffix (e.g. a currency symbol) used for number Controls with unit "amount"; either "static"
// (value is used verbatim) or "dynamic" (value instead references a field via fieldRef).
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AmountSuffix {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String type;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String value;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String fieldRef;
}
