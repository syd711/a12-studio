package de.a12.studio.modelsvalidation.validators.form;

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

  @Test
  void documentModelReferenceValidatorReportsMissingReference() {
    FormModel model = load("FormDocumentModelReferenceValidator_invalid");
    List<ModelValidationError> errors = new FormDocumentModelReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("document model reference is required"));
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
}
