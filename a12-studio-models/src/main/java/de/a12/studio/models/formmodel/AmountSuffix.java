package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// A global suffix (e.g. a currency symbol) used for number Controls with unit "amount"; either "static"
// (value is used verbatim) or dynamic (value instead references an enumeration field).
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class AmountSuffix {

  private String type;
  private String value;
}
