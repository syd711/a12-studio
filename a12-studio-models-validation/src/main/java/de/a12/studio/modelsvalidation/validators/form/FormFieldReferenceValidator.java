package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Every field configuration entry must reference a field that still exists in one of the referenced
 * Document Models (the SME problems view reports "a field is referenced in the Form Model that no
 * longer exists in the Document Model").
 */
public final class FormFieldReferenceValidator implements ModelValidator {

  // Fallback elementId for a dangling FieldConfigEntry with no Control/Column left in the tree that still
  // references it (e.g. an orphaned entry after the referencing node was deleted) - there's no tree row to
  // point at in that case, so this never resolves to a highlighted row, matching the previous behavior.
  public static final String ELEMENT_ID = "content/fieldConfiguration";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel)
        || formModel.getContent() == null
        || formModel.getContent().getFieldConfiguration() == null) {
      return List.of();
    }

    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      // Without a resolvable document model there is nothing to check against
      // (FormDocumentModelReferenceValidator already reports the missing reference).
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (FieldConfigEntry entry : formModel.getContent().getFieldConfiguration().getField()) {
      if (entry.getElementRef() == null || entry.getElementRef().isBlank()
          || indexes.stream().anyMatch(index -> index.isResolvable(entry.getElementRef()))) {
        continue;
      }
      String message = ValidationMessages.get("validation.formFieldReference.missing", entry.getElementRef());
      List<Object> referencingNodes = findReferencingNodes(formModel, entry.getElementRef());
      if (referencingNodes.isEmpty()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID, message, Severity.ERROR.name()));
      }
      else {
        for (Object node : referencingNodes) {
          errors.add(new ModelValidationError(model, idOf(node), message, Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }

  /** Every Control/Column in the Screen tree whose {@code elementRef} equals {@code elementRef}. */
  private static List<Object> findReferencingNodes(FormModel formModel, String elementRef) {
    List<Object> matches = new ArrayList<>();
    for (Screen screen : formModel.getContent().getScreens()) {
      visit(screen.getScreenElements(), elementRef, matches);
    }
    return matches;
  }

  private static void visit(List<ScreenElement> elements, String elementRef, List<Object> matches) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      visit(element, elementRef, matches);
    }
  }

  private static void visit(ScreenElement element, String elementRef, List<Object> matches) {
    if (element instanceof Section section) {
      visit(section.getScreenElements(), elementRef, matches);
    }
    else if (element instanceof MultiColumnSection section) {
      visit(section.getScreenElements(), elementRef, matches);
    }
    else if (element instanceof ControlGrid grid) {
      for (Row row : grid.getRow()) {
        for (Cell cell : row.getCell()) {
          if (cell instanceof Control control && elementRef.equals(control.getElementRef())) {
            matches.add(control);
          }
        }
      }
    }
    else if (element instanceof AbstractRepeat repeat) {
      for (RepeatOverviewColumn column : repeat.getRepeatOverviewColumn()) {
        if (column instanceof FieldBasedRepeatOverviewColumn fieldColumn
            && elementRef.equals(fieldColumn.getElementRef())) {
          matches.add(fieldColumn);
        }
      }
      if (repeat instanceof EmbeddedRepeat embeddedRepeat && embeddedRepeat.getControlGrid() != null) {
        visit(embeddedRepeat.getControlGrid(), elementRef, matches);
      }
      else if (repeat instanceof DetachedRepeat detachedRepeat && detachedRepeat.getDetailScreen() != null) {
        visit(detachedRepeat.getDetailScreen().getScreenElements(), elementRef, matches);
      }
    }
  }

  private static String idOf(Object node) {
    if (node instanceof Control control) {
      return control.getId();
    }
    if (node instanceof FieldBasedRepeatOverviewColumn column) {
      return column.getId();
    }
    return ELEMENT_ID;
  }
}
