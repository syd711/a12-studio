package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class GroupConfig {

  private Integer repeatability;
  private String usageType;
  private String modelAlias;
  private List<Element> elements = new ArrayList<>();
}
