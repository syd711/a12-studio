package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// References one of the dependent field's own Enumeration literals by value; deliberately does not carry a
// label (unlike documentmodel.EnumerationValue) since it only selects among values already defined there.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class EnumerationConstraintValue {

  private String value;
}
