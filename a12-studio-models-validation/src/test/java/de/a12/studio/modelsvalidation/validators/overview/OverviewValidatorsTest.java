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
}
