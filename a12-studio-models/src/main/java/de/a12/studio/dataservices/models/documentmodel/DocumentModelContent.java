package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DocumentModelContent {

  private ModelInfo modelInfo;
  private ModelConfig modelConfig;
  private ModelRoot modelRoot;
}
