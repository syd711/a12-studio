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
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks for errors the ported kernel rules ({@link BasicConsistencyValidator} and siblings) don't cover:
 * missing/unresolved references between elements. We use simple generic error messages instead of the
 * kernel's own wording so that changes to that wording don't need to be integrated into this code — except
 * {@code validation.missingReference.indexFieldInvalid}, which is shown directly in the Group properties
 * panel and is kept verbatim (matching SME's DomainGroup meta-model rule "A12_INDEX_FIELD_INVALID_REFERENCE",
 * see client/resources/models/documentModel/DomainGroup.json in the SME repo) on purpose.
 */
public final class MissingReferenceValidator implements ModelValidator {

  /**
   * Not a real element id (see {@link MissingLocaleValidator#ELEMENT_ID} for why a stable placeholder is
   * needed): backs the "Import target itself has a broken import chain" check below, a header-level problem
   * with no single element to blame.
   */
  private static final String TRANSITIVE_IMPORT_ELEMENT_ID = "header/modelReferences/typeDefinitions";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    ElementIndex index = context.elementIndex();
    List<ModelValidationError> result = new ArrayList<>();

    for (TypeDefinition duplicate : getDuplicateNamedTypeDefinitions(documentModel)) {
      result.add(error(model, duplicate.getId(), ElementProperty.GENERAL,
          ValidationMessages.get("validation.missingReference.duplicateTypeDefinitionName", duplicate.getId(), duplicate.getName())));
    }
    for (ModelReference reference : importReferences(documentModel)) {
      DocumentModel imported = resolveOtherModel(reference.getReference(), context.otherDocumentModels());
      if (imported != null && TransitiveTypeDefinitions.hasUnresolvedImportChain(imported, context.otherDocumentModels())) {
        result.add(new ModelValidationError(model, TRANSITIVE_IMPORT_ELEMENT_ID,
            ValidationMessages.get("validation.missingReference.transitiveImportBroken", imported.getId()), Severity.ERROR.name()));
      }
    }

    for (Element element : index.allElements()) {
      if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
        String groupPath = index.getPath(groupElement);
        if (hasMissingIncludeReference(groupElement, context.otherDocumentModels())) {
          result.add(error(model, groupElement.getId(), ElementProperty.INCLUDE_REFERENCE,
              ValidationMessages.get("validation.missingReference.missingIncludeReference", groupPath)));
        }
        if (hasMissingIndexField(groupElement, index)) {
          result.add(error(model, groupElement.getId(), ElementProperty.GROUP_PROPERTIES,
              ValidationMessages.get("validation.missingReference.indexFieldInvalid")));
        }
        for (Element duplicate : getElementsWithDuplicatedNames(groupElement)) {
          result.add(error(model, duplicate.getId(), ElementProperty.GENERAL,
              ValidationMessages.get("validation.missingReference.duplicatePath", groupPath)));
        }
      } else if (element instanceof ComputationElement computation) {
        if (hasMissingComputedField(computation, index)) {
          String path = index.getPath(computation);
          result.add(error(model, computation.getId(), ElementProperty.COMPUTATION_PROPERTIES,
              ValidationMessages.get("validation.missingReference.missingComputedField", path)));
        }
      } else if (element instanceof FieldElement field) {
        String path = index.getPath(field);
        if (hasTooFewEnumValues(field, index)) {
          result.add(error(model, field.getId(), ElementProperty.DATA_TYPE,
              ValidationMessages.get("validation.missingReference.tooFewEnumValues", path)));
        }
        TypeDefStatus typeDefStatus = typeDefStatus(field, index);
        if (typeDefStatus == TypeDefStatus.NOT_SPECIFIED) {
          result.add(error(model, field.getId(), ElementProperty.TYPE,
              ValidationMessages.get("validation.missingReference.missingTypeDefinition", path)));
        } else if (typeDefStatus == TypeDefStatus.DOES_NOT_EXIST) {
          result.add(error(model, field.getId(), ElementProperty.TYPE,
              ValidationMessages.get("validation.missingReference.invalidTypeDefinition", path)));
        }
      }
    }
    return result;
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String property, String message) {
    return new ModelValidationError(model, elementId, property, message, Severity.ERROR.name());
  }

  /**
   * A group is an Include if its {@link de.a12.studio.models.documentmodel.GroupConfig} carries an {@code
   * includeConfig}. A blank {@code reference} counts as missing: the UI leaves it unset when an Include is
   * first created, until the user assigns one, and this should keep surfacing the error until then.
   */
  private static boolean hasMissingIncludeReference(GroupElement groupElement, List<DocumentModel> otherModels) {
    IncludeConfig includeConfig = groupElement.getGroup().getIncludeConfig();
    if (includeConfig == null) {
      return false;
    }
    String reference = includeConfig.getReference();
    return reference == null || reference.isBlank() || resolveOtherModel(reference, otherModels) == null;
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

  /**
   * Every one of {@code documentModel}'s own type definitions whose name collides with another of its own type
   * definitions - mirrors SME's {@code TYPE_DEF_NAME_DUPLICATED} rule (see {@code DomainTypedef.json}'s
   * {@code DefaultDuplicateTypeDefName} custom condition), which only compares a model's own, non-included,
   * non-imported type definitions against each other. {@code getTypeDefinitions()} already only ever holds
   * this model's own list - anything inherited via Include/Import lives in the owning model's own list instead
   * (see {@link TransitiveTypeDefinitions}) - so no extra filtering is needed here.
   */
  private static Set<TypeDefinition> getDuplicateNamedTypeDefinitions(DocumentModel documentModel) {
    List<TypeDefinition> typeDefinitions = documentModel.getContent().getTypeDefinitions();
    if (typeDefinitions == null) {
      return Set.of();
    }
    Set<TypeDefinition> result = new LinkedHashSet<>();
    typeDefinitions.stream().collect(Collectors.groupingBy(TypeDefinition::getName)).values().stream()
        .filter(group -> group.size() > 1)
        .forEach(result::addAll);
    return result;
  }

  /** The model's header references of purpose {@link ModelReference#PURPOSE_TYPE_DEFINITIONS} ("Import"). */
  private static List<ModelReference> importReferences(DocumentModel model) {
    List<ModelReference> references = model.getModelReferences();
    if (references == null) {
      return List.of();
    }
    return references.stream().filter(ref -> ModelReference.PURPOSE_TYPE_DEFINITIONS.equals(ref.getPurpose())).toList();
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

  /** Mirrors SME's two distinct rules: {@code A12_TYPE_DEFINITION_MISSING} ("must be specified", no id set at
   * all) vs. {@code A12_TYPE_DEFINITION_INVALID} ("does not exist", an id set but unresolvable). */
  private enum TypeDefStatus { OK, NOT_SPECIFIED, DOES_NOT_EXIST }

  private static TypeDefStatus typeDefStatus(FieldElement field, ElementIndex index) {
    if (field.getField() == null || !(field.getField().getFieldType() instanceof TypeDefFieldType typeDefFieldType)) {
      return TypeDefStatus.OK;
    }
    String typeDefId = typeDefFieldType.getTypeDefType() == null ? null : typeDefFieldType.getTypeDefType().getTypeDefinitionId();
    if (typeDefId == null || typeDefId.isBlank()) {
      return TypeDefStatus.NOT_SPECIFIED;
    }
    return index.effectiveFieldType(typeDefFieldType) == null ? TypeDefStatus.DOES_NOT_EXIST : TypeDefStatus.OK;
  }
}
