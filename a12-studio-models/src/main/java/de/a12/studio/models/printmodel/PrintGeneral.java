package de.a12.studio.models.printmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// The list fields stay null when their key is absent in the file (older/minimal print models omit
// sections/watermarks/...); NON_NULL keeps them absent on save instead of adding empty arrays.
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class PrintGeneral extends PrintNode {

  private PrintMetadata metadata;
  // Order of segment ids here defines the segment order of the document.
  private List<PrintStructureEntry> structure;
  private SegmentDefaults segmentDefaults;
  private List<Object> sections;
  private List<Object> watermarks;
  private List<Object> runtimeVariables;
  private List<Object> textStyles;
}
