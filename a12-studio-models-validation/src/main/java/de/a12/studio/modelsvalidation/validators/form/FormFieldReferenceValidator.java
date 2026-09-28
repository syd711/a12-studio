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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Every Control/Column in the Screens tree, and every field configuration entry, must reference a field that
 * still exists in one of the referenced Document Models (the SME problems view reports "a field is referenced
 * in the Form Model that no longer exists in the Document Model"). Most Controls have no {@link FieldConfigEntry}
 * at all (it only exists once the field gets an initial value or dependent-field config), so the tree itself -
 * not just {@code fieldConfiguration.field} - has to be scanned for dangling {@code elementRef}s.
 */
public final class FormFieldReferenceValidator implements ModelValidator {

  // Fallback elementId for a dangling FieldConfigEntry with no Control/Column left in the tree that still
  // references it (e.g. an orphaned entry after the referencing node was deleted) - there's no tree row to
  // point at in that case, so this never resolves to a highlighted row, matching the previous behavior.
  public static final String ELEMENT_ID = "content/fieldConfiguration";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }

    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      // Without a resolvable document model there is nothing to check against
      // (FormDocumentModelReferenceValidator already reports the missing reference).
      return List.of();
    }

    Map<String, List<Object>> referencingNodesByRef = new LinkedHashMap<>();
    visitAll(formModel, (ref, node) -> referencingNodesByRef.computeIfAbsent(ref, k -> new ArrayList<>()).add(node));

    Set<String> elementRefs = new LinkedHashSet<>(referencingNodesByRef.keySet());
    if (formModel.getContent().getFieldConfiguration() != null) {
      for (FieldConfigEntry entry : formModel.getContent().getFieldConfiguration().getField()) {
        if (entry.getElementRef() != null && !entry.getElementRef().isBlank()) {
          elementRefs.add(entry.getElementRef());
        }
      }
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (String elementRef : elementRefs) {
      if (indexes.stream().anyMatch(index -> index.isResolvable(elementRef))) {
        continue;
      }
      String message = ValidationMessages.get("validation.formFieldReference.missing", elementRef);
      List<Object> referencingNodes = referencingNodesByRef.getOrDefault(elementRef, List.of());
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
  static List<Object> findReferencingNodes(FormModel formModel, String elementRef) {
    List<Object> matches = new ArrayList<>();
    visitAll(formModel, (ref, node) -> {
      if (elementRef.equals(ref)) {
        matches.add(node);
      }
    });
    return matches;
  }

  /** Visits every Control/Column in the Screen tree that carries a non-blank {@code elementRef}. */
  private static void visitAll(FormModel formModel, BiConsumer<String, Object> onRef) {
    for (Screen screen : formModel.getContent().getScreens()) {
      visit(screen.getScreenElements(), onRef);
    }
  }

  private static void visit(List<ScreenElement> elements, BiConsumer<String, Object> onRef) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      visit(element, onRef);
    }
  }

  private static void visit(ScreenElement element, BiConsumer<String, Object> onRef) {
    if (element instanceof Section section) {
      visit(section.getScreenElements(), onRef);
    }
    else if (element instanceof MultiColumnSection section) {
      visit(section.getScreenElements(), onRef);
    }
    else if (element instanceof ControlGrid grid) {
      for (Row row : grid.getRow()) {
        for (Cell cell : row.getCell()) {
          if (cell instanceof Control control && control.getElementRef() != null && !control.getElementRef().isBlank()) {
            onRef.accept(control.getElementRef(), control);
          }
        }
      }
    }
    else if (element instanceof AbstractRepeat repeat) {
      for (RepeatOverviewColumn column : repeat.getRepeatOverviewColumn()) {
        if (column instanceof FieldBasedRepeatOverviewColumn fieldColumn
            && fieldColumn.getElementRef() != null && !fieldColumn.getElementRef().isBlank()) {
          onRef.accept(fieldColumn.getElementRef(), fieldColumn);
        }
      }
      if (repeat instanceof EmbeddedRepeat embeddedRepeat && embeddedRepeat.getControlGrid() != null) {
        visit(embeddedRepeat.getControlGrid(), onRef);
      }
      else if (repeat instanceof DetachedRepeat detachedRepeat && detachedRepeat.getDetailScreen() != null) {
        visit(detachedRepeat.getDetailScreen().getScreenElements(), onRef);
      }
    }
  }

  static String idOf(Object node) {
    if (node instanceof Control control) {
      return control.getId();
    }
    if (node instanceof FieldBasedRepeatOverviewColumn column) {
      return column.getId();
    }
    return ELEMENT_ID;
  }
}
