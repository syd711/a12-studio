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
}
