package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> helperText = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private RequirednessConfig requirednessConfig;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean global;
  @JsonProperty("transient")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean transientField;
}
