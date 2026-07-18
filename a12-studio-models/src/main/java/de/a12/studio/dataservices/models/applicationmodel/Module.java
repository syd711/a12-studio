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
public class Module {

  private String name;
  // Leaving the menu name empty means the module has no entry in the navigation, per the app model docs.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Menu menu;
  private List<Flow> flows = new ArrayList<>();
}
