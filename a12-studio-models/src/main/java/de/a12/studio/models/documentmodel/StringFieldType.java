package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StringFieldType extends FieldType {

  @JsonProperty("StringType")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private StringTypeOptions stringType;

  public StringFieldType() {
    setType("StringType");
  }
}
