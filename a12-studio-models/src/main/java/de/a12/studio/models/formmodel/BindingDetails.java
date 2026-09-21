package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code binding.details} of a {@link Binding} screen element: the relationship linkage fields modeled here
 * ({@code name} - SME's "Binding Name" display label, {@code relationshipName}, {@code targetRole}, {@code
 * metaInformation}). SME's {@code I_Binding} carries several more fields under {@code details} (the
 * dropdown/dual-pane/table-list component configuration, CDM child-activity toggles, edit-modal config) that
 * aren't modeled yet; they are kept verbatim in {@link #getExtras()}, so they survive a load-then-save.
 */
@Getter
@Setter
public class BindingDetails {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String relationshipName;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String targetRole;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingMetaInformation metaInformation;

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
