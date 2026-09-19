package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.CustomScreenElement;
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

class InitialFocusAndCustomElementHeightValidatorsTest {

  private static Control control(String id, Boolean readonly) {
    Control control = new Control();
    control.setId(id);
    control.setReadonly(readonly);
    return control;
  }

  private static ControlGrid grid(Boolean readonly, Control... controls) {
    ControlGrid grid = new ControlGrid();
    grid.setReadonly(readonly);
    Row row = new Row();
    row.getCell().addAll(List.of(controls));
    grid.getRow().add(row);
    return grid;
  }

  private static Screen screen(String id, String focusedId, de.a12.studio.models.formmodel.ScreenElement... elements) {
    Screen screen = new Screen();
    screen.setId(id);
    screen.setName("Name_" + id);
    screen.setInitiallyFocusedElementId(focusedId);
    Section section = new Section();
    section.setId("section_" + id);
    section.getScreenElements().addAll(List.of(elements));
    screen.getScreenElements().add(section);
    return screen;
  }

  private static FormModel model(Screen... screens) {
    FormModel model = new FormModel();
    model.setId("Focus_FM");
    model.setContent(new FormModelContent());
    model.getContent().getScreens().addAll(List.of(screens));
    return model;
  }

  private static List<ModelValidationError> validate(FormModel model) {
    return new FormInitiallyFocusedElementValidator().validate(model, TestModels.context(model));
  }

  // ---- initially focused element ----

  @Test
  void anEditableControlOnTheFirstScreenCanTakeTheFocus() {
    FormModel model = model(screen("s1", "c2", grid(null, control("c1", null), control("c2", false))));

    assertEquals(List.of(), validate(model));
  }

  @Test
  void noFocusedElementIsFineOnAnyScreen() {
    FormModel model = model(screen("s1", null), screen("s2", ""), screen("s3", "  "));

    assertEquals(List.of(), validate(model));
  }

  @Test
  void aFocusedElementOnALaterScreenIsAnErrorNamingTheScreen() {
    FormModel model = model(screen("s1", null, grid(null, control("c1", null))),
        screen("s2", "c2", grid(null, control("c2", null))));

    List<ModelValidationError> errors = validate(model);

    assertEquals(List.of("s2"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("Name_s2") && errors.get(0).message().contains("first screen"), errors.get(0).message());
  }

  @Test
  void theFocusedElementMustExistAndBeAControl() {
    FormModel model = model(screen("s1", "gone", grid(null, control("c1", null))));

    List<ModelValidationError> errors = validate(model);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("\"gone\"") && errors.get(0).message().contains("Name_s1"), errors.get(0).message());
  }

  @Test
  void aReadonlyControlOrOneInAReadonlyGridCannotTakeTheFocus() {
    assertEquals(1, validate(model(screen("s1", "c1", grid(null, control("c1", true))))).size(), "readonly control");
    assertEquals(1, validate(model(screen("s1", "c1", grid(true, control("c1", null))))).size(), "readonly grid");
    assertEquals(0, validate(model(screen("s1", "c1", grid(false, control("c1", null))))).size(), "readonly=false is editable");
  }

  @Test
  void aControlInARepeatCannotTakeTheFocus() {
    InlineRepeat repeat = new InlineRepeat();
    repeat.setId("repeat1");
    // A Control reached through a repeat is not addressable, it exists once per row.
    Section inner = new Section();
    inner.setId("inner");
    inner.getScreenElements().add(grid(null, control("c1", null)));
    FormModel model = model(screen("s1", "c1", repeat, inner));
    assertEquals(0, validate(model).size(), "control beside the repeat is fine");

    Screen screen = model.getContent().getScreens().get(0);
    assertEquals(List.of("c1"), InitiallyFocusedElementSupport.focusableControls(screen).stream().map(Control::getId).toList());
  }

  @Test
  void onlyTheFirstScreenIsFirst() {
    FormModel model = model(screen("s1", null), screen("s2", null));
    FormModelContent content = model.getContent();

    assertTrue(InitiallyFocusedElementSupport.isFirstScreen(content, content.getScreens().get(0)));
    assertEquals(false, InitiallyFocusedElementSupport.isFirstScreen(content, content.getScreens().get(1)));
    assertEquals(false, InitiallyFocusedElementSupport.isFirstScreen(null, content.getScreens().get(0)));
  }

  // ---- custom screen element height ----

  private static FormModel modelWithCustomElement(Integer height) {
    CustomScreenElement element = new CustomScreenElement();
    element.setId("custom1");
    element.setName("Relationships");
    element.setHeight(height);
    return model(screen("s1", null, element));
  }

  @Test
  void aZeroHeightIsAnErrorNamingTheElement() {
    FormModel model = modelWithCustomElement(0);

    List<ModelValidationError> errors = new FormCustomScreenElementHeightValidator().validate(model, TestModels.context(model));

    assertEquals(List.of("custom1"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("Relationships"), errors.get(0).message());
  }

  @Test
  void aMissingOrPositiveHeightIsFine() {
    for (Integer height : new Integer[] {null, 500, 1}) {
      FormModel model = modelWithCustomElement(height);
      assertEquals(List.of(), new FormCustomScreenElementHeightValidator().validate(model, TestModels.context(model)), "height " + height);
    }
  }
}
