package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnspecifiedFieldType extends FieldType {

  @JsonProperty("UnspecifiedType")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private UnspecifiedTypeOptions unspecifiedType;
}
