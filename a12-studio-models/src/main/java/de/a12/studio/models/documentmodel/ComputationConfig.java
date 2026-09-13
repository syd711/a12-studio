package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ComputationConfig {

  private String computedFieldRelPath;
  // A precondition shared by every ComputationAlternative, evaluated in addition to each alternative's own
  // precondition. Edited via ComputationOptionsPanelController's "Common Precondition" checkbox + text area.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String commonPrecondition;
  private List<ComputationAlternative> computationAlternatives = new ArrayList<>();
  private List<Label> errorMessage = new ArrayList<>();
  // Kernel validation error codes to suppress for this computation. Only "MVK_INVALID_COMPARE_DEC_PLACES" has
  // editor UI (ComputationOptionsPanelController's "Allow Differing Decimal Places" checkbox) - any other code
  // present here (e.g. from a file authored elsewhere) round-trips losslessly but isn't otherwise surfaced.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> errorCodesToSuppress = new ArrayList<>();
}
