package de.a12.studio.dataservices.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class SceneChange {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Directive> onEnter = new ArrayList<>();
  // Cases never have onExit: they are mutually exclusive, so "another case following this one is active" can
  // never be satisfied. Still modeled here since scenes reuse this same type for their own onExit directives.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Directive> onExit = new ArrayList<>();
}
