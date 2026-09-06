package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldBasedRepeatOverviewColumn extends RepeatOverviewColumn {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String messageExposition;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DatePickerConfig datePickerConfig;
  private String elementRef;

  public FieldBasedRepeatOverviewColumn() {
    setType(RepeatOverviewColumnType.FIELD_BASED);
  }
}
