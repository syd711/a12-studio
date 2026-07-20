package de.a12.studio.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelReference {

  private String alias;
  private ModelType modelType;
  private String purpose;
  private String reference;
}
