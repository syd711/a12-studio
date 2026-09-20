package de.a12.studio.models.formmodel;

import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependentCaseTest {

  @Test
  void aCaseWithoutAnythingSetHasNoAction() {
    assertFalse(new DependentCase().hasAction());

    DependentCase onlyMasterValue = new DependentCase();
    onlyMasterValue.setMasterValue("a");
    onlyMasterValue.setFieldRef("");
    assertFalse(onlyMasterValue.hasAction());
  }

  @Test
  void everyKindOfActionCounts() {
    DependentCase notRelevantFalse = new DependentCase();
    notRelevantFalse.setNotRelevant(false);
    DependentCase readonly = new DependentCase();
    readonly.setReadonly(true);
    DependentCase emptyValue = new DependentCase();
    emptyValue.setValue("");
    DependentCase fieldRef = new DependentCase();
    fieldRef.setFieldRef("field_other");
    DependentCase hiddenNodes = new DependentCase();
    hiddenNodes.getNotRelevantNodes().add("section1");

    for (DependentCase dependentCase : new DependentCase[] {notRelevantFalse, readonly, emptyValue, fieldRef, hiddenNodes}) {
      assertTrue(dependentCase.hasAction());
    }
  }

  // SME accepts value "" (clear the dependent field) as the case's action, so saving must not drop it - else a
  // model that validates clean would report a case without action after the next load.
  @Test
  void emptyValueSurvivesRoundTripButAbsentValueStaysAbsent() throws Exception {
    DependentCase withEmptyValue = JsonSettings.objectMapper.readValue("{\"masterValue\":\"a\",\"value\":\"\"}", DependentCase.class);
    assertTrue(withEmptyValue.hasAction());
    assertEquals("\"\"", JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(withEmptyValue)).get("value").toString());

    DependentCase withoutValue = JsonSettings.objectMapper.readValue("{\"masterValue\":\"a\",\"notRelevant\":true}", DependentCase.class);
    assertFalse(JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(withoutValue)).has("value"));
  }
}
