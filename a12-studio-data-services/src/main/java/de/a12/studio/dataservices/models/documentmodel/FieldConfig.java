package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.a12.studio.dataservices.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FieldConfig {

  private FieldType fieldType;
  private List<Label> label = new ArrayList<>();
  private List<Label> helperText = new ArrayList<>();
  private RequirednessConfig requirednessConfig;
}
