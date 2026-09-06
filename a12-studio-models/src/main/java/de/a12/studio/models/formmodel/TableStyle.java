package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TableStyle {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer rowHeight;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer tableHeight;
  // Card-view row height, used when the repeat renders as cards instead of a table (e.g. narrow breakpoints).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer cardHeight;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer actionColumnWidth;
}
