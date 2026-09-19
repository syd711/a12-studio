package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// SME's "Control Index": picks the repetition of a repeatable group a Control outside that repeat shows, either
// by position (TYPE_NUMERIC, the number of the row) or by the value of the group's index field
// (TYPE_SEMANTIC). "type" stays a plain string so a value SME might add later survives a round trip.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ControlIndex {

  public static final String TYPE_SEMANTIC = "SEMANTIC";
  public static final String TYPE_NUMERIC = "NUMERIC";

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String type;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String value;
}
