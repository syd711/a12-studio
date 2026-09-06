package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DependentEnumerationConstraint {

  // The value of the master field this constraint applies to; null is a meaningful, distinct case (the
  // master field itself being unset/empty), so this must not be omitted from serialization when null.
  private String masterValue;
  private List<EnumerationConstraintValue> constraintValues = new ArrayList<>();
  // The value to switch the dependent field to when the master field changes to masterValue, if its current
  // value is no longer among constraintValues.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String valueForMasterChange;
}
