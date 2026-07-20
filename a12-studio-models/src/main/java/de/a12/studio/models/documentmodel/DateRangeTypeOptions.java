package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class DateRangeTypeOptions {

  private String rangeSeparator;
  private String format;
  private Boolean youngerThan1900Check;
  private String interpretationOfYear;
  private String notInDCustomFormat;
  private String notInDCustomRangeSeparator;
}
