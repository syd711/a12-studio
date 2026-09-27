package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.formmodel.AmountSuffix;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;
import java.util.Optional;

/**
 * A dynamic {@link AmountSuffix}'s {@code fieldRef} must resolve to a real, non-repeatable Enumeration field
 * of the referenced Document Model (SME's {@code amountSuffixFieldRefMustBeValidReference}).
 */
public final class FormAmountSuffixFieldRefValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/amountSuffix";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    AmountSuffix amountSuffix = formModel.getContent().getAmountSuffix();
    String fieldRef = amountSuffix == null ? null : amountSuffix.getFieldRef();
    if (fieldRef == null || fieldRef.isBlank()) {
      return List.of();
    }

    for (ElementIndex index : FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context)) {
      Optional<Element> element = index.resolveElement(fieldRef);
      if (element.isEmpty()) {
        continue;
      }
      if (isNonRepeatableEnumerationField(index, element.get(), fieldRef)) {
        return List.of();
      }
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.formAmountSuffix.fieldRefNotEnumeration", fieldRef), Severity.ERROR.name()));
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID,
        ValidationMessages.get("validation.formAmountSuffix.fieldRefMissing", fieldRef), Severity.ERROR.name()));
  }

  private static boolean isNonRepeatableEnumerationField(ElementIndex index, Element element, String fieldRef) {
    if (!(element instanceof FieldElement fieldElement) || fieldElement.getField() == null) {
      return false;
    }
    FieldType effectiveType = index.effectiveFieldType(fieldElement.getField().getFieldType());
    return effectiveType instanceof EnumerationFieldType && !index.isInRepeatableGroup(fieldRef);
  }
}
