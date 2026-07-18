package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

// The action triggered by a default row interaction (e.g. a row click), such as opening a detail screen.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RowAction {

  private String event;
}
