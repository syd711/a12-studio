package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * When the Filter Mode is "custom_list", the field selection must be non-empty, its field ids unique,
 * and each one must resolve against the referenced Document Model - not annotated {@code indexed} =
 * false, and not inside a repeatable group. Mirrors SME's {@code custom_list} filter field rules.
 */
public final class OverviewFilterCustomFieldsValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/filterConfiguration/fields";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel) || overviewModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    FilterConfiguration filterConfig = overviewModel.getContent().getConfiguration().getFilterConfiguration();
    if (filterConfig == null || !FilterConfiguration.FILTER_MODE_CUSTOM_LIST.equals(filterConfig.getFilterMode())) {
      return List.of();
    }

    List<FieldRef> fields = filterConfig.getFields();
    if (fields.isEmpty()) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          "At least one field must be selected for a custom field selection.", Severity.ERROR.name()));
    }

    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (FieldRef field : fields) {
      String fieldId = field.getFieldId();
      if (fieldId == null || fieldId.isBlank()) {
        continue;
      }
      if (!seen.add(fieldId)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "The field \"" + fieldId + "\" is selected more than once.", Severity.ERROR.name()));
      }
    }

    DocumentModel documentModel = OverviewElementResolution.referencedDocumentModel(overviewModel, context);
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return errors;
    }
    ElementIndex index = new ElementIndex(documentModel);
    for (FieldRef field : fields) {
      String fieldId = field.getFieldId();
      if (fieldId == null || fieldId.isBlank()) {
        continue;
      }
      Element element = OverviewElementResolution.resolve(index, fieldId);
      if (element == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "The reference is invalid. The referenced field \"" + fieldId + "\" does not exist in the document model.",
            Severity.ERROR.name()));
        continue;
      }
      if (OverviewElementResolution.isIndexedFalse(element)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "The \"indexed\" annotation of field \"" + element.getName()
                + "\" should not be false. Please resolve this problem in the corresponding Document Model.",
            Severity.ERROR.name()));
      }
      if (OverviewElementResolution.isInRepeatableGroup(index, element)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "The reference is invalid. The referenced field \"" + element.getName() + "\" is repeatable.",
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
