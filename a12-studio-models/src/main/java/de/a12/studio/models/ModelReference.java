package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelReference {

  public static final String PURPOSE_INCLUDE = "include";
  public static final String PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW = "document-model-for-overview";
  // An Overview Model's alternative to PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW: references a Query Model
  // instead of a Document Model directly. The Query Model's own targetDocumentModel is still mirrored into
  // a PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW reference (see OverviewReferencePanelController), so this purpose
  // only needs to be checked to tell which of the two modes is active.
  public static final String PURPOSE_QUERY_MODEL_FOR_OVERVIEW = "query-model-for-overview";
  public static final String PURPOSE_DOCUMENT_MODEL = "Document model";
  public static final String PURPOSE_DOCUMENT_MODEL_FOR_TREE = "document-model-for-tree";
  // Matches SME's DocumentModelExpansion.importPurpose exactly: a header reference of this purpose means
  // "import every type definition owned by the referenced Type Definition Model", as opposed to an "include"
  // reference (which inlines a whole other document model's element tree via a Group's includeConfig).
  public static final String PURPOSE_TYPE_DEFINITIONS = "typeDefinitions";

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String alias;
  private ModelType modelType;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String purpose;
  private String reference;
}
