package de.a12.studio.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ApplicationModelContent {

  private List<Module> modules = new ArrayList<>();
  private Region region;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> defaultRegion = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private InitialActivity initialActivity;
}
