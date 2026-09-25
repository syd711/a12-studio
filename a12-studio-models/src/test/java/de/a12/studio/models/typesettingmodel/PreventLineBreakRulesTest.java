package de.a12.studio.models.typesettingmodel;

import org.junit.jupiter.api.Test;

import static de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType.CHARACTER_SEQUENCE;
import static de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType.NUMBER_UNIT;
import static de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType.SPECIAL_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PreventLineBreakRulesTest {

  private static final String KM = "[+-]?(\\d{1,3}(?:[.,]\\d{3})+|\\d+)(?:[.,]\\d+)? Km";

  @Test
  void classifiesEveryKindLikeSme() {
    assertEquals(CHARACTER_SEQUENCE, PreventLineBreakRules.classify("T-shirt"));
    assertEquals(CHARACTER_SEQUENCE, PreventLineBreakRules.classify("E\\.mail"));
    assertEquals(NUMBER_UNIT, PreventLineBreakRules.classify(KM));
    assertEquals(SPECIAL_PATTERN, PreventLineBreakRules.classify(SpecialPattern.PARAGRAPH_SECTION.regex()));
    assertEquals(SPECIAL_PATTERN, PreventLineBreakRules.classify(SpecialPattern.DOCUMENT_SECTION.regex()));
  }

  @Test
  void aRuleWithoutValueKeepsItsKind() {
    assertEquals(CHARACTER_SEQUENCE, PreventLineBreakRules.classify(""));
    assertEquals(CHARACTER_SEQUENCE, PreventLineBreakRules.classify(null));
    assertEquals(CHARACTER_SEQUENCE, PreventLineBreakRules.classify("{{character}}"));
    assertEquals(NUMBER_UNIT, PreventLineBreakRules.classify("{{unit}}"));
    assertEquals(NUMBER_UNIT, PreventLineBreakRules.classify(PreventLineBreakRules.toPattern(NUMBER_UNIT, "")));
    assertEquals(SPECIAL_PATTERN, PreventLineBreakRules.classify("{{special}}"));
    for (PreventLineBreakRuleType type : PreventLineBreakRuleType.values()) {
      assertEquals(type, PreventLineBreakRules.classify(type.emptyPattern()));
      assertEquals("", PreventLineBreakRules.toValue(type, type.emptyPattern()));
    }
  }

  @Test
  void convertsCharacterSequencesBothWays() {
    assertEquals("T-shirt", PreventLineBreakRules.toValue(CHARACTER_SEQUENCE, "T-shirt"));
    assertEquals("T-shirt", PreventLineBreakRules.toPattern(CHARACTER_SEQUENCE, "T-shirt"));
    assertEquals("E\\.mail", PreventLineBreakRules.toPattern(CHARACTER_SEQUENCE, "E.mail"));
    assertEquals("E.mail", PreventLineBreakRules.toValue(CHARACTER_SEQUENCE, "E\\.mail"));
    // a backslash that does not escape a metacharacter is left alone
    assertEquals("a\\b", PreventLineBreakRules.toValue(CHARACTER_SEQUENCE, "a\\b"));
  }

  @Test
  void convertsUnitsBothWays() {
    assertEquals(KM, PreventLineBreakRules.toPattern(NUMBER_UNIT, "Km"));
    assertEquals("Km", PreventLineBreakRules.toValue(NUMBER_UNIT, KM));
    assertEquals(PreventLineBreakRules.NUMBER_UNIT_PREFIX + "\\$", PreventLineBreakRules.toPattern(NUMBER_UNIT, "$"));
    assertEquals("$", PreventLineBreakRules.toValue(NUMBER_UNIT, PreventLineBreakRules.NUMBER_UNIT_PREFIX + "\\$"));
    assertEquals("€", PreventLineBreakRules.toValue(NUMBER_UNIT, PreventLineBreakRules.toPattern(NUMBER_UNIT, "€")));
  }

  @Test
  void convertsSpecialPatternsBothWays() {
    for (SpecialPattern special : PreventLineBreakRules.SPECIAL_PATTERNS) {
      assertEquals(special.regex(), PreventLineBreakRules.toPattern(SPECIAL_PATTERN, special.label()));
      assertEquals(special.label(), PreventLineBreakRules.toValue(SPECIAL_PATTERN, special.regex()));
    }
    assertEquals("", PreventLineBreakRules.toPattern(SPECIAL_PATTERN, "not a label"));
  }
}
