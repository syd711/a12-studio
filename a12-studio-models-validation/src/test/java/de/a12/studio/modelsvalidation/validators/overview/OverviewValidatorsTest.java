package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per overview model validator, each loading a fixture that contains exactly that error. */
class OverviewValidatorsTest {

  @Test
  void columnsNotEmptyValidatorReportsMissingColumns() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewColumnsNotEmptyValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewColumnsNotEmptyValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Columns must not be empty"));
  }

  @Test
  void fieldReferenceValidatorReportsMissingAndUnindexedFields() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFieldReferenceValidator_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFieldReferenceValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    // One column references a field that doesn't exist, the other a field annotated indexed=false.
    assertEquals(2, errors.size());
  }

  @Test
  void documentModelRequiredValidatorReportsMissingReference() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewDocumentModelRequiredValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewDocumentModelRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Document Model or Query Model reference is required"));
  }

  @Test
  void filterModeRequiredValidatorReportsMissingFilterMode() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterModeRequiredValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewFilterModeRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("mandatory"));
  }

  @Test
  void filterCustomFieldsValidatorReportsEmptySelection() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterCustomFieldsValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewFilterCustomFieldsValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("At least one field"));
  }

  @Test
  void filterSectionsValidatorReportsMissingIdAndDuplicateField() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewFilterSectionsValidator_invalid.json", OverviewModel.class);
    DocumentModel refDm = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
    List<ModelValidationError> errors = new OverviewFilterSectionsValidator().validate(model,
        TestModels.contextWithDocumentModels(model, refDm));

    // Missing section id, plus the same field selected twice within the section.
    assertEquals(2, errors.size());
  }

  @Test
  void pagingSizeValidatorReportsInvalidValue() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewPagingSizeValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewPagingSizeValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("at least 1"));
  }

  @Test
  void initialSortingReferenceValidatorReportsDeletedColumn() {
    OverviewModel model = TestModels.load("/overviewmodel/OverviewInitialSortingReferenceValidator_invalid.json", OverviewModel.class);
    List<ModelValidationError> errors = new OverviewInitialSortingReferenceValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("no longer exists"));
  }
}
