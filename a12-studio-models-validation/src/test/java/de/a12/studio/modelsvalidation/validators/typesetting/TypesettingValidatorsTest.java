package de.a12.studio.modelsvalidation.validators.typesetting;

import de.a12.studio.models.typesettingmodel.PreventLineBreakRule;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRules;
import de.a12.studio.models.typesettingmodel.SpecialPattern;
import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.models.typesettingmodel.TypesettingModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType.CHARACTER_SEQUENCE;
import static de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType.NUMBER_UNIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * One group of tests per Typesetting Model validator, each against a minimal in-memory model: the rules only
 * look at {@code content.preventLineBreakRules}/{@code orphan}/{@code widow}, same reasoning as {@code
 * SelectionValidatorsTest}. The expected outcomes are the {@code print-typesetting} editor's own.
 */
class TypesettingValidatorsTest {

  @Test
  void characterSequenceAcceptsLettersAndHyphens() {
    assertTrue(valueErrors(rule(CHARACTER_SEQUENCE, "T-shirt"), rule(CHARACTER_SEQUENCE, "Straße")).isEmpty());
  }

  @Test
  void characterSequenceIsRequired() {
    List<ModelValidationError> errors = valueErrors(rule(CHARACTER_SEQUENCE, ""));

    assertEquals(1, errors.size());
    assertEquals("content/preventLineBreakRules/0", errors.get(0).elementId());
    assertEquals("ERROR", errors.get(0).severity());
  }

  @Test
  void characterSequenceRejectsAnythingButLettersAndHyphens() {
    for (String invalid : List.of("T shirt", "abc1", "a.b", "a_b")) {
      assertEquals(1, valueErrors(rule(CHARACTER_SEQUENCE, invalid)).size(), invalid);
    }
  }

  @Test
  void characterSequenceIsLimitedToTwentyCharacters() {
    assertTrue(valueErrors(rule(CHARACTER_SEQUENCE, "a".repeat(20))).isEmpty());
    assertEquals(1, valueErrors(rule(CHARACTER_SEQUENCE, "a".repeat(21))).size());
  }

  @Test
  void numberUnitAcceptsLettersAndUnitSymbols() {
    assertTrue(valueErrors(rule(NUMBER_UNIT, "Km"), rule(NUMBER_UNIT, "%"), rule(NUMBER_UNIT, "$"),
        rule(NUMBER_UNIT, "€"), rule(NUMBER_UNIT, "°C")).isEmpty());
  }

  @Test
  void numberUnitIsRequired() {
    assertEquals(1, valueErrors(rule(NUMBER_UNIT, "")).size());
    assertEquals(1, valueErrors(new PreventLineBreakRule(PreventLineBreakRuleType.NUMBER_UNIT.emptyPattern())).size());
  }

  @Test
  void numberUnitRejectsDigitsSpacesAndSpecialCharacters() {
    for (String invalid : List.of("5", "k m", "m/s", "a.b", "a-b", "(m)", "a_b", "a\\b", "a b")) {
      assertEquals(1, valueErrors(rule(NUMBER_UNIT, invalid)).size(), invalid);
    }
  }

  @Test
  void numberUnitIsLimitedToTwentyCharacters() {
    assertTrue(valueErrors(rule(NUMBER_UNIT, "a".repeat(20))).isEmpty());
    assertEquals(1, valueErrors(rule(NUMBER_UNIT, "a".repeat(21))).size());
  }

  @Test
  void specialPatternMustBeChosen() {
    assertTrue(valueErrors(new PreventLineBreakRule(SpecialPattern.PARAGRAPH_SECTION.regex()),
        new PreventLineBreakRule(SpecialPattern.DOCUMENT_SECTION.regex())).isEmpty());
    assertEquals(1, valueErrors(new PreventLineBreakRule(PreventLineBreakRuleType.SPECIAL_PATTERN.emptyPattern())).size());
  }

