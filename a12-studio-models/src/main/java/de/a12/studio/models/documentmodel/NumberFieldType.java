package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NumberFieldType extends FieldType {

  @JsonProperty("NumberType")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private NumberTypeOptions numberType;
}
