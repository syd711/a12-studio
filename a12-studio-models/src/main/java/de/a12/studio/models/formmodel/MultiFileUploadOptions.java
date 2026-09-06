package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Presentation config for a repeat that collects attachments (Inline/Embedded only - SME doesn't offer this
// on Detached repeats either).
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class MultiFileUploadOptions {

  private String elementRef;
  // Only hides/shows a download button in the row actions - not a general enablement flag.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableDownload;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer fileUploadDescription;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean hideFileUploadDescription;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer fileUploadButtonText;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean hideFileUploadButtonText;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer fileUploadHelperText;
}
