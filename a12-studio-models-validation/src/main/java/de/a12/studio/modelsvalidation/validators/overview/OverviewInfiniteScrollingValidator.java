package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Pagination needs a Paging Size (SME: {@code pagingSizeIsRequired}); Infinite Scrolling needs a fixed Row
 * Height and Action Column Width (SME: {@code rowHeightIsRequired}, {@code actionColumnWidthIsRequired},
 * "This field is required."), with SME's minimums (Row Height >= 1, Action Column Width >= {@link
 * OverviewConfiguration#MIN_ACTION_COLUMN_WIDTH}) enforced whichever behaviour is active - mirrors {@code
 * de.a12.studio.modelsvalidation.validators.tree.TreeVirtualScrollingValidator}. {@link
 * OverviewPagingSizeValidator} separately covers "Paging Size, if set, must be at least 1" for the case where
 * this validator does not fire (e.g. infinite scrolling with a leftover pagingSize).
 */
public final class OverviewInfiniteScrollingValidator implements ModelValidator {

  public static final String PAGING_SIZE_ELEMENT_ID = "content/configuration/pagingSize";
  public static final String ROW_HEIGHT_ELEMENT_ID = "content/configuration/rowHeight";
  public static final String ACTION_COLUMN_WIDTH_ELEMENT_ID = "content/configuration/actionColumnWidth";

  private static final int MIN_ROW_HEIGHT = 1;

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel) || overviewModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    OverviewConfiguration configuration = overviewModel.getContent().getConfiguration();
    boolean infiniteScrolling = Boolean.TRUE.equals(configuration.getEnableInfiniteScroll());
    List<ModelValidationError> errors = new ArrayList<>();

    if (!infiniteScrolling && configuration.getPagingSize() == null) {
      errors.add(error(model, PAGING_SIZE_ELEMENT_ID, "validation.overviewInfiniteScrolling.pagingSizeRequired"));
    }

    Integer rowHeight = configuration.getRowHeight();
    if (rowHeight == null && infiniteScrolling) {
      errors.add(error(model, ROW_HEIGHT_ELEMENT_ID, "validation.overviewInfiniteScrolling.rowHeightRequired"));
    }
    else if (rowHeight != null && rowHeight < MIN_ROW_HEIGHT) {
      errors.add(error(model, ROW_HEIGHT_ELEMENT_ID, "validation.overviewInfiniteScrolling.rowHeightTooLow"));
    }

    Double actionColumnWidth = configuration.getActionColumnWidth();
    if (actionColumnWidth == null && infiniteScrolling) {
      errors.add(error(model, ACTION_COLUMN_WIDTH_ELEMENT_ID, "validation.overviewInfiniteScrolling.actionColumnWidthRequired"));
    }
    else if (actionColumnWidth != null && actionColumnWidth < OverviewConfiguration.MIN_ACTION_COLUMN_WIDTH) {
      errors.add(error(model, ACTION_COLUMN_WIDTH_ELEMENT_ID, "validation.overviewInfiniteScrolling.actionColumnWidthTooLow"));
    }
    return errors;
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String messageKey) {
    return new ModelValidationError(model, elementId, ValidationMessages.get(messageKey), Severity.ERROR.name());
  }
}
