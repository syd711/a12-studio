package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.ExpressionRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.InlineRepeat;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormInitialSortingColumnSortableValidatorTest {

  @Test
  void reportsAnInitialSortingColumnThatIsNotSortableWhateverItsType() {
    FieldBasedRepeatOverviewColumn unset = new FieldBasedRepeatOverviewColumn();
    unset.setId("unset");
    ExpressionRepeatOverviewColumn explicitlyOff = new ExpressionRepeatOverviewColumn();
    explicitlyOff.setId("off");
    explicitlyOff.setSortable(false);
    InlineRepeat first = repeat("repeat1", "unset", unset);
    InlineRepeat second = repeat("repeat2", "off", explicitlyOff);

    List<ModelValidationError> errors = validate(first, second);

    assertEquals(List.of("unset", "off"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("\"unset\"") && errors.get(0).message().contains("repeat1"), errors.get(0).message());
  }

  @Test
  void acceptsASortableInitialSortingColumnAndOtherNonSortableColumns() {
    FieldBasedRepeatOverviewColumn sortable = new FieldBasedRepeatOverviewColumn();
    sortable.setId("sortable");
    sortable.setSortable(true);
    FieldBasedRepeatOverviewColumn other = new FieldBasedRepeatOverviewColumn();
    other.setId("other");
    InlineRepeat repeat = repeat("repeat1", "sortable", sortable, other);

    assertEquals(List.of(), validate(repeat));
  }

  @Test
  void ignoresARepeatWithoutInitialSortingAndOneThatNamesAColumnThatIsGone() {
    FieldBasedRepeatOverviewColumn column = new FieldBasedRepeatOverviewColumn();
    column.setId("column");
    InlineRepeat noSorting = repeat("repeat1", null, column);
    InlineRepeat dangling = repeat("repeat2", "deleted", column);

    assertEquals(List.of(), validate(noSorting, dangling));
  }

  @Test
  void findsARepeatInsideADetailScreen() {
    FieldBasedRepeatOverviewColumn column = new FieldBasedRepeatOverviewColumn();
    column.setId("column");
    InlineRepeat inner = repeat("inner", "column", column);
    Section detailSection = new Section();
    detailSection.getScreenElements().add(inner);
    Screen detail = new Screen();
    detail.getScreenElements().add(detailSection);
    DetachedRepeat detached = new DetachedRepeat();
    detached.setId("detached");
    detached.setDetailScreen(detail);

    assertEquals(List.of("column"), validate(detached).stream().map(ModelValidationError::elementId).toList());
  }

  private static InlineRepeat repeat(String id, String initialSorting, de.a12.studio.models.formmodel.RepeatOverviewColumn... columns) {
    InlineRepeat repeat = new InlineRepeat();
    repeat.setId(id);
    repeat.setInitialSorting(initialSorting);
    repeat.getRepeatOverviewColumn().addAll(List.of(columns));
    return repeat;
  }

  private static List<ModelValidationError> validate(de.a12.studio.models.formmodel.ScreenElement... repeats) {
    FormModel model = new FormModel();
    model.setId("Sorting_FM");
    model.setContent(new FormModelContent());
    Section section = new Section();
    section.getScreenElements().addAll(List.of(repeats));
    Screen screen = new Screen();
    screen.getScreenElements().add(section);
    model.getContent().getScreens().add(screen);
    return new FormInitialSortingColumnSortableValidator().validate(model, TestModels.context(model));
  }
}
