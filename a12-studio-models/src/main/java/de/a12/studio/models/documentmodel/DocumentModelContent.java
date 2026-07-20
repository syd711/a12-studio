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
public class DocumentModelContent {

  private ModelInfo modelInfo;
  private ModelConfig modelConfig;
  private ModelRoot modelRoot;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<TypeDefinition> typeDefinitions = new ArrayList<>();
}
