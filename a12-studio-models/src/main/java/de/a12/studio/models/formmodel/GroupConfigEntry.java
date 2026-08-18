package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Model-wide configuration for a Document Model repeatable group, applying to every Repeat referencing it
// (analogous to FieldConfigEntry for fields). label/hint/placeholder can be overridden at the Repeat level.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class GroupConfigEntry {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DependentConfig dependentGroup;
  private String groupRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer numberOfInitialRows;
  // Model-wide label, hint and placeholder for the group (override-able at each Repeat node level).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText label;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer hint;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer placeholder;
}
