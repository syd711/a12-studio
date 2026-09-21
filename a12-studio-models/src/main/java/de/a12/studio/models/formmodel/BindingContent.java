package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@code binding} field of a {@link Binding} screen element, matching SME's {@code I_Binding} mixin's
 * {@code type}/{@code elementId}/{@code details} shape. {@code type} is always {@code "relationship"} today (the
 * only binding kind a12-studio creates), kept as a field rather than hardcoded so a real SME export using some
 * other value still round-trips it losslessly. Any other key is kept in {@link #getExtras()} for the same reason.
 */
@Getter
@Setter
public class BindingContent {

  private String type;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String elementId;
  private BindingDetails details = new BindingDetails();

  // Everything SME's I_Binding carries that isn't modeled above (UI-component configuration, CDM child-activity
  // wiring, edit-modal config, ...), kept verbatim so a load-then-save doesn't drop it.
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
