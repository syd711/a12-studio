package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomFieldFieldType extends FieldType {

  @JsonProperty("CustomFieldType")
  private CustomFieldTypeOptions customFieldType = new CustomFieldTypeOptions();
}
