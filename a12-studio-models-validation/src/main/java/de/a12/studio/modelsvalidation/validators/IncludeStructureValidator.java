package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Locale;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Two of SME's Include checks that a12-studio didn't yet port (the third, "creates an include loop", is a
 * pre-existing open TODO item tracked separately): the included model must declare every locale the
 * including model has, and the included model must have exactly one, non-repeatable root group. Mirrors
 * SME/kernel's {@code DomainInclude.json} rules. Like {@link IncludeTypeDefinitionModeValidator}, this only
 * ever compares the two models directly involved in one Include edge.
 */
public final class IncludeStructureValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    Set<String> ownLocales = localeCodesOf(model);
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
      checkLocales(model, groupElement.getId(), included, ownLocales, errors);
      checkSingleNonRepeatableRootGroup(model, groupElement.getId(), included, errors);
    }
    return errors;
  }

  private static void checkLocales(A12Model<?> model, String elementId, DocumentModel included, Set<String> ownLocales,
      List<ModelValidationError> errors) {
    Set<String> includedLocales = localeCodesOf(included);
    for (String locale : ownLocales) {
      if (!includedLocales.contains(locale)) {
        errors.add(new ModelValidationError(model, elementId, ElementProperty.INCLUDE_REFERENCE,
            ValidationMessages.get("validation.includeStructure.missingLocale", included.getId(), locale), Severity.ERROR.name()));
      }
    }
  }

  private static void checkSingleNonRepeatableRootGroup(A12Model<?> model, String elementId, DocumentModel included,
      List<ModelValidationError> errors) {
    List<GroupElement> rootGroups = included.getContent() == null || included.getContent().getModelRoot() == null
        ? List.of()
        : included.getContent().getModelRoot().getRootGroups();
    if (rootGroups.size() != 1) {
      errors.add(new ModelValidationError(model, elementId, ElementProperty.INCLUDE_REFERENCE,
          ValidationMessages.get("validation.includeStructure.notSingleRootGroup", included.getId()), Severity.ERROR.name()));
      return;
    }
    GroupElement rootGroup = rootGroups.get(0);
    Integer repeatability = rootGroup.getGroup() == null ? null : rootGroup.getGroup().getRepeatability();
    if (repeatability != null && repeatability > 1) {
      errors.add(new ModelValidationError(model, elementId, ElementProperty.INCLUDE_REFERENCE,
          ValidationMessages.get("validation.includeStructure.repeatableRootGroup", included.getId()), Severity.ERROR.name()));
    }
  }

  private static Set<String> localeCodesOf(A12Model<?> model) {
    return model.getLocales() == null ? Set.of()
        : model.getLocales().stream()
            .map(Locale::getCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
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
