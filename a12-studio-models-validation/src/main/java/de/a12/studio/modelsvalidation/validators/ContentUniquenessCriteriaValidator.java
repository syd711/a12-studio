package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.ContentUniquenessCriterion;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Structural checks for {@link de.a12.studio.models.documentmodel.DocumentModelContent#getDocumentUniquenessCriteria()}
 * ({@link ContentUniquenessCriterion}) - a name is required and unique among the model's own criteria, at
 * least one Field is required, and every Field's {@link ContentUniquenessCriterion.Field#getFullName() full
 * path} must resolve against the model (via {@link ElementIndex#resolveAbsolutePath}) - mirroring the
 * structural shape of every other reference/basic-consistency validator in this package. No fixture on disk
 * exercises this content today; not independently re-verified against SME's own form for it (see the "Document
 * Model: gap review" note in {@code docs/sme-reference-comparison.md}, gap 9).
 */
public final class ContentUniquenessCriteriaValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/documentUniquenessCriteria";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel) || documentModel.getContent() == null
        || documentModel.getContent().getDocumentUniquenessCriteria() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> seenNames = new HashSet<>();
    Set<String> reportedDuplicates = new HashSet<>();
    for (ContentUniquenessCriterion criterion : documentModel.getContent().getDocumentUniquenessCriteria()) {
      if (criterion.getName() == null || criterion.getName().isBlank()) {
        errors.add(error(model, Severity.ERROR, "validation.contentUniquenessCriteria.nameRequired"));
      }
      else if (!seenNames.add(criterion.getName()) && reportedDuplicates.add(criterion.getName())) {
        errors.add(error(model, Severity.ERROR, "validation.contentUniquenessCriteria.duplicateName", criterion.getName()));
      }
      if (criterion.getFields() == null || criterion.getFields().isEmpty()) {
        errors.add(error(model, Severity.ERROR, "validation.contentUniquenessCriteria.fieldsRequired", criterionLabel(criterion)));
        continue;
      }
      for (ContentUniquenessCriterion.Field field : criterion.getFields()) {
        String fullName = field.getFullName();
        if (fullName == null || fullName.isBlank()
            || context.elementIndex().resolveAbsolutePath(fullName).isEmpty()) {
          errors.add(error(model, Severity.ERROR, "validation.contentUniquenessCriteria.fieldNotFound",
              criterionLabel(criterion), fullName));
        }
      }
    }
    return errors;
  }

  private static String criterionLabel(ContentUniquenessCriterion criterion) {
    return criterion.getName() == null || criterion.getName().isBlank() ? "?" : criterion.getName();
  }

  private static ModelValidationError error(A12Model<?> model, Severity severity, String messageKey, Object... args) {
    return new ModelValidationError(model, ELEMENT_ID, ValidationMessages.get(messageKey, args), severity.name());
  }
}
