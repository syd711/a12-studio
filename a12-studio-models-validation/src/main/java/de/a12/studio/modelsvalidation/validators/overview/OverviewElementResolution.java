package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelElements;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnLinkReference;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;

import java.util.List;

/**
 * Shared field-reference resolution used by every Overview Model validator that checks an
 * {@code elementRef}/{@code fieldId} against the referenced Document Model: columns
 * ({@link OverviewFieldReferenceValidator}), the filter's custom field list, and filter sections.
 * Mirrors the SME rules that a referenced field must exist, must not be annotated {@code indexed}
 * = false, and must not live inside a repeatable group (its data isn't unique per document).
 */
public final class OverviewElementResolution {

  private OverviewElementResolution() {
  }

  /**
   * The Document Model this Overview Model's columns/filters resolve against: the one named by a header
   * {@code modelType: "document"} reference (following it through a Combination Model stand-in, e.g. {@code
   * PersonEmployee_Ov.json} -> {@code PersonEmployee_Cm}, via {@link
   * CombinedDocumentModelElements#resolveForFieldReferences}), or - when there's no such reference at all, or
   * it doesn't resolve - the target Document Model of the Query Model named by a {@link
   * ModelReference#PURPOSE_QUERY_MODEL_FOR_OVERVIEW} reference instead (a legitimate alternative binding, see
   * that constant's own doc; mirrors the Overview Model editor's own fallback, {@code
   * OverviewModelEditorController#currentDocumentModelId} in {@code a12-studio-ui}).
   */
  public static DocumentModel referencedDocumentModel(OverviewModel model, ValidationContext context) {
    if (model.getModelReferences() == null) {
      return null;
    }
    String documentModelId = model.getModelReferences().stream()
        .filter(reference -> reference.getModelType() == ModelType.DOCUMENT)
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
    DocumentModel explicit = resolveDocumentModelOrCombination(documentModelId, context);
    if (explicit != null) {
      return explicit;
    }
    String queryModelId = model.getModelReferences().stream()
        .filter(reference -> ModelReference.PURPOSE_QUERY_MODEL_FOR_OVERVIEW.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
    if (!(context.findOtherModel(queryModelId) instanceof QueryModel queryModel) || queryModel.getContent() == null) {
      return null;
    }
    return resolveDocumentModelOrCombination(queryModel.getContent().getTargetDocumentModel(), context);
  }

  /** {@code documentModelId} resolved as a plain Document Model, or - if it names a Combination Model instead
   * - the synthetic merge {@link CombinedDocumentModelElements#resolveForFieldReferences} makes of it. */
  public static DocumentModel resolveDocumentModelOrCombination(String documentModelId, ValidationContext context) {
    if (documentModelId == null) {
      return null;
    }
    DocumentModel direct = context.findOtherDocumentModel(documentModelId);
    return direct != null ? direct : CombinedDocumentModelElements.resolveForFieldReferences(context.projectItem(), documentModelId);
  }

  /**
   * The {@link ElementIndex} {@code column}'s {@code elementRef} actually resolves against: {@code
   * documentModelIndex} for a plain column, or - for a column carrying {@code linkReferences} (a
   * Relationship UI Model's Available/Selected Items overview projecting a field of the related document) -
   * an index over the Document Model the link reference points at instead (see {@link
   * ColumnLinkReference#resolveDocumentModelId}: the relationship's link document for a {@code LINK}
   * reference, the target role's document for a {@code CHILD} one), falling back to {@code
   * documentModelIndex} if the relationship or that model doesn't resolve. Mirrors {@code
   * OverviewColumnOptions#indexFor} in {@code a12-studio-ui}.
   */
  public static ElementIndex indexFor(Column column, ElementIndex documentModelIndex, ValidationContext context) {
    if (column == null || column.getLinkReferences() == null || column.getLinkReferences().isEmpty()) {
      return documentModelIndex;
    }
    DocumentModel linkedDocumentModel = referencedLinkedDocumentModel(column.getLinkReferences().get(0), context);
    return linkedDocumentModel != null ? new ElementIndex(linkedDocumentModel, context.otherDocumentModels()) : documentModelIndex;
  }

  private static DocumentModel referencedLinkedDocumentModel(ColumnLinkReference linkReference, ValidationContext context) {
    if (linkReference.getRelationship() == null
        || !(context.findOtherModel(linkReference.getRelationship()) instanceof RelationshipModel relationshipModel)
        || relationshipModel.getContent() == null) {
      return null;
    }
    return resolveDocumentModelOrCombination(linkReference.resolveDocumentModelId(relationshipModel.getContent()), context);
  }

  /**
   * Resolves {@code elementRef} against the referenced Document Model, following {@link
   * ElementIndex#resolveElement} through Include groups - a column can just as validly reference a field
   * that lives inside an included model via the compound {@code "<includeGroupId>_<targetId>"} id shape, so
   * {@code index} must have been built with the project's other Document Models (see {@link
   * ElementIndex#ElementIndex(DocumentModel, java.util.List)}) for that to resolve.
   */
  public static Element resolve(ElementIndex index, String elementRef) {
    if (elementRef == null || elementRef.isBlank()) {
      return null;
    }
    return index.resolveElement(elementRef).orElse(null);
  }

  /**
   * The kernel injects the same fixed {@code __meta} group (id {@value #META_GROUP_ID_PREFIX}{@code 8f4b1})
   * into every Document Model at load time - {@code docRef}, {@code modelReference}, {@code modelVersion},
   * {@code creator}, {@code createdAt}, {@code modifier}, {@code modifiedAt} (plus a nested {@code
   * extensions} group) - with the exact same element ids in every model (confirmed identical across multiple
   * unrelated SME fixtures, e.g. {@code Person_DM.json}, {@code PersonWithTeamsAndContracts_COM.json}), so
   * these are legitimately referenceable fields even though a12-studio's own Document Model files never
   * author them explicitly and {@link ElementIndex} has no record of them. Recognized by id prefix rather
   * than a fixed id set so any of the group's known fields (present or future) resolve.
   */
  private static final String META_GROUP_ID_PREFIX = "abc6a6767a60488754aace2accb73824_";

  /**
   * The seven {@code __meta} fields, id to display name - gap 15 of "Overview Model: gap review" (metadata
   * fields cannot be picked for columns/filters/sorting). Four of the ids/names ({@code creator}/{@code
   * createdAt}/{@code modifier}/{@code modifiedAt}) are directly confirmed by a real fixture that uses them as
   * Filter Items with English/German labels ({@code testing/workspaces/advanced_new/models/10_People/Person_Ov.json},
   * under a "Meta Data" section: "Creator"/"Created At"/"Modifier"/"Modified At", in this exact relative order
   * alongside the other three); the remaining three ({@code docRef}/{@code modelReference}/{@code
   * modelVersion}) are inferred from that same order (no fixture uses them, so their display names are a
   * best-effort spacing of the field name itself, not independently confirmed).
   */
  public static final List<MetaField> META_FIELDS = List.of(
      new MetaField(META_GROUP_ID_PREFIX + "field_1169c", "Doc Ref"),
      new MetaField(META_GROUP_ID_PREFIX + "field_cfb40", "Model Reference"),
      new MetaField(META_GROUP_ID_PREFIX + "field_17e3b", "Model Version"),
      new MetaField(META_GROUP_ID_PREFIX + "field_cdaf9", "Creator"),
      new MetaField(META_GROUP_ID_PREFIX + "field_65672", "Created At"),
      new MetaField(META_GROUP_ID_PREFIX + "field_1fa32", "Modifier"),
      new MetaField(META_GROUP_ID_PREFIX + "field_5cd66", "Modified At"));

  /** One {@link #META_FIELDS} entry: a kernel-injected {@code __meta} field's fixed id and display name. */
  public record MetaField(String id, String displayName) {
  }

  /** True when {@code elementRef} (an {@code elementId}/{@code fieldId}) refers to the kernel-injected {@code
   * __meta} group or one of its fields - see {@link #META_GROUP_ID_PREFIX}. Callers should skip existence/
   * indexed/repeatable checks for these instead of resolving them against {@link ElementIndex}, which never
   * contains them. */
  public static boolean isMetaFieldId(String elementRef) {
    return elementRef != null && elementRef.startsWith(META_GROUP_ID_PREFIX);
  }

  /** The display name for {@code elementRef} if it's one of {@link #META_FIELDS}, else {@code null}. */
  public static String metaFieldDisplayName(String elementRef) {
    return META_FIELDS.stream()
        .filter(field -> field.id().equals(elementRef))
        .map(MetaField::displayName)
        .findFirst()
        .orElse(null);
  }

  public static boolean isIndexedFalse(Element element) {
    if (element.getAnnotations() == null) {
      return false;
    }
    for (Annotation annotation : element.getAnnotations()) {
      if ("indexed".equals(annotation.getName()) && "false".equalsIgnoreCase(String.valueOf(annotation.getValue()))) {
        return true;
      }
    }
    return false;
  }

  /**
   * True when {@code elementRef} resolves to an element with a repeatable ancestor - delegates to {@link
   * ElementIndex#isInRepeatableGroup}, which (unlike a plain {@link ElementIndex#parentOf} walk against
   * {@code index}) correctly accounts for an Include's own repeatability when {@code elementRef} resolves
   * into an included model rather than {@code index}'s own tree.
   */
  public static boolean isInRepeatableGroup(ElementIndex index, String elementRef) {
    return index.isInRepeatableGroup(elementRef);
  }

  /**
   * True when {@code element} is a multi-select group itself, or a field living directly inside one -
   * columns may reference either shape depending on how the field was picked. Mirrors SME's
   * {@code DocumentModelApi.isMultiSelect}, used e.g. to disallow {@code sortable} on such columns
   * ({@link OverviewSortableMultiSelectValidator}).
   */
  public static boolean isMultiSelect(ElementIndex index, Element element) {
    if (isMultiSelectGroup(element)) {
      return true;
    }
    GroupElement parent = index.parentOf(element);
    return parent != null && isMultiSelectGroup(parent);
  }

  private static boolean isMultiSelectGroup(Element element) {
    return element instanceof GroupElement groupElement && groupElement.getGroup() != null
        && GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(groupElement.getGroup().getUsageType());
  }

  /**
   * True when {@code element} is a multi-select group (or a field inside one) whose single value field is
   * Enumeration-typed - mirrors SME's {@code DocumentModelApi.isEnumerationMultiSelect}, the extra condition
   * the Column dialog's Element Reference picker and the Custom Selection Of Fields/Section Data pickers apply
   * on top of {@link #isMultiSelect} (gap 16 of "Overview Model: gap review"): a String multi-select is a
   * legal Document Model shape, but neither picker offers one.
   */
  public static boolean isEnumerationMultiSelect(ElementIndex index, Element element) {
    GroupElement group = isMultiSelectGroup(element) ? (GroupElement) element : index.parentOf(element);
    if (group == null || !isMultiSelectGroup(group) || group.getGroup().getElements() == null) {
      return false;
    }
    return group.getGroup().getElements().stream()
        .filter(FieldElement.class::isInstance)
        .map(FieldElement.class::cast)
        .findFirst()
        .map(field -> field.getField() != null
            && index.effectiveFieldType(field.getField().getFieldType()) instanceof EnumerationFieldType)
        .orElse(false);
  }

  /**
   * True when {@code element} is an attachment group itself, or a field living directly inside one - mirrors
   * {@link #isMultiSelect}, used to detect a column's element-specific display options (e.g. {@code
   * attachmentDisplayMode}) in the Column dialog.
   */
  public static boolean isAttachment(ElementIndex index, Element element) {
    if (isAttachmentGroup(element)) {
      return true;
    }
    GroupElement parent = index.parentOf(element);
    return parent != null && isAttachmentGroup(parent);
  }

  private static boolean isAttachmentGroup(Element element) {
    return element instanceof GroupElement groupElement && groupElement.getGroup() != null
        && GroupConfig.USAGE_TYPE_ATTACHMENT.equals(groupElement.getGroup().getUsageType());
  }
}
