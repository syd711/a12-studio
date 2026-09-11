package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * A model cannot Include another model whose own Type Definition "mode" - local/included type definitions
 * vs. an imported Type Definition Model, the two mutually exclusive ways of introducing type definitions
 * (see {@code TypeDefinitionTableController.updateAddImportAvailability()}) - differs from this model's own
 * mode, mirroring SME's {@code IncludeDifferentTypeDefinitionMode} custom condition. Unlike {@link
 * MissingReferenceValidator}'s broken-import checks, this only ever compares the two models directly
 * involved in one Include edge, not the whole transitive graph - the same shape as SME's own check.
 */
public final class IncludeTypeDefinitionModeValidator implements ModelValidator {

  private enum TypeDefMode { NONE, LOCAL, IMPORT }

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    TypeDefMode ownMode = modeOf(documentModel);
    if (ownMode == TypeDefMode.NONE) {
      return List.of();
    }

    ElementIndex index = context.elementIndex();
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (!(element instanceof GroupElement groupElement) || groupElement.getGroup() == null) {
        continue;
      }
      IncludeConfig includeConfig = groupElement.getGroup().getIncludeConfig();
      if (includeConfig == null || includeConfig.getReference() == null) {
        continue;
      }
      DocumentModel included = resolveOtherModel(includeConfig.getReference(), context.otherDocumentModels());
      if (included == null) {
        continue;
      }
      TypeDefMode includedMode = modeOf(included);
      if (includedMode != TypeDefMode.NONE && includedMode != ownMode) {
        errors.add(new ModelValidationError(model, groupElement.getId(), ElementProperty.INCLUDE_REFERENCE,
            ValidationMessages.get("validation.includeTypeDefinitionMode.mismatch", included.getId()), Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static TypeDefMode modeOf(DocumentModel documentModel) {
    List<ModelReference> references = documentModel.getModelReferences();
    boolean hasImport = references != null
        && references.stream().anyMatch(reference -> ModelReference.PURPOSE_TYPE_DEFINITIONS.equals(reference.getPurpose()));
    if (hasImport) {
      return TypeDefMode.IMPORT;
    }
    List<?> typeDefinitions = documentModel.getContent().getTypeDefinitions();
    return typeDefinitions != null && !typeDefinitions.isEmpty() ? TypeDefMode.LOCAL : TypeDefMode.NONE;
  }

  /** Mirrors the strip-path-and-.json-suffix resolution the a12 kernel's reference resolver used (see
   * MissingReferenceValidator's private twin of this method). */
  private static DocumentModel resolveOtherModel(String reference, List<DocumentModel> otherModels) {
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
    return otherModels.stream().filter(candidate -> finalId.equals(candidate.getId())).findFirst().orElse(null);
  }
}
