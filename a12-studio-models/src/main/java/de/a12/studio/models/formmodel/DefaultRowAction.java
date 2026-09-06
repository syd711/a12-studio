package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// The action triggered by a default row interaction (e.g. a row click), such as opening a detail screen.
// Renamed from a bare "RowAction" to avoid confusion with the richer RowAction used inside
// AbstractRepeat.getRowActionGroup() - a JSON key rename would be a wire-format change, but this is a
// same-project Java rename only (no @JsonTypeInfo on this class), so the wire key ("defaultRowAction",
// defined by the owning field) is unaffected.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DefaultRowAction {

  private String event;
  // A row action the user configured explicitly (as opposed to one of the built-in defaults like "edit").
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean custom;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean hideButton;
}
