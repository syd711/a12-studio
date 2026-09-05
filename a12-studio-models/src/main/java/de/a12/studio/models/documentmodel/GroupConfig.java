package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class GroupConfig {

  public static final String USAGE_TYPE_ATTACHMENT = "attachment";
  public static final String USAGE_TYPE_MULTI_SELECT = "multi-select";

  private Integer repeatability;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String usageType;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String indexFieldName;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private IncludeConfig includeConfig;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Element> elements = new ArrayList<>();
}
