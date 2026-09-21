package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@code binding.details.metaInformation} of a {@link Binding} screen element. */
@Getter
@Setter
public class BindingMetaInformation {

  private String version;

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
