package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of the kernel's MultiSelectGroupRule (decompiled from kernel-md-model, EUPL-1.2 dual-licensed): a
 * multi-select group must be repeatable and contain exactly one enumeration/string field, plus rules.
 */
public final class MultiSelectGroupValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel);
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (!(element instanceof GroupElement groupElement) || groupElement.getGroup() == null) {
        continue;
      }
      GroupConfig group = groupElement.getGroup();
      if (!GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(group.getUsageType())) {
        continue;
      }
      Integer repeatability = group.getRepeatability();
      if (repeatability == null || repeatability <= 1) {
        errors.add(error(model, groupElement.getId(), "The multi-select group [" + groupElement.getName() + "] must be repeatable."));
      }
      List<FieldElement> fieldsInGroup = group.getElements() == null ? List.of()
          : group.getElements().stream().filter(FieldElement.class::isInstance).map(FieldElement.class::cast).toList();
      if (fieldsInGroup.size() != 1) {
        errors.add(error(model, groupElement.getId(),
            "In the multi-select group [" + groupElement.getName() + "], there must be only one field."));
      } else {
        checkMultiSelectField(model, groupElement, group, fieldsInGroup.get(0), index, errors);
      }
      boolean otherElementsPresent = group.getElements() != null
          && group.getElements().stream().anyMatch(e -> !(e instanceof FieldElement) && !(e instanceof RuleElement));
      if (otherElementsPresent) {
        errors.add(error(model, groupElement.getId(),
            "Besides rules and one field, other elements are not allowed in the multi-select group [" + groupElement.getName() + "]."));
      }
    }
    return errors;
  }

  private static void checkMultiSelectField(A12Model<?> model, GroupElement groupElement, GroupConfig group, FieldElement field,
      ElementIndex index, List<ModelValidationError> errors) {
    if (field.getField() == null || field.getField().getRequirednessConfig() == null) {
      errors.add(error(model, field.getId(),
          "The field [" + field.getName() + "] in the multi-select group [" + groupElement.getName() + "] must be marked as required."));
    }
    if (field.getName() != null && field.getName().equals(group.getIndexFieldName())) {
      errors.add(error(model, field.getId(), "The field [" + field.getName() + "] in the multi-select group [" + groupElement.getName()
          + "] may not be defined as index field."));
    }
    FieldType effectiveType = field.getField() == null ? null : index.effectiveFieldType(field.getField().getFieldType());
    if (!(effectiveType instanceof EnumerationFieldType) && !(effectiveType instanceof StringFieldType)) {
      errors.add(error(model, field.getId(), "The field [" + field.getName() + "] in the multi-select group [" + groupElement.getName()
          + "] must be an enumeration or a string."));
    }
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }
}
