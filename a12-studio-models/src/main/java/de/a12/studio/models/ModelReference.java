package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelReference {

  public static final String PURPOSE_INCLUDE = "include";
  public static final String PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW = "document-model-for-overview";
  public static final String PURPOSE_DOCUMENT_MODEL = "Document model";
  public static final String PURPOSE_DOCUMENT_MODEL_FOR_TREE = "document-model-for-tree";

  private String purpose;
  private ModelType modelType;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String alias;
  private String reference;
}
