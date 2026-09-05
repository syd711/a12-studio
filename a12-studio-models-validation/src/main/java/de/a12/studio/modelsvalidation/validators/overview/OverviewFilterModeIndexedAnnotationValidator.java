package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * When every Document Model field is filterable ({@code filterMode} "all"/"all_with_meta"), none of
 * those fields may be annotated {@code indexed} = false - unlike "custom_list"
 * ({@link OverviewFilterCustomFieldsValidator}), the field set here isn't hand-picked, so the whole
 * model must be checked. Mirrors SME's {@code ValidateIndexedAnnotationForFilterMode} rule; a12-studio
 * doesn't model synthetic "__meta" fields, so "all" and "all_with_meta" are checked identically here.
 */
public final class OverviewFilterModeIndexedAnnotationValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/filterConfiguration/filterMode";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel) || overviewModel.getContent().getConfiguration() == null
        || !Boolean.TRUE.equals(overviewModel.getContent().getConfiguration().getEnableFilter())) {
      return List.of();
    }
    FilterConfiguration filterConfig = overviewModel.getContent().getConfiguration().getFilterConfiguration();
    if (filterConfig == null || !(FilterConfiguration.FILTER_MODE_ALL.equals(filterConfig.getFilterMode())
        || FilterConfiguration.FILTER_MODE_ALL_WITH_META.equals(filterConfig.getFilterMode()))) {
      return List.of();
    }

    DocumentModel documentModel = OverviewElementResolution.referencedDocumentModel(overviewModel, context);
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel, context.otherDocumentModels());
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (ElementIndex.isField(element) && OverviewElementResolution.isIndexedFalse(element)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.common.indexedAnnotationFalse", element.getName()), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
