package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// Column header/body alignment override for a repeat overview column, e.g. {"head": "left", "body": "left"}.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Alignment {

  private String head;
  private String body;
}
