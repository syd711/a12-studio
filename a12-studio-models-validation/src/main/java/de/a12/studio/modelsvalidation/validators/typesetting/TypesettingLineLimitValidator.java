package de.a12.studio.modelsvalidation.validators.typesetting;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.models.typesettingmodel.TypesettingModelDefaults;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The kernel meta-model's number limits on {@code content.orphan} and {@code content.widow}
 * ({@code minValue 0}, {@code maxValue 10}, see the {@code print-typesetting} library's
 * {@code DomainTypesettingMetaModel}). Both are optional, so a missing value is fine.
 */
public final class TypesettingLineLimitValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TypesettingModel typesettingModel) || typesettingModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    check(model, typesettingModel.getContent().getOrphan(), TypesettingElementIds.ORPHAN,
        "validation.typesetting.orphanOutOfRange", errors);
    check(model, typesettingModel.getContent().getWidow(), TypesettingElementIds.WIDOW,
        "validation.typesetting.widowOutOfRange", errors);
    return errors;
  }

  private static void check(A12Model<?> model, Integer value, String elementId, String messageKey,
      List<ModelValidationError> errors) {
    if (value != null && (value < TypesettingModelDefaults.MIN_LINE_LIMIT || value > TypesettingModelDefaults.MAX_LINE_LIMIT)) {
      errors.add(new ModelValidationError(model, elementId,
          ValidationMessages.get(messageKey, TypesettingModelDefaults.MIN_LINE_LIMIT, TypesettingModelDefaults.MAX_LINE_LIMIT),
          Severity.ERROR.name()));
    }
  }
}
