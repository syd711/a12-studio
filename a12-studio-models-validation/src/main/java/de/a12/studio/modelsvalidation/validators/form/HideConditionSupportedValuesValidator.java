package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.HideCondition;
import de.a12.studio.models.formmodel.HideConditionCase;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Every hide condition's trigger values must actually be possible for its master field's type (SME: "There
 * are hide condition values selected which are not supported by the hide condition field."). Only Boolean/
 * Confirm ({@code "true"}/no value) and Enumeration (its declared values/no value) master fields have a
 * known, checkable set of supported values; an unresolved master field or an unsupported field type is
 * skipped here (nothing to check against), matching SME's {@code resolveHideConditionSupportedMasterValues}
 * returning undefined in those cases.
 */
public final class HideConditionSupportedValuesValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (HideConditionElements.Entry entry : HideConditionElements.collect(formModel.getContent())) {
      HideCondition condition = entry.hideCondition();
      if (condition.getMasterField() == null || condition.getMasterField().isBlank() || condition.getCases().isEmpty()) {
        continue;
      }
      Set<String> supported = resolveSupportedValues(indexes, condition.getMasterField());
      if (supported == null) {
        continue;
      }
      List<String> unsupported = condition.getCases().stream()
          .map(HideConditionCase::getMasterValue)
          .filter(value -> !supported.contains(value))
          .toList();
      if (!unsupported.isEmpty()) {
        errors.add(new ModelValidationError(model, entry.nodeId(),
            ValidationMessages.get("validation.hideCondition.unsupportedValues", describe(unsupported)),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static String describe(List<String> values) {
    return values.stream().map(v -> v == null ? "(no value)" : v).collect(Collectors.joining(", "));
  }

  /** Returns {@code null} when the master field can't be resolved or its type has no fixed value set. */
  private static Set<String> resolveSupportedValues(List<ElementIndex> indexes, String masterFieldId) {
    for (ElementIndex index : indexes) {
      var resolved = index.resolveElement(masterFieldId);
      if (resolved.isEmpty() || !(resolved.get() instanceof FieldElement field) || field.getField() == null) {
        continue;
      }
      FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
      Set<String> values = new HashSet<>();
      values.add(null);
      if (effectiveType instanceof BooleanFieldType || effectiveType instanceof ConfirmFieldType) {
        values.add("true");
        return values;
      }
      if (effectiveType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
        for (EnumerationValue value : enumType.getEnumerationType().getValues()) {
          values.add(value.getValue());
        }
        return values;
      }
      return null;
    }
    return null;
  }
}
