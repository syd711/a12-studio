package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
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
 * sighted user - unless the label is merely absent from the column itself because it's inherited from the
 * referenced Document Model field's own label (SME auto-fills a reference column's label from the field it
 * points at, see {@code docs/modules/overviewModel/0203_overview_columns.adoc}). Mirrors SME's
 * "referenceColumnHeaderShouldHaveLabelOrIcon" rule (kernel condition {@code OMElementRefHasNoLabel}), which
 * only warns when the *field itself* also has no label - explicitly hiding the label via {@code labelHidden}
 * is the one case that still warns regardless of the field's own label, since that deliberately suppresses
 * the accessible name.
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

    ElementIndex documentModelIndex = new ElementIndex(documentModel, context.otherDocumentModels());
    List<ModelValidationError> errors = new ArrayList<>();
    for (Column column : overviewModel.getContent().getColumns()) {
      ElementIndex index = OverviewElementResolution.indexFor(column, documentModelIndex, context);
      // A dangling elementRef is already flagged separately (as an ERROR) by
      // OverviewFieldReferenceValidator - only report this accessibility warning once the field is known to
      // actually exist, identified by its resolved display path rather than its raw internal id.
      if (isMissingLabelOrIcon(column, index) && index.isResolvable(column.getElementRef())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewColumnHeaderLabelOrIcon.missing", index.resolveDisplayPath(column.getElementRef())),
            Severity.WARNING.name()));
      }
    }
    return errors;
  }

  /** Whether {@code column} would trigger this validator: a reference column with no icon, whose label is
   * either explicitly hidden or missing both from the column itself and from the Document Model field it
   * references (via {@code index}). Exposed so UI code (the Columns panel row rendering) can flag the same
   * column live without going through the validation service, which can't tell this warning apart per-column
   * since every column shares {@link #ELEMENT_ID}. */
  public static boolean isMissingLabelOrIcon(Column column, ElementIndex index) {
    if (column == null || column.getElementRef() == null || column.getElementRef().isBlank()) {
      return false;
    }
    boolean hasIcon = column.getIcon() != null && column.getIcon().getName() != null && !column.getIcon().getName().isBlank();
    if (hasIcon) {
      return false;
    }
    if (Boolean.TRUE.equals(column.getLabelHidden())) {
      return true;
    }
    boolean hasVisibleLabelText = column.getLabel().stream().anyMatch(OverviewColumnHeaderLabelOrIconValidator::hasText);
    if (hasVisibleLabelText) {
      return false;
    }
    return index == null || !referencedFieldHasLabel(index, column.getElementRef());
  }

  private static boolean referencedFieldHasLabel(ElementIndex index, String elementRef) {
    Element element = index.resolveElement(elementRef).orElse(null);
    if (!(element instanceof FieldElement fieldElement) || fieldElement.getField() == null) {
      return false;
    }
    return fieldElement.getField().getLabel().stream().anyMatch(OverviewColumnHeaderLabelOrIconValidator::hasText);
  }

  private static boolean hasText(Label label) {
    return label.getText() != null && !label.getText().isBlank();
  }
}
