package de.a12.studio.dataservices.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.dataservices.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Menu {

  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private InitialActivity initialActivity;
  // Comma-separated role names; matches the raw "permission" property on disk (the SME editor splits/joins
  // this into a roles list for its own UI, but the file format keeps it as a single string).
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String permission;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Menu> children = new ArrayList<>();
}
