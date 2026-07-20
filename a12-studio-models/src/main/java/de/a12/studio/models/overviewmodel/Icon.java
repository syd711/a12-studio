package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Icon {

  public static final String THEME_FILLED = "filled";
  public static final String THEME_OUTLINED = "outlined";
  public static final String THEME_ROUNDED = "rounded";
  public static final String THEME_CUSTOM = "custom";

  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String theme;
}
