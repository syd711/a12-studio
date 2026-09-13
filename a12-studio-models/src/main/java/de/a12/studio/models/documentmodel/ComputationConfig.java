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
  private List<ComputationAlternative> computationAlternatives = new ArrayList<>();
  private List<Label> errorMessage = new ArrayList<>();
  // Kernel validation error codes to suppress for this computation - no editor UI yet, mapped purely for
  // lossless round-tripping.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> errorCodesToSuppress = new ArrayList<>();
}
