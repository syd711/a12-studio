package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Column widths per responsive breakpoint, e.g. "3-3-6" meaning three columns of width 3, 3 and 6 (out of 12).
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ColumnLayout {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String lg;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String md;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String sm;
}
