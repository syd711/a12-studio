package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.a12.studio.modelsvalidation.validators.content.ContentReferenceValidatorsTest.element;
import static de.a12.studio.modelsvalidation.validators.content.ContentReferenceValidatorsTest.page;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Gap 9 of "Content Model: gap review": the length/spacing/numeric half of {@link ContentSettingValueValidator}. */
class ContentSettingValueValidatorTest {

  @Test
  void validLengthValuesHaveNoFinding() {
    ContentModel page = page(null,
        element("keyword", "Box", "style", map("width", "auto")),
        element("pxValue", "Box", "style", map("width", "400px")),
        element("percentValue", "Box", "style", map("width", "50%")),
        element("bareZero", "Box", "style", map("width", "0")),
        element("decimal", "Box", "style", map("width", "12.5px")));

    assertEquals(List.of(), run(page));
  }

  @Test
  void invalidLengthValuesAreReportedByPathAndValue() {
    ContentModel page = page(null,
        element("noUnit", "Box", "style", map("width", "400")),
        element("badUnit", "Box", "style", map("width", "400pixels")),
        element("badKeyword", "Box", "style", map("width", "middle")));

    List<ModelValidationError> errors = run(page);

    assertEquals(List.of("noUnit", "badUnit", "badKeyword"), errors.stream().map(ModelValidationError::elementId).toList(), errors.toString());
    errors.forEach(error -> assertTrue(error.message().contains("style.width"), error.message()));
    assertTrue(errors.get(0).message().contains("\"400\""), errors.get(0).message());
  }

  @Test
  void aRuleOnlyAppliesToItsConfiguredElementTypes() {
    // style.width is not one of Paragraph's settings - a garbage value there is not this validator's concern.
    ContentModel page = page(null, element("p", "Paragraph", "style", map("width", "not-a-length")));

    assertEquals(List.of(), run(page));
  }

  @Test
  void aMissingSettingHasNoFinding() {
    ContentModel page = page(null, element("box", "Box", "style", map()));

    assertEquals(List.of(), run(page));
  }

  @Test
  void keywordOnlyLengthsRejectAnythingOutsideTheKeywordList() {
    ContentModel valid = page(null, element("box", "Box", "style", map("justifyContent", "space-between")));
    assertEquals(List.of(), run(valid));

    ContentModel invalid = page(null, element("box", "Box", "style", map("justifyContent", "16px")));
    List<ModelValidationError> errors = run(invalid);
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("style.justifyContent"), errors.get(0).message());
  }

  @Test
  void spacingShorthandAcceptsOneToFourValidLengthTokens() {
    ContentModel page = page(null,
        element("one", "Box", "style", map("padding", "8px")),
        element("two", "Box", "style", map("padding", "8px 4%")),
        element("three", "Box", "style", map("padding", "8px 4px 2rem")),
        element("four", "Box", "style", map("padding", "8px 4px 2px 0")));

    assertEquals(List.of(), run(page));
  }

  @Test
  void spacingShorthandRejectsAnInvalidTokenOrTooManyTokens() {
    ContentModel badToken = page(null, element("box", "Box", "style", map("padding", "8px xyz")));
    List<ModelValidationError> errors = run(badToken);
    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("style.padding"), errors.get(0).message());

    ContentModel tooMany = page(null, element("box", "Box", "style", map("padding", "8px 8px 8px 8px 8px")));
    assertEquals(1, run(tooMany).size());
  }

  @Test
  void numberSettingsAcceptAJsonNumberOrANumericString() {
    ContentModel page = page(null,
        element("asNumber", "DatePicker", "datePickerConfig", map("minYear", 2000)),
        element("asNumericString", "DatePicker", "datePickerConfig", map("minYear", "2000")));

    assertEquals(List.of(), run(page));
  }

  @Test
  void aNonNumericNumberSettingIsReported() {
    ContentModel page = page(null, element("box", "DatePicker", "datePickerConfig", map("minYear", "not-a-year")));

    List<ModelValidationError> errors = run(page);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("datePickerConfig.minYear"), errors.get(0).message());
  }

  @Test
  void everyValidatorIgnoresModelsThatAreNotContentModels() {
    de.a12.studio.models.formmodel.FormModel form = new de.a12.studio.models.formmodel.FormModel();
    form.setId("Order_FM");
    assertEquals(List.of(), new ContentSettingValueValidator().validate(form, TestModels.context(form)));
  }

  private static java.util.Map<String, Object> map(Object... pairs) {
    return ContentReferenceValidatorsTest.map(pairs);
  }

  private static List<ModelValidationError> run(ContentModel page) {
    List<A12Model<?>> none = new ArrayList<>();
    return new ContentSettingValueValidator().validate(page, TestModels.contextWithOtherModels(page, none.toArray(new A12Model<?>[0])));
  }
}
