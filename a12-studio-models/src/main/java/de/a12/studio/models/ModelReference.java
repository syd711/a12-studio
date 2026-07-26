package de.a12.studio.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelReference {

  public static final String PURPOSE_INCLUDE = "include";
  public static final String PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW = "document-model-for-overview";

  private String alias;
  private ModelType modelType;
  private String purpose;
  private String reference;
}
