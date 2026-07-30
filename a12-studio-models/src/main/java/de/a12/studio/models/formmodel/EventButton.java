package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({"type", "id", "name", "buttonStyling", "event", "validation", "scope", "enablement"})
public class EventButton extends Button {

  // Custom event name handled by the application embedding this form.
  private String event;
  // How the button behaves before any data has changed, e.g. "HIDDEN" or "DISABLED".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String enablement;

  public EventButton() {
    setType(ButtonType.EVENT);
  }
}
