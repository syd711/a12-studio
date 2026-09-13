package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.Column;
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
 * A reference column with no icon and no visible label can't be identified by a screen-reader or
 * sighted user. Mirrors SME's "referenceColumnHeaderShouldHaveLabelOrIcon" WARNING rule.
 */
public final class OverviewColumnHeaderLabelOrIconValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/columns/label";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    DocumentModel documentModel = OverviewElementResolution.referencedDocumentModel(overviewModel, context);
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel, context.otherDocumentModels());
    List<ModelValidationError> errors = new ArrayList<>();
    for (Column column : overviewModel.getContent().getColumns()) {
      // A dangling elementRef is already flagged separately (as an ERROR) by
      // OverviewFieldReferenceValidator - only report this accessibility warning once the field is known to
      // actually exist, identified by its resolved display path rather than its raw internal id.
      if (isMissingLabelOrIcon(column) && index.isResolvable(column.getElementRef())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewColumnHeaderLabelOrIcon.missing", index.resolveDisplayPath(column.getElementRef())),
            Severity.WARNING.name()));
      }
    }
    return errors;
  }

  /** Whether {@code column} would trigger this validator: a reference column with no icon and no visible
   * label. Exposed so UI code (the Columns panel row rendering) can flag the same column live without going
   * through the validation service, which can't tell this warning apart per-column since every column shares
   * {@link #ELEMENT_ID}. */
  public static boolean isMissingLabelOrIcon(Column column) {
    if (column == null || column.getElementRef() == null || column.getElementRef().isBlank()) {
      return false;
    }
    boolean hasIcon = column.getIcon() != null && column.getIcon().getName() != null && !column.getIcon().getName().isBlank();
    if (hasIcon) {
      return false;
    }
    boolean labelHidden = Boolean.TRUE.equals(column.getLabelHidden());
    boolean hasVisibleLabelText = column.getLabel().stream().anyMatch(OverviewColumnHeaderLabelOrIconValidator::hasText);
    return labelHidden || !hasVisibleLabelText;
  }

  private static boolean hasText(Label label) {
    return label.getText() != null && !label.getText().isBlank();
  }
}
