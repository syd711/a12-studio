package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class HideConditionCase {

  // The value of the master field that triggers hiding; null is a meaningful, distinct case (the master
  // field itself being unset/empty), so this must not be omitted from serialization when null.
  private String masterValue;
}
