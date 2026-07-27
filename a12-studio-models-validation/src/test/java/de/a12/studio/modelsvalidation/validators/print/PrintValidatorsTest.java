package de.a12.studio.modelsvalidation.validators.print;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per print model validator, each loading a fixture that contains exactly that error. */
class PrintValidatorsTest {

  private PrintModel load(String name) {
    return TestModels.load("/printmodel/" + name + ".json", PrintModel.class);
  }

  private DocumentModel refDm() {
    return TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
  }

  @Test
  void documentModelReferenceValidatorReportsDottedName() {
    PrintModel model = load("PrintDocumentModelReferenceValidator_invalid");
    List<ModelValidationError> errors = new PrintDocumentModelReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("must not contain dots"));
  }

  @Test
  void fieldReferenceValidatorReportsUnresolvablePath() {
    PrintModel model = load("PrintFieldReferenceValidator_invalid");
    List<ModelValidationError> errors = new PrintFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm()));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("/Root/DoesNotExist"));
  }

  @Test
  void elementReferenceIntegrityValidatorReportsDanglingReferences() {
    PrintModel model = load("PrintElementReferenceIntegrityValidator_invalid");
    List<ModelValidationError> errors = new PrintElementReferenceIntegrityValidator().validate(model, TestModels.context(model));

    // A segment element reference to a missing definition + a structure entry to a missing segment.
    assertEquals(2, errors.size());
  }

  @Test
  void calculationValidatorReportsMissingNameOperationAndModel() {
    PrintModel model = load("PrintCalculationValidator_invalid");
    List<ModelValidationError> errors = new PrintCalculationValidator().validate(model, TestModels.context(model));

    // Empty name, no computation alternatives, and a document model that doesn't exist.
    assertEquals(3, errors.size());
  }

  @Test
  void tableColumnWidthValidatorReportsSumAbove100() {
    PrintModel model = load("PrintTableColumnWidthValidator_invalid");
    List<ModelValidationError> errors = new PrintTableColumnWidthValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("must not exceed 100"));
  }

  @Test
  void imageValidatorReportsMissingAlternativeText() {
    PrintModel model = load("PrintImageValidator_invalid");
    List<ModelValidationError> errors = new PrintImageValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("alternative text"));
  }

  @Test
  void headlineOrderValidatorWarnsAboutSkippedLevel() {
    PrintModel model = load("PrintHeadlineOrderValidator_invalid");
    List<ModelValidationError> errors = new PrintHeadlineOrderValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals(Severity.WARNING.name(), errors.get(0).severity());
    assertTrue(errors.get(0).message().contains("Headline level 2"));
  }
}
