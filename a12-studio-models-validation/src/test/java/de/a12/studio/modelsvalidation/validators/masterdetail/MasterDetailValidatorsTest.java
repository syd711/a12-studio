package de.a12.studio.modelsvalidation.validators.masterdetail;

import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per master-detail model validator, each loading a fixture that contains exactly that error. */
class MasterDetailValidatorsTest {

  @Test
  void referenceValidatorReportsAllMissingModels() {
    MasterDetailModel model = TestModels.load("/masterdetailmodel/MasterDetailReferenceValidator_invalid.json", MasterDetailModel.class);
    List<ModelValidationError> errors = new MasterDetailReferenceValidator().validate(model, TestModels.context(model));

    // Missing overview model + missing document model + missing form model in the mapping.
    assertEquals(3, errors.size());
  }

  @Test
  void typeConsistencyValidatorReportsMissingOverviewModel() {
    MasterDetailModel model = TestModels.load("/masterdetailmodel/MasterDetailTypeConsistencyValidator_invalid.json", MasterDetailModel.class);
    List<ModelValidationError> errors = new MasterDetailTypeConsistencyValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("overview model is required"));
  }

  @Test
  void referenceValidatorReportsMissingTreeModel() {
    MasterDetailModel model = TestModels.load("/masterdetailmodel/MasterDetailReferenceValidator_tree_invalid.json", MasterDetailModel.class);
    List<ModelValidationError> errors = new MasterDetailReferenceValidator().validate(model, TestModels.context(model));

    // Missing tree model + missing document model + missing form model in the mapping.
    assertEquals(3, errors.size());
  }

  @Test
  void typeConsistencyValidatorReportsMissingTreeModel() {
    MasterDetailModel model = TestModels.load("/masterdetailmodel/MasterDetailTypeConsistencyValidator_tree_invalid.json", MasterDetailModel.class);
    List<ModelValidationError> errors = new MasterDetailTypeConsistencyValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("tree model is required"));
  }
}
