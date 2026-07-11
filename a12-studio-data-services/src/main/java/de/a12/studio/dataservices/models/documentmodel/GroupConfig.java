package de.a12.studio.dataservices.models.documentmodel;

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

  private Integer repeatability;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String usageType;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String modelAlias;
  private List<Element> elements = new ArrayList<>();
}
