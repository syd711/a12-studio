package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class IconRef {

  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String theme;
}
