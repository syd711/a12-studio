package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtual scrolling needs a fixed row height and a fixed action column width (SME: {@code rowHeightIsRequired},
 * {@code actionColumnWidthIsRequired}, "This field is required."); the two numbers have SME's minimums (Row Height 1,
 * Action Column Width 0.3) whether or not virtual scrolling is on.
 */
public final class TreeVirtualScrollingValidator implements ModelValidator {

  public static final String ROW_HEIGHT_ELEMENT_ID = "content/configuration/rowHeight";
  public static final String ACTION_COLUMN_WIDTH_ELEMENT_ID = "content/configuration/actionColumnWidth";

  private static final int MIN_ROW_HEIGHT = 1;

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel) || treeModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    TreeConfiguration configuration = treeModel.getContent().getConfiguration();
    boolean virtualScrolling = Boolean.TRUE.equals(configuration.getEnableVirtualScroll());
    List<ModelValidationError> errors = new ArrayList<>();

    Integer rowHeight = configuration.getRowHeight();
    if (rowHeight == null && virtualScrolling) {
      errors.add(error(model, ROW_HEIGHT_ELEMENT_ID, "validation.treeVirtualScrolling.rowHeightMissing"));
    }
    else if (rowHeight != null && rowHeight < MIN_ROW_HEIGHT) {
      errors.add(error(model, ROW_HEIGHT_ELEMENT_ID, "validation.treeVirtualScrolling.rowHeightTooLow"));
    }

    Double actionColumnWidth = configuration.getActionColumnWidth();
    if (actionColumnWidth == null && virtualScrolling) {
      errors.add(error(model, ACTION_COLUMN_WIDTH_ELEMENT_ID, "validation.treeVirtualScrolling.actionColumnWidthMissing"));
    }
    else if (actionColumnWidth != null && actionColumnWidth < TreeColumn.MIN_WIDTH) {
      errors.add(error(model, ACTION_COLUMN_WIDTH_ELEMENT_ID, "validation.treeVirtualScrolling.actionColumnWidthTooLow"));
    }
    return errors;
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String messageKey) {
    return new ModelValidationError(model, elementId, ValidationMessages.get(messageKey), Severity.ERROR.name());
  }
}
