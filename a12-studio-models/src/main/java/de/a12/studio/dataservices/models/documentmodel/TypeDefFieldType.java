package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeDefFieldType extends FieldType {

  @JsonProperty("TypeDefType")
  private TypeDefTypeOptions typeDefType = new TypeDefTypeOptions();
}
