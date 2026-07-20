package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DateFieldType extends FieldType {

  @JsonProperty("DateType")
  private DateTypeOptions dateType = new DateTypeOptions();
}
