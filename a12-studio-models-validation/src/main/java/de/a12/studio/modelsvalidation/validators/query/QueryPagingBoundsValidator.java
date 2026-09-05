package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/** If set, Page Size must be at least 1 and Page Number must not be negative - the Studio's own paging panel
 * spinners already enforce this, but this defends hand-edited JSON. */
public final class QueryPagingBoundsValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/paging";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null
        || queryModel.getContent().getPaging() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    Integer pageSize = queryModel.getContent().getPaging().getPageSize();
    if (pageSize != null && pageSize < 1) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.queryPaging.pageSizeTooLow"), Severity.ERROR.name()));
    }
    Integer pageNumber = queryModel.getContent().getPaging().getPageNumber();
    if (pageNumber != null && pageNumber < 0) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.queryPaging.pageNumberNegative"), Severity.ERROR.name()));
    }
    return errors;
  }
}
