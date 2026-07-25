package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.modelsvalidation.ElementIndex;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks for errors the ported kernel rules ({@link DocumentModelConsistencyValidator}) don't cover:
 * missing/unresolved references between elements. We use simple generic error messages instead of the
 * kernel's own wording so that changes to that wording don't need to be integrated into this code — except
 * {@link #INDEX_FIELD_INVALID_MESSAGE}, which is shown directly in the Group properties panel and is kept
 * verbatim on purpose.
 */
public final class MissingReferenceValidator implements ModelValidator {

  // Verbatim text of SME's DomainGroup meta-model rule "A12_INDEX_FIELD_INVALID_REFERENCE" (see
  // client/resources/models/documentModel/DomainGroup.json in the SME repo), which fires the moment a group's
  // configured index field no longer resolves to a field in the group (e.g. that field was deleted). Kept
  // verbatim, unlike the other generic messages below, so the Group properties panel can show users the exact
  // wording they already know from SME.
  public static final String INDEX_FIELD_INVALID_MESSAGE = "The index field is not a valid field.";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel);
    List<ModelValidationError> result = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
        String groupPath = index.getPath(groupElement);
        if (hasMissingIncludeReference(groupElement, documentModel, context.otherDocumentModels())) {
          result.add(error(model, groupElement.getId(), "Include with path '" + groupPath + "': Missing Include Reference"));
        }
        if (hasMissingIndexField(groupElement, index)) {
          result.add(error(model, groupElement.getId(), INDEX_FIELD_INVALID_MESSAGE));
        }
        for (Element duplicate : getElementsWithDuplicatedNames(groupElement)) {
          result.add(error(model, duplicate.getId(), "Element with path '" + groupPath + "': Multiple Elements with same path"));
        }
      } else if (element instanceof ComputationElement computation) {
        if (hasMissingComputedField(computation, index)) {
          String path = index.getPath(computation);
          result.add(error(model, computation.getId(), "Computation with path '" + path + "': Missing Computed Field"));
        }
      } else if (element instanceof FieldElement field) {
        String path = index.getPath(field);
        if (hasTooFewEnumValues(field, index)) {
          result.add(error(model, field.getId(), "Field with path '" + path + "': Enumeration must have at least two values"));
        }
        if (hasMissingTypeDef(field, index)) {
          result.add(error(model, field.getId(), "Field with path '" + path + "': Missing Type Definition"));
        }
      }
    }
    return result;
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }

  private static Optional<ModelReference> findIncludeReference(DocumentModel model, String alias) {
    if (model.getModelReferences() == null) {
      return Optional.empty();
    }
    return model.getModelReferences().stream()
        .filter(r -> ModelReference.PURPOSE_INCLUDE.equals(r.getPurpose()) && alias.equals(r.getAlias()))
        .findFirst();
  }

  private static boolean hasMissingIncludeReference(GroupElement groupElement, DocumentModel model, List<DocumentModel> otherModels) {
    String alias = groupElement.getGroup().getModelAlias();
    if (alias == null || alias.isBlank()) {
      return false;
    }
    Optional<ModelReference> reference = findIncludeReference(model, alias);
    return reference.isEmpty() || resolveOtherModel(reference.get().getReference(), otherModels) == null;
  }

  /** Mirrors the strip-path-and-.json-suffix resolution the a12 kernel's reference resolver used. */
  private static DocumentModel resolveOtherModel(String reference, List<DocumentModel> otherModels) {
    if (reference == null) {
      return null;
    }
    String id = reference;
    int lastSlash = id.lastIndexOf('/');
    if (lastSlash >= 0) {
      id = id.substring(lastSlash + 1);
    }
    int jsonSuffix = id.lastIndexOf(".json");
    if (jsonSuffix >= 0) {
      id = id.substring(0, jsonSuffix);
    }
    String finalId = id;
    return otherModels.stream().filter(dm -> finalId.equals(dm.getId())).findFirst().orElse(null);
  }

  private static boolean hasMissingIndexField(GroupElement groupElement, ElementIndex index) {
    String indexFieldName = groupElement.getGroup().getIndexFieldName();
    if (indexFieldName == null || indexFieldName.isBlank()) {
      return false;
    }
    return index.resolveRelativePath(groupElement, indexFieldName).filter(ElementIndex::isField).isEmpty();
  }

  private static Set<Element> getElementsWithDuplicatedNames(GroupElement groupElement) {
    List<Element> elements = groupElement.getGroup().getElements();
    if (elements == null) {
      return Set.of();
    }
    Set<Element> result = new LinkedHashSet<>();
    elements.stream().collect(Collectors.groupingBy(Element::getName)).values().stream()
        .filter(group -> group.size() > 1)
        .forEach(result::addAll);
    return result;
  }

  private static boolean hasMissingComputedField(ComputationElement computation, ElementIndex index) {
    String relPath = computation.getComputation() == null ? null : computation.getComputation().getComputedFieldRelPath();
    if (relPath == null || relPath.isBlank()) {
      return true;
    }
    return index.resolveRelativePath(computation, relPath).filter(ElementIndex::isField).isEmpty();
  }

  private static boolean hasTooFewEnumValues(FieldElement field, ElementIndex index) {
    GroupElement parent = index.parentOf(field);
    if (parent == null || parent.getGroup() == null || !"multi-select".equals(parent.getGroup().getUsageType())) {
      return false;
    }
    List<EnumerationValue> enumValues = getEnumValues(field, index);
    return enumValues != null && enumValues.size() < 2;
  }

  private static List<EnumerationValue> getEnumValues(FieldElement field, ElementIndex index) {
    if (field.getField() == null) {
      return null;
    }
    FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
    if (effectiveType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
      return enumType.getEnumerationType().getValues();
    }
    return null;
  }

  private static boolean hasMissingTypeDef(FieldElement field, ElementIndex index) {
    if (field.getField() == null || !(field.getField().getFieldType() instanceof TypeDefFieldType typeDefFieldType)) {
      return false;
    }
    String typeDefId = typeDefFieldType.getTypeDefType() == null ? null : typeDefFieldType.getTypeDefType().getTypeDefinitionId();
    if (typeDefId == null || typeDefId.isBlank()) {
      return true;
    }
    return index.effectiveFieldType(typeDefFieldType) == null;
  }
}
