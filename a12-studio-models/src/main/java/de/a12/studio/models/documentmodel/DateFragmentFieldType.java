package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DateFragmentFieldType extends FieldType {

  @JsonProperty("DateFragmentType")
  private DateFragmentTypeOptions dateFragmentType = new DateFragmentTypeOptions();

  public DateFragmentFieldType() {
    setType("DateFragmentType");
  }
}