  @Test
  void messageNamesTheRowWithinItsOwnTable() {
    // the unit rule is the third rule in the file but the first row of the unit table
    List<ModelValidationError> errors = valueErrors(rule(CHARACTER_SEQUENCE, "T-shirt"), rule(CHARACTER_SEQUENCE, "E-mail"),
        rule(NUMBER_UNIT, ""));

    assertEquals(1, errors.size());
    assertEquals("content/preventLineBreakRules/2", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("row 1"), errors.get(0).message());
  }

  @Test
  void duplicatesAreReportedOnEveryOccurrence() {
    TypesettingModel model = model(rule(CHARACTER_SEQUENCE, "T-shirt"), rule(NUMBER_UNIT, "Km"), rule(CHARACTER_SEQUENCE, "T-shirt"));
    List<ModelValidationError> errors = new TypesettingRuleDuplicateValidator().validate(model, TestModels.context(model));

    assertEquals(List.of("content/preventLineBreakRules/0", "content/preventLineBreakRules/2"),
        errors.stream().map(ModelValidationError::elementId).toList());
  }

  @Test
  void differentRulesAreNoDuplicates() {
    TypesettingModel model = model(rule(CHARACTER_SEQUENCE, "T-shirt"), rule(NUMBER_UNIT, "T-shirt"));

    assertTrue(new TypesettingRuleDuplicateValidator().validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void twoEmptyRowsOfOneKindAreDuplicates() {
    TypesettingModel model = model(rule(NUMBER_UNIT, ""), rule(NUMBER_UNIT, ""));

    assertEquals(2, new TypesettingRuleDuplicateValidator().validate(model, TestModels.context(model)).size());
  }

  @Test
  void orphanAndWidowMustBeWithinZeroAndTen() {
    for (int valid : new int[] {0, 2, 10}) {
      assertTrue(lineLimitErrors(valid, valid).isEmpty(), "valid " + valid);
    }
    assertTrue(lineLimitErrors(null, null).isEmpty());

    assertEquals(List.of(TypesettingElementIds.ORPHAN), lineLimitErrors(11, 2).stream().map(ModelValidationError::elementId).toList());
    assertEquals(List.of(TypesettingElementIds.WIDOW), lineLimitErrors(2, -1).stream().map(ModelValidationError::elementId).toList());
    assertEquals(2, lineLimitErrors(11, 11).size());
  }

  @Test
  void validatorsIgnoreOtherModelTypes() {
    var model = new de.a12.studio.models.selectionmodel.SelectionModel();

    assertTrue(new TypesettingRuleValueValidator().validate(model, TestModels.context(model)).isEmpty());
    assertTrue(new TypesettingRuleDuplicateValidator().validate(model, TestModels.context(model)).isEmpty());
    assertTrue(new TypesettingLineLimitValidator().validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void ruleElementIdsRoundTrip() {
    assertEquals(7, TypesettingElementIds.ruleIndex(TypesettingElementIds.rule(7)));
    assertEquals(-1, TypesettingElementIds.ruleIndex(TypesettingElementIds.ORPHAN));
    assertEquals(-1, TypesettingElementIds.ruleIndex(null));
  }

  private static List<ModelValidationError> valueErrors(PreventLineBreakRule... rules) {
    TypesettingModel model = model(rules);
    return new TypesettingRuleValueValidator().validate(model, TestModels.context(model));
  }

  private static List<ModelValidationError> lineLimitErrors(Integer orphan, Integer widow) {
    TypesettingModel model = model();
    model.getContent().setOrphan(orphan);
    model.getContent().setWidow(widow);
    return new TypesettingLineLimitValidator().validate(model, TestModels.context(model));
  }

  private static PreventLineBreakRule rule(PreventLineBreakRuleType type, String value) {
    return new PreventLineBreakRule(PreventLineBreakRules.toPattern(type, value));
  }

  private static TypesettingModel model(PreventLineBreakRule... rules) {
    TypesettingModel model = new TypesettingModel();
    model.setId("Test_TSM");
    TypesettingModelContent content = new TypesettingModelContent();
    content.setPreventLineBreakRules(new ArrayList<>(List.of(rules)));
    model.setContent(content);
    return model;
  }
}
