package de.a12.studio.models.structuralmappingmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FieldMapping {

  private String sourceFieldFullName;
  private String targetFieldFullName;
}
