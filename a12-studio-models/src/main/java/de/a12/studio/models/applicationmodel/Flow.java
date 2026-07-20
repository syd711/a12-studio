package de.a12.studio.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Flow {

  private String name;
  private List<Scene> scenes = new ArrayList<>();
}
