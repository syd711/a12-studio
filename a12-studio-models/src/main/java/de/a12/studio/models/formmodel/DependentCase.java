package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DependentCase {

  // The value of the master field this case applies to; null is a meaningful, distinct case (the master
  // field itself being unset/empty), so this must not be omitted from serialization when null.
  private String masterValue;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean notRelevant;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  // Ids of form nodes (Section, ControlGrid, etc.) that are hidden when this case applies.
  // Populated by the confirm-field node editor's Dependencies tab tree selection.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> notRelevantNodes = new ArrayList<>();
}
