package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The columns themselves: a name and a width of at least 0.3 are required, and a header should show something (SME:
 * {@code columnHeaderShouldHaveLabelOrIcon}, a warning - "Empty column header! The column header doesn't have an icon or
 * a visible label.").
 */
public final class TreeColumnValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/columns";
  public static final String HEADER_ELEMENT_ID = "content/columns/label";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (TreeColumn column : treeModel.getContent().getColumns()) {
      String name = column.getName() != null && !column.getName().isBlank() ? column.getName() : column.getId();
      if (column.getName() == null || column.getName().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.treeColumn.nameMissing", column.getId()), Severity.ERROR.name()));
      }
      if (column.getWidth() == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.treeColumn.widthMissing", name), Severity.ERROR.name()));
      }
      else if (column.getWidth() < TreeColumn.MIN_WIDTH) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.treeColumn.widthTooLow", name, TreeColumn.MIN_WIDTH), Severity.ERROR.name()));
      }
      if (isHeaderEmpty(column)) {
        errors.add(new ModelValidationError(model, HEADER_ELEMENT_ID,
            ValidationMessages.get("validation.treeColumn.emptyHeader", name), Severity.WARNING.name()));
      }
    }
    return errors;
  }

  /**
   * Whether {@code column}'s header shows neither an icon nor a text: no icon, and either the label is hidden or no
   * language has a label text. Also used by the Columns panel to flag the row live.
   */
  public static boolean isHeaderEmpty(TreeColumn column) {
    boolean hasIcon = column.getIcon() != null && column.getIcon().getName() != null && !column.getIcon().getName().isBlank();
    if (hasIcon) {
      return false;
    }
    boolean hasVisibleLabel = !Boolean.TRUE.equals(column.getLabelHidden()) && column.getLabel().stream()
        .map(Label::getText)
        .anyMatch(text -> text != null && !text.isBlank());
    return !hasVisibleLabel;
  }
}
