package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnStyles;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Column-level rules SME enforces beyond the reference/indexed/repeatable checks of {@link
 * OverviewFieldReferenceValidator}: {@code preferSortingIsRequiredForSortableField} (a Sortable reference
 * column needs a Preferred Sorting), {@code suffixRefMustHaveValidReference}/{@code
 * indexedAnnotationShouldBeNotFalseForDynamicSuffix} (a Number column's Dynamic Suffix field must resolve and
 * not be {@code indexed = false}), the column header/content Style references ({@code mustHaveValidReference}
 * against {@code content.styles}, {@code headerStylesNotUnique}/{@code contentStylesNotUnique} within their own
 * list), and Width's SME minimum ({@link Column#MIN_WIDTH}, not itself a named meta model rule but the same
 * {@code NumberType} constraint as {@link de.a12.studio.models.overviewmodel.OverviewConfiguration#MIN_ACTION_COLUMN_WIDTH}).
 * <p>
 * NOT ported: {@code attachmentDisplayModeIsRequiredForAttachment}/{@code
 * multiSelectDisplayModeIsRequiredForMultiSelect} ({@code [elementType] == "attachment"/"multi-select" AND
 * FieldNotFilled(...DisplayMode)}). Checked directly against the SME source (not guessed): {@code
 * transformations/exportTransformations.ts} strips {@code elementType} from every saved file ({@code
 * elementType: undefined}), and {@code transformations/importTransformations.ts}'s own reconstruction on load
 * derives it FROM whichever display-mode-shaped field is already present ({@code if (column.attachmentDisplayMode)
 * elementType = ATTACHMENT}), not by resolving the reference against the document model - {@code
 * middlewares/updateColumnByElementRef.ts} is the only place that does the real semantic resolution, and it only
 * fires on a live editor interaction (entering the row / changing the reference), not on file load. A column
 * whose attachment/multi-select field was picked once and never revisited - e.g. {@code
 * testing/workspaces/basic/models/Company_OM.json}'s "Logo" column, confirmed by running an early version of
 * this rule against every fixture, 5 real files - therefore has {@code elementType} genuinely absent even in
 * real SME, which the rule's plain field-path condition then never fires on. So the semantically "more correct"
 * live-resolution Studio would otherwise use (as {@link OverviewElementResolution#isAttachment}/{@link
 * OverviewElementResolution#isMultiSelect} already do for the Column dialog's own field-specific panels) is
 * actually *stricter* than SME's real, session-history-dependent rule, and would flag real SME-authored files SME
 * itself accepts. Revisit if a stored, chicken-and-egg-free equivalent of {@code elementType} is ever modeled.
 */
public final class OverviewColumnValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/columns";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    List<Column> columns = overviewModel.getContent().getColumns();
    if (columns.isEmpty()) {
      return List.of();
    }

    DocumentModel documentModel = OverviewElementResolution.referencedDocumentModel(overviewModel, context);
    ElementIndex documentModelIndex = documentModel != null && documentModel.getContent() != null
        && documentModel.getContent().getModelRoot() != null ? new ElementIndex(documentModel, context.otherDocumentModels()) : null;
    Set<String> modelStyles = new HashSet<>(overviewModel.getContent().getStyles());

    List<ModelValidationError> errors = new ArrayList<>();
    for (Column column : columns) {
      validateColumn(model, column, documentModelIndex, context, modelStyles, errors);
    }
    return errors;
  }

  private void validateColumn(A12Model<?> model, Column column, ElementIndex documentModelIndex, ValidationContext context,
      Set<String> modelStyles, List<ModelValidationError> errors) {
    String columnName = column.getId();
    if (column.getWidth() != null && column.getWidth() < Column.MIN_WIDTH) {
      errors.add(error(model, columnName, "validation.overviewColumn.widthTooLow", columnName, Column.MIN_WIDTH));
    }

    boolean referenceColumn = column.getElementRef() != null && !column.getElementRef().isBlank();
    if (referenceColumn) {
      if (Boolean.TRUE.equals(column.getSortable()) && isBlank(column.getPreferredSorting())) {
        errors.add(error(model, columnName, "validation.overviewColumn.preferredSortingRequired", columnName));
      }

      // attachmentDisplayMode/multiSelectDisplayMode-required are NOT enforced here - see the class javadoc.
      ElementIndex index = OverviewElementResolution.indexFor(column, documentModelIndex, context);
      Element element = index != null ? OverviewElementResolution.resolve(index, column.getElementRef()) : null;
      if (element != null && isNumberField(element) && !isBlank(column.getSuffixRef())) {
        Element suffixElement = OverviewElementResolution.resolve(index, column.getSuffixRef());
        if (suffixElement == null) {
          errors.add(error(model, columnName, "validation.overviewColumn.suffixRefMissing", columnName, column.getSuffixRef()));
        }
        else if (Boolean.TRUE.equals(column.getUseDynamicSuffix()) && OverviewElementResolution.isIndexedFalse(suffixElement)) {
          errors.add(error(model, columnName, "validation.common.indexedAnnotationFalse", suffixElement.getName()));
        }
      }
    }

    validateStyles(model, columnName, column.getStyles(), modelStyles, errors);
  }

  private void validateStyles(A12Model<?> model, String columnName, ColumnStyles styles, Set<String> modelStyles,
      List<ModelValidationError> errors) {
    if (styles == null) {
      return;
    }
    validateStyleList(model, columnName, "header", styles.getHeader(), modelStyles, errors);
    validateStyleList(model, columnName, "content", styles.getContent(), modelStyles, errors);
  }

  private void validateStyleList(A12Model<?> model, String columnName, String listName, List<String> styleRefs,
      Set<String> modelStyles, List<ModelValidationError> errors) {
    Set<String> seen = new HashSet<>();
    for (String styleRef : styleRefs) {
      if (styleRef == null || styleRef.isBlank()) {
        continue;
      }
      if (!modelStyles.contains(styleRef)) {
        errors.add(error(model, columnName, "validation.overviewColumn.styleReferenceMissing", columnName, listName, styleRef));
      }
      else if (!seen.add(styleRef)) {
        errors.add(error(model, columnName, "validation.overviewColumn.styleReferenceDuplicate", columnName, listName, styleRef));
      }
    }
  }

  private static boolean isNumberField(Element element) {
    return element instanceof FieldElement fieldElement && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof NumberFieldType;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static ModelValidationError error(A12Model<?> model, String columnName, String messageKey, Object... args) {
    return new ModelValidationError(model, ELEMENT_ID + "/" + columnName, ValidationMessages.get(messageKey, args), Severity.ERROR.name());
  }
}
