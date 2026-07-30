package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Model-wide configuration for a Document Model field, applying to every Control/column referencing it
// (as opposed to per-Control settings, which take precedence).
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FieldConfigEntry {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText label;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer placeholder;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer hint;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String initialValue;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer suffix;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String exposition;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DependentConfig dependentField;
  private String elementRef;
}
