package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnumerationFieldType extends FieldType {

  @JsonProperty("EnumerationType")
  private EnumerationTypeOptions enumerationType;
}
