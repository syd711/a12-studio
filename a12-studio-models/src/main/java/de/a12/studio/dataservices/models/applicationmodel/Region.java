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
public class Region {

  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Layout layout;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Region> subRegions = new ArrayList<>();
}
