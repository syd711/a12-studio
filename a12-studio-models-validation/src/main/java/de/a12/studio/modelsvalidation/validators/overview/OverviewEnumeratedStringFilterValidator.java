package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.EnumeratedStringFilter;
import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
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

/**
 * When {@code content.configuration.filterConfiguration.enumeratedStringFilter} is present (i.e. "Filter
 * String Fields with Multi-Select" is enabled), its field list must be non-empty, its field ids unique, each
 * one must resolve against the referenced Document Model, and its Paging Size must be set - mirrors SME's
 * {@code enumeratedStringFilter} rules ({@code fieldIdsMustBeFilled}, {@code fieldIdsNotUnique}, field {@code
 * mustHaveValidReference}, {@code pagingSizeIsRequired}).
 */
public final class OverviewEnumeratedStringFilterValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/filterConfiguration/enumeratedStringFilter";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel) || overviewModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    FilterConfiguration filterConfig = overviewModel.getContent().getConfiguration().getFilterConfiguration();
    EnumeratedStringFilter filter = filterConfig == null ? null : filterConfig.getEnumeratedStringFilter();
    if (filter == null) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    List<FieldRef> fields = filter.getFields();
    if (fields.isEmpty()) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.overviewEnumeratedStringFilter.empty"), Severity.ERROR.name()));
    }
    if (filter.getPagingSize() == null) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.overviewEnumeratedStringFilter.pagingSizeRequired"), Severity.ERROR.name()));
    }

    Set<String> seen = new HashSet<>();
    for (FieldRef field : fields) {
      String fieldId = field.getFieldId();
      if (fieldId == null || fieldId.isBlank()) {
        continue;
      }
      if (!seen.add(fieldId)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewEnumeratedStringFilter.duplicate", fieldId), Severity.ERROR.name()));
      }
    }

    DocumentModel documentModel = OverviewElementResolution.referencedDocumentModel(overviewModel, context);
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return errors;
    }
    ElementIndex index = new ElementIndex(documentModel, context.otherDocumentModels());
    for (FieldRef field : fields) {
      String fieldId = field.getFieldId();
      if (fieldId == null || fieldId.isBlank()) {
        continue;
      }
      Element element = OverviewElementResolution.resolve(index, fieldId);
      if (element == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.common.fieldReferenceMissing", fieldId), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
