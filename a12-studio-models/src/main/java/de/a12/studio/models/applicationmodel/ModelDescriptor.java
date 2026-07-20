package de.a12.studio.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.ModelType;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ModelDescriptor {

  private ModelType modelType;
  private String name;
  // Set for modelType "form" or "tree" in case of a heterogeneous document model hierarchy.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String documentModel;
}
