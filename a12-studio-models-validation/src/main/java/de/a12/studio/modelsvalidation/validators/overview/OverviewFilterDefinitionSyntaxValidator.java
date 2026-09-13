package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.overviewmodel.NewFilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.querymodel.ql.QueryLanguageEmitter;
import de.a12.studio.models.querymodel.ql.QueryLanguageException;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * If set, a Filter-Definition-based {@link FilterItem}'s {@link FilterItem#getFilterDefinition()} must be
 * syntactically valid Query Language - same grammar as {@link
 * de.a12.studio.models.querymodel.QueryModelContent#getFilterDefinition()}, per the platform docs' "Filter Items"
 * section ("the filter definition editor uses the same language as Query Model filter definitions"). Mirrors
 * {@link de.a12.studio.modelsvalidation.validators.query.QueryFilterDefinitionSyntaxValidator}: the Studio's own
 * filter item dialog already surfaces this live via {@code RichtextEditorController}'s validator hook, this
 * defends hand-edited JSON the same way.
 */
public final class OverviewFilterDefinitionSyntaxValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/newFilterConfiguration/filterGroups";

  private final QueryLanguageEmitter emitter = new QueryLanguageEmitter();

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
    for (FilterGroup group : filterConfiguration.getFilterGroups()) {
      for (FilterItem item : group.getFilterItems()) {
        if (!FilterItem.TYPE_QUERY.equals(item.getType())) {
          continue;
        }
        String filterDefinition = item.getFilterDefinition();
        if (filterDefinition == null || filterDefinition.isBlank()) {
          continue;
        }
        try {
          emitter.emit(filterDefinition);
        }
        catch (QueryLanguageException e) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              ValidationMessages.get("validation.overviewFilterDefinition.invalidSyntax", e.getMessage()), Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }
}
