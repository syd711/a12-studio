package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per form model validator, each loading a fixture that contains exactly that error. */
class FormValidatorsTest {

  private FormModel load(String name) {
    return TestModels.load("/formmodel/" + name + ".json", FormModel.class);
  }

  private DocumentModel refDm() {
    return TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
  }

  private DocumentModel refWithIncludeDm() {
    return TestModels.load("/documentmodel/RefWithInclude_DM.json", DocumentModel.class);
  }

  private DocumentModel refIncludedDm() {
    return TestModels.load("/documentmodel/RefIncluded_DM.json", DocumentModel.class);
  }

  private DocumentModel hideConditionMasterDm() {
    return TestModels.load("/documentmodel/HideConditionMaster_DM.json", DocumentModel.class);
  }

  private DocumentModel dependencyDriftDm() {
    return TestModels.load("/documentmodel/DependencyDrift_DM.json", DocumentModel.class);
  }

  @Test
  void documentModelReferenceValidatorReportsMissingReference() {
    FormModel model = load("FormDocumentModelReferenceValidator_invalid");
    List<ModelValidationError> errors = new FormDocumentModelReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("document model reference is required"));
  }

  @Test
  void documentModelReferenceValidatorAcceptsCombinedDocumentModelReference() {
    FormModel model = load("FormDocumentModelReferenceValidator_combination_valid");
    CombinedDocumentModel combinedModel = TestModels.load("/combineddocumentmodel/Ref_Cm.json", CombinedDocumentModel.class);
    List<ModelValidationError> errors = new FormDocumentModelReferenceValidator().validate(model,
        TestModels.contextWithOtherModels(model, combinedModel));

    assertEquals(0, errors.size());
  }

  @Test
  void fieldReferenceValidatorReportsUnknownField() {
    FormModel model = load("FormFieldReferenceValidator_invalid");
    List<ModelValidationError> errors = new FormFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm()));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("field_missing"));
  }

  @Test
  void fieldReferenceValidatorResolvesFieldThroughInclude() {
    FormModel model = load("FormFieldReferenceValidator_include_valid");
    List<ModelValidationError> errors = new FormFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refWithIncludeDm(), refIncludedDm()));

    assertEquals(0, errors.size());
  }

  @Test
  void groupReferenceValidatorReportsUnknownGroup() {
    FormModel model = load("FormGroupReferenceValidator_invalid");
    List<ModelValidationError> errors = new FormGroupReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm()));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("group_missing"));
  }

  @Test
  void unusedConfigEntryValidatorReportsResolvableButUnreferencedFieldAndGroup() {
    FormModel model = load("FormUnusedConfigEntryValidator_invalid");
    List<ModelValidationError> errors = new FormUnusedConfigEntryValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm()));

    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(e -> e.message().contains("field_1")));
    assertTrue(errors.stream().anyMatch(e -> e.message().contains("group_root")));
  }

  @Test
  void layoutColumnSumValidatorReportsSumAbove12AndColumnCountMismatch() {
    FormModel model = load("FormLayoutColumnSumValidator_invalid");
    List<ModelValidationError> errors = new FormLayoutColumnSumValidator().validate(model, TestModels.context(model));

    // lg "6-6-6" sums to 18 (> 12) and md "6-6" has a different column count than lg.
    assertEquals(2, errors.size());
  }

  @Test
  void siblingNameUniquenessValidatorReportsDuplicateScreenAndSectionNames() {
    FormModel model = load("FormSiblingNameUniquenessValidator_invalid");
    List<ModelValidationError> errors = new FormSiblingNameUniquenessValidator().validate(model, TestModels.context(model));

    // Two screens named "SameScreen" + two sibling sections named "SameSection".
    assertEquals(2, errors.size());
  }

  @Test
  void controlGridLayoutValidatorReportsWrongColumnCountAndExceedsIndex() {
    FormModel model = load("ControlGridLayoutValidator_invalid");
    List<ModelValidationError> errors = new ControlGridLayoutValidator().validate(model, TestModels.context(model));

    // A 2-column ("6-6") grid whose row has 3 cells: one "wrong number of columns" message (lg only) plus
    // one "exceeds max index" message for the 3rd cell per breakpoint (lg, md - cascaded from layout.md
    // "12-12" - and sm, cascaded from md since layout.sm is unset) - matches SME's real 4-message output
    // for this exact case (Invoice_FM.json's "BillingAddressControls").
    assertEquals(4, errors.size());
    assertEquals(1, errors.stream().filter(e -> e.message().contains("wrong number of columns")).count());
    assertEquals(3, errors.stream().filter(e -> e.message().contains("exceeds")).count());
    assertTrue(errors.get(0).message().contains(
        "Form model field [BillingAddressControls] contains a wrong number of columns for layout lg. "
            + "The expected number of columns is 2 but there are 3 defined columns."));
    assertTrue(errors.stream().anyMatch(e -> e.message().equals(
        "The element [control-7fc85] exceeds for layout lg with offset [0] the defined maximum index [2] "
            + "for the control grid [BillingAddressControls].")));
    assertTrue(errors.stream().anyMatch(e -> e.message().equals(
        "The element [control-7fc85] exceeds for layout md with offset [0] the defined maximum index [2] "
            + "for the control grid [BillingAddressControls].")));
    assertTrue(errors.stream().anyMatch(e -> e.message().equals(
        "The element [control-7fc85] exceeds for layout sm with offset [0] the defined maximum index [2] "
            + "for the control grid [BillingAddressControls].")));
  }

  @Test
  void controlGridLayoutValidatorAllowsRowsThatUseFewerColumnsThanDefined() {
    FormModel model = load("ControlGridLayoutValidator_valid");
    List<ModelValidationError> errors = new ControlGridLayoutValidator().validate(model, TestModels.context(model));

    // A single-cell row in a 2-column grid, and an offset-1 single-cell row in another 2-column grid: both
    // under-fill (or exactly fill, via offset) the defined columns rather than overflowing them.
    assertEquals(0, errors.size());
  }

  @Test
  void hideConditionAtLeastOneCaseValidatorReportsEmptyCases() {
    FormModel model = load("HideConditionAtLeastOneCaseValidator_invalid");
    List<ModelValidationError> errors = new HideConditionAtLeastOneCaseValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("At least one hide condition value"));
  }

  @Test
  void hideConditionAtLeastOneCaseValidatorCoversRepeatOverviewColumns() {
    FormModel model = load("HideConditionColumnValidator_invalid");
    List<ModelValidationError> errors = new HideConditionAtLeastOneCaseValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("column_hidden", errors.get(0).elementId());
  }

  @Test
  void styleReferenceValidatorReportsUndefinedAndNamelessStyles() {
    FormModel model = load("FormStyleReferenceValidator_invalid");
    List<ModelValidationError> errors = new FormStyleReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(3, errors.size(), errors.toString());
    // The nameless entry of the model-level styles has no element of its own.
    assertTrue(errors.stream().anyMatch(e -> FormStyleReferenceValidator.ELEMENT_ID.equals(e.elementId())));
    ModelValidationError ghost = errors.stream().filter(e -> "section_ghost".equals(e.elementId())).findFirst().orElseThrow();
    assertTrue(ghost.message().contains("ghost") && ghost.message().contains("Undefined"), ghost.message());
    assertTrue(errors.stream().anyMatch(e -> "section_nameless".equals(e.elementId())));
    assertTrue(errors.stream().noneMatch(e -> "section_ok".equals(e.elementId())), "a defined style is fine");
  }

  @Test
  void hideConditionSupportedValuesValidatorReportsUnsupportedValues() {
    FormModel model = load("HideConditionSupportedValuesValidator_invalid");
    List<ModelValidationError> errors = new HideConditionSupportedValuesValidator().validate(model,
        TestModels.contextWithDocumentModels(model, hideConditionMasterDm()));

    // "maybe" isn't a valid Boolean hide value, and "c" isn't a declared value of the Status enum.
    assertEquals(2, errors.size());
  }

  @Test
  void dependentFieldMasterRequiredValidatorReportsMissingMaster() {
    FormModel model = load("DependentFieldMasterRequiredValidator_invalid");
    List<ModelValidationError> errors = new DependentFieldMasterRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("field_dependent"));
  }

  @Test
  void dependentGroupMasterRequiredValidatorReportsMissingMaster() {
    FormModel model = load("DependentGroupMasterRequiredValidator_invalid");
    List<ModelValidationError> errors = new DependentGroupMasterRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("group_dependent"));
  }

  @Test
  void dependentEnumerationMasterRequiredValidatorReportsMissingMaster() {
    FormModel model = load("DependentEnumerationMasterRequiredValidator_invalid");
    List<ModelValidationError> errors = new DependentEnumerationMasterRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("field_dependent_enum"));
  }

  @Test
  void buttonScreenReferenceValidatorReportsOnlyTargetsThatAreNeitherScreensNorSpecialTokens() {
    FormModel model = load("FormButtonScreenReferenceValidator_invalid");
    List<ModelValidationError> errors = new FormButtonScreenReferenceValidator().validate(model, TestModels.context(model));

    // "ToSecond" (an existing screen) and "Next" (#next) are fine; the model-level and the per-screen footer
    // each hold one button pointing at a deleted screen.
    assertEquals(2, errors.size());
    assertTrue(errors.stream().anyMatch(e -> e.elementId().equals("button_gone")
        && e.message().contains("screen_gone") && e.message().contains("ToGone")));
    assertTrue(errors.stream().anyMatch(e -> e.elementId().equals("button_stale")
        && e.message().contains("screen_deleted") && e.message().contains("StaleScreenButton")));
  }

  @Test
  void dependentControlOptionsMustExistValidatorReportsMissingOtherScreenAndWrongTypeOptions() {
    FormModel model = load("DependentControlOptionsMustExistValidator_invalid");
    List<ModelValidationError> errors = new DependentControlOptionsMustExistValidator().validate(model, TestModels.context(model));

    // section_ok is fine. Left over: a deleted grid, an entry without idref, an element of another screen and a
    // ButtonPanel (not a Section/Control Grid/Custom Screen Element), all reported on the master Control - plus
    // the deleted node in the Confirm control's notRelevantNodes.
    assertEquals(5, errors.size());
    List<ModelValidationError> onControl = errors.stream().filter(e -> "control_master".equals(e.elementId())).toList();
    assertEquals(4, onControl.size());
    assertTrue(onControl.stream().allMatch(e -> e.message().contains("field_confirm")));
    assertTrue(onControl.stream().anyMatch(e -> e.message().contains("grid_ghost") && e.message().contains("does not exist")));
    assertTrue(onControl.stream().anyMatch(e -> e.message().contains("without a screen element selected")));
    assertTrue(onControl.stream().anyMatch(e -> e.message().contains("section_other_screen") && e.message().contains("same screen")));
    assertTrue(onControl.stream().anyMatch(e -> e.message().contains("buttonpanel1") && e.message().contains("not a section")));
    assertTrue(errors.stream().anyMatch(e -> e.message().contains("section_deleted") && e.message().contains("field_confirm")));
  }

  @Test
  void dependentControlsAtLeastOneOptionValidatorReportsOnlyEmptyDependency() {
    FormModel model = load("DependentControlsAtLeastOneOptionValidator_invalid");
    List<ModelValidationError> errors = new DependentControlsAtLeastOneOptionValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("control_empty", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("field_empty"));
  }

  @Test
  void dependentFieldAtLeastOneActionValidatorReportsOnlyCasesWithoutAnyAction() {
    FormModel model = load("DependentFieldAtLeastOneActionValidator_invalid");
    List<ModelValidationError> errors = new DependentFieldAtLeastOneActionValidator().validate(model, TestModels.context(model));

    // Case "a" and the "no value" case do nothing. notRelevant=false, readonly, value="", fieldRef and hidden nodes
    // all count as an action; a dependentField without master field is DependentFieldMasterRequired's business.
    assertEquals(2, errors.size());
    assertTrue(errors.stream().allMatch(e -> e.message().contains("field_dependent")));
    assertTrue(errors.stream().anyMatch(e -> e.message().contains("case \"a\"")));
    assertTrue(errors.stream().anyMatch(e -> e.message().contains("(no value)")));
  }

  @Test
  void dependencyDriftValidatorReportsMastersAndValuesTheDocumentModelNoLongerHas() {
    FormModel model = load("FormDependencyDriftValidator_invalid");
    List<ModelValidationError> errors = new FormDependencyDriftValidator().validate(model,
        TestModels.contextWithDocumentModels(model, dependencyDriftDm()));

    List<String> messages = errors.stream().map(ModelValidationError::message).toList();
    // Hide conditions: a master that is a String, and one that was deleted.
    assertHasOne(messages, "hide condition", "section_hide_string", "field_string", "not a boolean, confirm or enumeration");
    assertHasOne(messages, "hide condition", "section_hide_gone", "field_deleted", "no longer exists");
    // Control dependencies: the trigger's own field is a String; the enumeration has no value "zz" ("a" and
    // "(no value)" are fine).
    assertHasOne(messages, "control dependency", "field_string", "not a boolean, confirm or enumeration");
    assertHasOne(messages, "control dependency", "field_enum", "\"zz\"");
    // Dependent field: master is a String / deleted; case "z" has no such master value; forced value "gone" and
    // the copy source "field_ghost" no longer exist.
    assertHasOne(messages, "dependent field configuration", "\"field_string\" uses the master field \"field_string\"", "not a boolean");
    assertHasOne(messages, "dependent field configuration", "field_gone", "no longer exists");
    assertHasOne(messages, "dependent field configuration", "field_enum_dep", "\"z\"");
    assertHasOne(messages, "sets the value \"gone\"", "field_enum_dep");
    assertHasOne(messages, "copies the value of the field \"field_ghost\"", "field_enum_dep");
    // Dependent enumeration: on a String field; master is a Boolean; offered value "q" and switch-to value "w" gone.
    assertHasOne(messages, "dependent enumeration of \"field_string\" is set up for a field that is not an enumeration");
    assertHasOne(messages, "dependent enumeration", "field_enum_dep", "field_bool", "not an enumeration field");
    assertHasOne(messages, "offers the value \"q\"");
    assertHasOne(messages, "switches to the value \"w\"");
    // Dependent group: master value "maybe" is no Boolean value.
    assertHasOne(messages, "dependent group configuration", "group_items", "\"maybe\"");
    assertEquals(14, errors.size(), messages.toString());
  }

  @Test
  void dependencyDriftValidatorIsSilentWithoutTheDocumentModel() {
    FormModel model = load("FormDependencyDriftValidator_invalid");

    assertEquals(0, new FormDependencyDriftValidator().validate(model, TestModels.context(model)).size());
  }

  @Test
  void dependentControlContextValidatorReportsAncestorsAndForeignDataContexts() {
    FormModel model = load("FormDependentControlContextValidator_invalid");
    List<ModelValidationError> errors = new FormDependentControlContextValidator().validate(model,
        TestModels.contextWithDocumentModels(model, dependencyDriftDm()));

    List<String> found = errors.stream().map(e -> e.elementId() + "->" + e.message().replaceAll(".*hides \"([^\"]+)\", which (.*)\\.$", "$1:$2")).toList();
    // control_master hides its own section and grid; hiding a plain grid and a grid inside a repeat is fine for
    // a control outside every repeat. control_row (in the repeat) can hide its own grid neither, nor a grid
    // outside the repeat (it has many instances of the trigger). control_outside needs an index, so it may only
    // hide elements outside a repeat.
    assertEquals(5, errors.size(), found.toString());
    assertTrue(found.contains("control_master->section_top:contains the control itself"), found.toString());
    assertTrue(found.contains("control_master->grid_master:contains the control itself"), found.toString());
    assertTrue(found.contains("control_row->grid_repeat:contains the control itself"), found.toString());
    assertTrue(found.contains("control_row->grid_ok:is in a data context (repeat) that the control cannot control uniquely"), found.toString());
    assertTrue(found.contains("control_outside->grid_repeat:is in a data context (repeat) that the control cannot control uniquely"), found.toString());
  }

  @Test
  void referenceTypeDriftValidatorReportsReferencesToTheWrongKindOfElement() {
    FormModel model = load("FormReferenceTypeDriftValidator_invalid");
    List<ModelValidationError> errors = new FormReferenceTypeDriftValidator().validate(model,
        TestModels.contextWithDocumentModels(model, dependencyDriftDm()));

    assertEquals(4, errors.size(), errors.toString());
    assertTrue(errors.stream().anyMatch(e -> e.elementId().equals("control_group")
        && e.message().contains("group_single") && e.message().contains("not a field")));
    assertTrue(errors.stream().anyMatch(e -> e.elementId().equals("column_group")
        && e.message().contains("group_single") && e.message().contains("not a field")));
    assertTrue(errors.stream().anyMatch(e -> e.elementId().equals("repeat_field")
        && e.message().contains("field_string") && e.message().contains("not a group")));
    assertTrue(errors.stream().anyMatch(e -> e.elementId().equals("repeat_single")
        && e.message().contains("group_single") && e.message().contains("not repeatable")));
  }

  @Test
  void controlIndexRequiredValidatorReportsOnlyIndexableControlsWithoutIndex() {
    FormModel model = load("FormControlIndexRequiredValidator_invalid");
    List<ModelValidationError> errors = new FormControlIndexRequiredValidator().validate(model,
        TestModels.contextWithDocumentModels(model, dependencyDriftDm()));

    // control_needs shows a field of the repeatable group outside its repeat; the one with an index, the plain
    // one and the one inside its repeat are fine.
    assertEquals(1, errors.size(), errors.toString());
    assertEquals("control_needs", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("field_item_bool"));
  }

  /** Every needle must be in exactly one of the messages (which also pins that a case is reported once). */
  private static void assertHasOne(List<String> messages, String... needles) {
    long matches = messages.stream().filter(message -> List.of(needles).stream().allMatch(message::contains)).count();
    assertEquals(1, matches, "messages containing " + List.of(needles) + " in " + messages);
  }

  @Test
  void externalEnumerationSourceRequiredValidatorReportsMissingSource() {
    FormModel model = load("ExternalEnumerationSourceRequiredValidator_invalid");
    List<ModelValidationError> errors = new ExternalEnumerationSourceRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("field_external_enum"));
  }
}
