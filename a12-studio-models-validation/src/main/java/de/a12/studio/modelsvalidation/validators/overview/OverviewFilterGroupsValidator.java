package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.overviewmodel.FilterItemOptions;
import de.a12.studio.models.overviewmodel.NewFilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Each Custom Filter {@link FilterGroup} needs an id and at least one {@link FilterItem}; every field-based
 * filter item must reference a field ({@code options.fieldId}), and that field must resolve against the
 * referenced Document Model like a column's element reference. Mirrors {@link OverviewFilterSectionsValidator}'s
 * rules for the older {@code filterConfiguration.sectionData} structure.
 * <p>
 * Without this validator a filter item can be saved with no field reference at all - see {@link FilterItem}'s
 * class doc: {@code testing/workspaces/basic/models/Company_OM.json} shipped in exactly that state because
 * nothing flagged it, and SME fails to load the resulting Overview Model.
 */
public final class OverviewFilterGroupsValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/newFilterConfiguration/filterGroups";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel) || overviewModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    NewFilterConfiguration filterConfiguration = overviewModel.getContent().getConfiguration().getNewFilterConfiguration();
    if (filterConfiguration == null || filterConfiguration.getFilterGroups().isEmpty()) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    ElementIndex index = elementIndex(overviewModel, context);

    for (FilterGroup group : filterConfiguration.getFilterGroups()) {
      if (group.getId() == null || group.getId().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID, "A filter group id is required.", Severity.ERROR.name()));
      }
      if (group.getFilterItems().isEmpty()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "Filter group \"" + describeGroup(group) + "\" must have at least one filter item.", Severity.ERROR.name()));
        continue;
      }

      for (FilterItem item : group.getFilterItems()) {
        FilterItemOptions options = item.getOptions();
        String fieldId = options != null ? options.getFieldId() : null;
        if (fieldId == null || fieldId.isBlank()) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "Filter item \"" + describeItem(item) + "\" in filter group \"" + describeGroup(group)
                  + "\" must reference a field.", Severity.ERROR.name()));
          continue;
        }

        if (index == null) {
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
    }
    return errors;
  }

  private static ElementIndex elementIndex(OverviewModel model, ValidationContext context) {
    DocumentModel documentModel = OverviewElementResolution.referencedDocumentModel(model, context);
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return null;
    }
    return new ElementIndex(documentModel);
  }

  private static String describeGroup(FilterGroup group) {
    return group.getId() != null ? group.getId() : "(unnamed)";
  }

  private static String describeItem(FilterItem item) {
    return item.getId() != null ? item.getId() : "(unnamed)";
  }
}
