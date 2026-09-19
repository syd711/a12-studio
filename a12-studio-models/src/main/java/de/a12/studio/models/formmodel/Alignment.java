package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Column header/body alignment override for a repeat overview column, e.g. {"head": "left", "body": "left"}.
// Horizontal values: left/center/right; vertical values: top/middle/bottom. An absent side means "default".
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Alignment {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String head;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String body;

  /** Whether neither side is set, i.e. this object would serialize as an empty {@code {}}. */
  @JsonIgnore
  public boolean isBlank() {
    return (head == null || head.isEmpty()) && (body == null || body.isEmpty());
  }
}
