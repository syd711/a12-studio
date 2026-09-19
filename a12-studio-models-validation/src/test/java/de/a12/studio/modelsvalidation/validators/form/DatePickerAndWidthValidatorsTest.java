package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DatePickerConfig;
import de.a12.studio.models.formmodel.ExpressionRepeatOverviewColumn;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatePickerAndWidthValidatorsTest {

  private static DatePickerConfig config(Integer min, Integer max, Boolean absolute, Integer preselection) {
    DatePickerConfig config = new DatePickerConfig();
    config.setMinYear(min);
    config.setMaxYear(max);
    config.setAbsolute(absolute);
    config.setPreselectionYear(preselection);
    return config;
  }

  // ---- which fields have a date picker ----

  @Test
  void dateDateTimeAndYyyyMmDdRangesHaveADatePicker() {
    assertTrue(DatePickerSupport.isSupported(new DateFieldType()));
    assertTrue(DatePickerSupport.isSupported(new DateTimeFieldType()));

    DateRangeFieldType range = new DateRangeFieldType();
    range.getDateRangeType().setFormat("YYYY-MM-DD");
    assertTrue(DatePickerSupport.isSupported(range));
    range.getDateRangeType().setFormat("YYYY-MM");
    assertFalse(DatePickerSupport.isSupported(range));

    assertFalse(DatePickerSupport.isSupported(new StringFieldType()));
    assertFalse(DatePickerSupport.isSupported(new NumberFieldType()));
    assertFalse(DatePickerSupport.isSupported(null));
  }

  // ---- redundant configs (SME's removeEmptyDatePickerConfig) ----

  @Test
  void aConfigWithoutInformationIsRedundant() {
    assertTrue(DatePickerSupport.isRedundant(null));
    assertTrue(DatePickerSupport.isRedundant(new DatePickerConfig()));
    assertTrue(DatePickerSupport.isRedundant(config(null, null, false, null)), "only absolute:false");
    assertTrue(DatePickerSupport.isRedundant(config(null, null, null, 0)), "a preselection of 0 is none");

    assertFalse(DatePickerSupport.isRedundant(config(-7, null, null, null)));
    assertFalse(DatePickerSupport.isRedundant(config(null, 7, false, null)));
    assertFalse(DatePickerSupport.isRedundant(config(null, null, true, null)));
    assertFalse(DatePickerSupport.isRedundant(config(null, null, null, -3)));
    assertFalse(DatePickerSupport.isRedundant(config(0, null, null, null)), "year 0 is a value, unlike a preselection of 0");
  }

  // ---- the three rules of I_DatePickerConfig.json ----

  @Test
  void theRangeRulesMatchSme() {
    assertEquals(List.of(), DatePickerSupport.problems(config(-70, -18, false, -34)), "the real fixture's values");
    assertEquals(List.of(), DatePickerSupport.problems(config(5, 5, null, 5)), "min equal to max is fine");
    assertEquals(List.of(), DatePickerSupport.problems(config(null, null, null, 99)), "no range to compare with");

    assertEquals(List.of(DatePickerSupport.Problem.MIN_YEAR_ABOVE_MAX_YEAR), DatePickerSupport.problems(config(5, 2, null, null)));
    assertEquals(List.of(DatePickerSupport.Problem.PRESELECTION_OUTSIDE_RANGE), DatePickerSupport.problems(config(-10, 10, null, 11)));
    assertEquals(List.of(DatePickerSupport.Problem.PRESELECTION_OUTSIDE_RANGE), DatePickerSupport.problems(config(-10, 10, null, -11)));
    assertEquals(List.of(DatePickerSupport.Problem.NEGATIVE_YEAR_WHEN_ABSOLUTE), DatePickerSupport.problems(config(-5, 2030, true, null)));
    assertEquals(List.of(), DatePickerSupport.problems(config(1990, 2030, true, 2000)));
    assertEquals(List.of(), DatePickerSupport.problems(null));
  }

  // ---- validators ----

  private static FormModel model(Control control, InlineRepeat repeat) {
    FormModel model = new FormModel();
    model.setId("DatePicker_FM");
    model.setContent(new FormModelContent());
    Screen screen = new Screen();
    screen.setId("screen1");
    Section section = new Section();
    section.setId("section1");
    ControlGrid grid = new ControlGrid();
    Row row = new Row();
    row.getCell().add(control);
    grid.getRow().add(row);
    section.getScreenElements().add(grid);
    section.getScreenElements().add(repeat);
    screen.getScreenElements().add(section);
    model.getContent().getScreens().add(screen);
    return model;
  }

  @Test
  void theDatePickerValidatorFindsControlsAndColumnsAnywhere() {
    Control control = new Control();
    control.setId("control1");
    control.setDatePickerConfig(config(5, 2, null, null));
    FieldBasedRepeatOverviewColumn column = new FieldBasedRepeatOverviewColumn();
    column.setId("column1");
    column.setDatePickerConfig(config(-5, 3, true, null));
    FieldBasedRepeatOverviewColumn fine = new FieldBasedRepeatOverviewColumn();
    fine.setId("column2");
    fine.setDatePickerConfig(config(-7, 7, false, null));
    InlineRepeat repeat = new InlineRepeat();
    repeat.setId("repeat1");
    repeat.getRepeatOverviewColumn().addAll(List.of(column, fine));
    FormModel model = model(control, repeat);

    List<ModelValidationError> errors = new FormDatePickerConfigValidator().validate(model, TestModels.context(model));

    assertEquals(List.of("control1", "column1"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("minimal year"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("negative"), errors.get(1).message());
  }

  // ---- column width ----

  @Test
  void aWidthNeedsAtLeastOneThirdAndOneDecimal() {
    for (double valid : new double[] {0.3, 0.4, 0.8, 1, 1.0, 1.5, 2, 2.5, 10}) {
      assertTrue(FormColumnWidthValidator.isValid(valid), "valid: " + valid);
    }
    for (double invalid : new double[] {0, -1, 0.2, 0.29, 0.25, 1.25, 0.85, 1.05}) {
      assertFalse(FormColumnWidthValidator.isValid(invalid), "invalid: " + invalid);
    }
  }

  @Test
  void theWidthValidatorReportsTheColumnsWithABadWidthOnly() {
    Control control = new Control();
    control.setId("control1");
    FieldBasedRepeatOverviewColumn narrow = new FieldBasedRepeatOverviewColumn();
    narrow.setId("narrow");
    narrow.setWidth(0.2);
    ExpressionRepeatOverviewColumn precise = new ExpressionRepeatOverviewColumn();
    precise.setId("precise");
    precise.setWidth(1.25);
    FieldBasedRepeatOverviewColumn fine = new FieldBasedRepeatOverviewColumn();
    fine.setId("fine");
    fine.setWidth(0.8);
    FieldBasedRepeatOverviewColumn none = new FieldBasedRepeatOverviewColumn();
    none.setId("none");
    InlineRepeat repeat = new InlineRepeat();
    repeat.setId("repeat1");
    repeat.getRepeatOverviewColumn().addAll(List.of(narrow, precise, fine, none));
    FormModel model = model(control, repeat);

    List<ModelValidationError> errors = new FormColumnWidthValidator().validate(model, TestModels.context(model));

    assertEquals(List.of("narrow", "precise"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("0.3") || errors.get(0).message().contains("0,3"), errors.get(0).message());
  }
}
