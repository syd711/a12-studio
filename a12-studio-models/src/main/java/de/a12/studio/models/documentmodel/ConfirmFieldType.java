package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmFieldType extends FieldType {

  @JsonProperty("ConfirmType")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private ConfirmTypeOptions confirmType;

  public ConfirmFieldType() {
    setType("ConfirmType");
  }
}
