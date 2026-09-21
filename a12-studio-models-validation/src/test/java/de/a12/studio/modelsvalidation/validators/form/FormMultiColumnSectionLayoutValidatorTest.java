package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.ColumnLayout;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormMultiColumnSectionLayoutValidatorTest {

  @Test
  void reportsASectionWithoutAnyLayoutOrWithABlankLg() {
    MultiColumnSection noLayout = section("noLayout", null);
    MultiColumnSection blankLg = section("blankLg", layout(" "));
    MultiColumnSection onlyMd = section("onlyMd", layout(null));
    onlyMd.getLayout().setMd("6-6");
    MultiColumnSection fine = section("fine", layout("6-6"));

    List<ModelValidationError> errors = validate(noLayout, blankLg, onlyMd, fine);

    assertEquals(List.of("noLayout", "blankLg", "onlyMd"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("\"noLayout\""), errors.get(0).message());
  }

  @Test
  void findsASectionInsideADetailScreen() {
    MultiColumnSection inner = section("inner", null);
    Screen detail = new Screen();
    detail.getScreenElements().add(inner);
    DetachedRepeat repeat = new DetachedRepeat();
    repeat.setId("repeat");
    repeat.setDetailScreen(detail);

    assertEquals(List.of("inner"), validate(repeat).stream().map(ModelValidationError::elementId).toList());
  }

  private static MultiColumnSection section(String id, ColumnLayout layout) {
    MultiColumnSection section = new MultiColumnSection();
    section.setId(id);
    section.setName(id);
    section.setLayout(layout);
    return section;
  }

  private static ColumnLayout layout(String lg) {
    ColumnLayout layout = new ColumnLayout();
    layout.setLg(lg);
    return layout;
  }

  private static List<ModelValidationError> validate(ScreenElement... elements) {
    FormModel model = new FormModel();
    model.setId("Layout_FM");
    model.setContent(new FormModelContent());
    Screen screen = new Screen();
    screen.getScreenElements().addAll(List.of(elements));
    model.getContent().getScreens().add(screen);
    return new FormMultiColumnSectionLayoutValidator().validate(model, TestModels.context(model));
  }
}
