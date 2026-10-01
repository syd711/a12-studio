package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormAnnotationDuplicateValidatorTest {

  private final FormAnnotationDuplicateValidator validator = new FormAnnotationDuplicateValidator();

  @Test
  void duplicatesAreFoundOnControlsAndColumnsAndSameNamesAcrossNodesAreFine() {
    Control control = new Control();
    control.setId("control1");
    control.getAnnotations().addAll(List.of(annotation("a"), annotation("a")));
    Control other = new Control();
    other.setId("control2");
    other.getAnnotations().add(annotation("a"));
    FieldBasedRepeatOverviewColumn column = new FieldBasedRepeatOverviewColumn();
    column.setId("column1");
    column.getAnnotations().addAll(List.of(annotation("b"), annotation("c"), annotation("b")));
    InlineRepeat repeat = new InlineRepeat();
    repeat.setId("repeat1");
    repeat.getRepeatOverviewColumn().add(column);

    FormModel model = new FormModel();
    model.setId("Test_FM");
    model.setContent(new FormModelContent());
    Screen screen = new Screen();
    screen.setId("screen1");
    Section section = new Section();
    section.setId("section1");
    ControlGrid grid = new ControlGrid();
    Row row = new Row();
    row.getCell().addAll(List.of(control, other));
    grid.getRow().add(row);
    section.getScreenElements().add(grid);
    section.getScreenElements().add(repeat);
    screen.getScreenElements().add(section);
    model.getContent().getScreens().add(screen);

    List<ModelValidationError> errors = validator.validate(model, TestModels.context(model));

    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(e -> "control1".equals(e.elementId()) && e.message().contains("\"a\"")));
    assertTrue(errors.stream().anyMatch(e -> "column1".equals(e.elementId()) && e.message().contains("\"b\"")));
  }

  private static Annotation annotation(String name) {
    Annotation annotation = new Annotation();
    annotation.setName(name);
    return annotation;
  }
}
