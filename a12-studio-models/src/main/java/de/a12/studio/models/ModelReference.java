package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  // A Form Model's reference to the Document Model it binds its Controls' data to, matching SME's
  // FormModelFrame convention (see e.g. Invoice_FM.json's modelReferences entry).
  public static final String PURPOSE_DATA_BINDING = "data binding";
  // A Content Model's reference to the Document Model whose data its elements are bound to, matching SME's
  // ExportTransformations.transformModelReferences (alias "DM") and real fixtures (e.g. Product_OfBundle_CM.json).
  public static final String PURPOSE_DOCUMENT_MODEL_FOR_CONTENT_MODEL = "document-model-for-content-model";
  // A Query Model's reference to the Document Model it projects fields from, matching QueryModel.json's
  // modelReferences entry.
  public static final String PURPOSE_DOCUMENT_MODEL_FOR_QUERY = "document-model-for-query";
  // A Relationship UI Model's references to the Overview/Query/Form Models its component configuration
  // points at, matching real fixtures (e.g. Teammembers_Ru.json, ParentTeam_Ru.json) exactly.
  public static final String PURPOSE_RELATIONSHIP_UI_AVAILABLE_ITEMS = "availableItems";
  public static final String PURPOSE_RELATIONSHIP_UI_SELECTED_ITEMS = "selectedItems";
  public static final String PURPOSE_RELATIONSHIP_UI_LINK = "link";
  public static final String PURPOSE_RELATIONSHIP_UI_AVAILABLE_ITEMS_IN_EDIT_MODAL = "availableItemsInEditModal";
  public static final String PURPOSE_RELATIONSHIP_UI_SELECTED_ITEMS_IN_EDIT_MODAL = "selectedItemsInEditModal";
  public static final String PURPOSE_RELATIONSHIP_UI_AVAILABLE_ITEMS_QUERY = "availableItemsQuery";
  public static final String PURPOSE_RELATIONSHIP_UI_SELECTED_ITEM_QUERY = "selectedItemQuery";

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String alias;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String purpose;

  // ModelType.fromValue() returns null (by design, see its javadoc) for a real a12 platform type
  // a12-studio has no editor for yet (e.g. "additive-document", "composed-document"). unresolvedModelType
  // keeps the original JSON string in that case so saving a reference to such a type doesn't corrupt it
  // into a null modelType - see getModelTypeForJson/setModelTypeForJson below.
  @JsonIgnore
  private ModelType modelType;
  @JsonIgnore
  private String unresolvedModelType;

  private String reference;

  @JsonProperty("modelType")
  private String getModelTypeForJson() {
    return modelType != null ? modelType.getValue() : unresolvedModelType;
  }

  @JsonProperty("modelType")
  private void setModelTypeForJson(String value) {
    this.modelType = ModelType.fromValue(value);
    this.unresolvedModelType = modelType == null ? value : null;
  }
}
