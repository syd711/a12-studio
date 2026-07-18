package de.a12.studio.dataservices.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ElementBox {

  private List<BoxElement> majorElements = new ArrayList<>();
  private List<BoxElement> minorElements = new ArrayList<>();
}
