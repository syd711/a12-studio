package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TreeNodeColumn {

  private String columnRef;
  private String elementRef;
  // Per-node override of the referenced column's display, e.g. {"attachmentDisplayMode": "preview"} or
  // {"multiSelectDisplayMode": "comma_separated"} - see overviewmodel.Column's constants for the known
  // values. No editor UI yet - mapped purely for lossless round-tripping.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Configuration configuration;

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  public static class Configuration {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String attachmentDisplayMode;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String multiSelectDisplayMode;
  }
}
