package de.a12.studio.models.applicationmodel;

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
  // Both scenes and cases can carry onExit directives (run when the scene, or the active case within it,
  // stops matching); this type is shared between the two.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Directive> onExit = new ArrayList<>();
}
