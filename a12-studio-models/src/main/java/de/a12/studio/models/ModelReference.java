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
  // Matches SME's DocumentModelExpansion.importPurpose exactly: a header reference of this purpose means
  // "import every type definition owned by the referenced Type Definition Model", as opposed to an "include"
  // reference (which inlines a whole other document model's element tree via a Group's includeConfig).
  public static final String PURPOSE_TYPE_DEFINITIONS = "typeDefinitions";

  private String purpose;
  private ModelType modelType;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String alias;
  private String reference;
}
