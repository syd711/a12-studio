package de.a12.studio.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class Constraints {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String type;
  // Preferred display width (1-11) of the added view when constraints type is "MasterDetail"; if unset,
  // the available space is split evenly, equivalent to a preferred width of 6.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer preferredWidth;

  // `constraints` is genuinely free-form JSON on a View Add directive in SME - anything besides type/preferredWidth
  // (e.g. Dashboard tile sizing/label config) must round-trip unharmed even though the studio only has UI for the
  // MasterDetail shape.
  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }
}
