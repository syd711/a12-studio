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
public class Scene {

  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String description;
  // The name of the scene that must have been shown directly before this one; acts as an additional match condition.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String priorScene;
  // Name of a case in this scene that should be treated as the default.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String defaultCase;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<MatchCondition> matchConditions = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private SceneChange sceneChange;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Case> cases = new ArrayList<>();
}
