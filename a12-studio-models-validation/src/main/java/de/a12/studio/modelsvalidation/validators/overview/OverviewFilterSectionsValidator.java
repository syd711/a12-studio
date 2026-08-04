package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.FilterSection;
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
 * Each Filter "Section Data" entry needs an id and at least one field; a field id must be unique within
 * its own section and must not be reused by another section; every field id must resolve against the
 * referenced Document Model like a column's element reference. Mirrors SME's section-data rules.
 */
public final class OverviewFilterSectionsValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/filterConfiguration/sectionData";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel) || overviewModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    FilterConfiguration filterConfig = overviewModel.getContent().getConfiguration().getFilterConfiguration();
    if (filterConfig == null || filterConfig.getSectionData().isEmpty()) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    ElementIndex index = elementIndex(overviewModel, context);
    Set<String> fieldIdsSeenAcrossSections = new HashSet<>();

    for (FilterSection section : filterConfig.getSectionData()) {
      if (section.getId() == null || section.getId().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID, "A section id is required.", Severity.ERROR.name()));
      }
      if (section.getFields().isEmpty()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "Section \"" + describeSection(section) + "\" must have at least one field.", Severity.ERROR.name()));
      }

      // Deduplicated first, so a field repeated within one section is only reported once (as an
      // in-section duplicate) rather than also tripping the across-sections check on its second occurrence.
      Set<String> fieldIdsInSection = new HashSet<>();
      for (FieldRef field : section.getFields()) {
        String fieldId = field.getFieldId();
        if (fieldId == null || fieldId.isBlank()) {
          continue;
        }
        if (!fieldIdsInSection.add(fieldId)) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "The field \"" + fieldId + "\" is selected more than once in section \"" + describeSection(section) + "\".",
              Severity.ERROR.name()));
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
      }
      for (String fieldId : fieldIdsInSection) {
        if (!fieldIdsSeenAcrossSections.add(fieldId)) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "The field \"" + fieldId + "\" is used in more than one section.", Severity.ERROR.name()));
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

  private static String describeSection(FilterSection section) {
    return section.getId() != null ? section.getId() : "(unnamed)";
  }
}
