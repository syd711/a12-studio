package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
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
import de.a12.studio.modelsvalidation.validators.AnnotationDuplicateValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Form Model counterpart of {@link AnnotationDuplicateValidator}: SME's {@code I_Annotated} mixin
 * ({@code RepetitionNotUnique(annotations/name)}) is composed into screens, screen elements, rows, controls and
 * repeat overview columns, so none of them may declare the same annotation {@code name} twice. Buttons, config
 * entries and row actions are not walked.
 */
public final class FormAnnotationDuplicateValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null
        || formModel.getContent().getScreens() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Screen screen : formModel.getContent().getScreens()) {
      check(model, screen.getId(), screen.getAnnotations(), errors);
      visit(model, screen.getScreenElements(), errors);
    }
    return errors;
  }

  private void visit(A12Model<?> model, List<ScreenElement> elements, List<ModelValidationError> errors) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      check(model, element.getId(), element.getAnnotations(), errors);
      if (element instanceof AbstractRepeat repeat) {
        for (RepeatOverviewColumn column : repeat.getRepeatOverviewColumn()) {
          check(model, column.getId(), column.getAnnotations(), errors);
        }
      }
      if (element instanceof Section section) {
        visit(model, section.getScreenElements(), errors);
      }
      else if (element instanceof MultiColumnSection section) {
        visit(model, section.getScreenElements(), errors);
      }
      else if (element instanceof ControlGrid grid) {
        visitRows(model, grid, errors);
      }
      else if (element instanceof EmbeddedRepeat repeat && repeat.getControlGrid() != null) {
        visitRows(model, repeat.getControlGrid(), errors);
      }
      else if (element instanceof DetachedRepeat repeat && repeat.getDetailScreen() != null) {
        visit(model, repeat.getDetailScreen().getScreenElements(), errors);
      }
    }
  }

  private void visitRows(A12Model<?> model, ControlGrid grid, List<ModelValidationError> errors) {
    for (Row row : grid.getRow()) {
      check(model, row.getId(), row.getAnnotations(), errors);
      for (Cell cell : row.getCell()) {
        if (cell instanceof Control control) {
          check(model, control.getId(), control.getAnnotations(), errors);
        }
      }
    }
  }

  private void check(A12Model<?> model, String nodeId, List<Annotation> annotations,
      List<ModelValidationError> errors) {
    if (annotations == null) {
      return;
    }
    Set<String> seen = new HashSet<>();
    Set<String> reported = new HashSet<>();
    for (Annotation annotation : annotations) {
      String name = annotation.getName();
      if (name == null || name.isBlank()) {
        continue;
      }
      if (!seen.add(name) && reported.add(name)) {
        errors.add(new ModelValidationError(model, nodeId,
            ValidationMessages.get("validation.annotationDuplicate", name), Severity.ERROR.name()));
      }
    }
  }
}
