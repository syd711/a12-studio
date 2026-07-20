package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DatePickerConfig {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer minYear;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer maxYear;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean absolute;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer preselectionYear;
}
