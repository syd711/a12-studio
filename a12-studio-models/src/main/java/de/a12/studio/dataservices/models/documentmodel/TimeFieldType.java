package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeFieldType extends FieldType {

  @JsonProperty("TimeType")
  private TimeTypeOptions timeType = new TimeTypeOptions();
}
